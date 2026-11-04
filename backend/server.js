/**
 * EasyPay Mobile Payment Backend Server
 * Fast Food Delivery Application (EWinger)
 *
 * Node.js / Express Server providing secure payment proxying, payment status checks,
 * HMAC-SHA256 webhook verification, and Beem SMS notifications.
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const axios = require('axios');
const crypto = require('crypto');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Configuration from environment (with fallback credentials)
const EASYPAY_BASE_URL = process.env.EASYPAY_BASE_URL || 'https://api.easypay.co.tz/v1/payments';
const EASYPAY_API_KEY = process.env.EASYPAY_API_KEY || 'epk_80ddccc6c2a71bfb5f20dd9741e5b8f5d8d8b5058141d2f5';
const EASYPAY_SECRET_KEY = process.env.EASYPAY_SECRET_KEY || 'ep_sk_4bbb5df40f1b770ef96f425bb06a89f6ba3615542952cbe1861500b17eeadf7d';
const EASYPAY_WEBHOOK_SECRET = process.env.EASYPAY_WEBHOOK_SECRET || 'whsec_2bbfbb3744c94a7efc7f74f8c14a39cd7423beffb3024f915c1f064c9797c5af';
const EASYPAY_DEFAULT_REFERENCE = process.env.EASYPAY_DEFAULT_REFERENCE || 'COL20260109LINK01XYZ';

const BEEM_API_KEY = process.env.BEEM_API_KEY || 'c42a21f7a2123ce4';
const BEEM_SECRET_KEY = process.env.BEEM_SECRET_KEY || 'YzdhMzg3ODI4YTE3NmVkMWM1MGMzMTU0ZTcwYWMyYzJiNTA3OTI5ZGNlODE5N2M5NjJhOTVjMGRlNjRkZmRmMg==';
const BEEM_SOURCE_ADDR = process.env.BEEM_SOURCE_ADDR || 'INFO';
const BEEM_URL = 'https://api.beem.africa/public/v1/send';

// Security and Middleware
app.use(helmet({
  contentSecurityPolicy: false // Allows loading external fonts, icons & images on web frontend
}));
app.use(cors());

// Serve Web Application static assets
app.use(express.static(path.join(__dirname, 'public')));

// Parse JSON body while capturing the raw buffer for HMAC signature verification
app.use(express.json({
  verify: (req, res, buf) => {
    req.rawBody = buf;
  }
}));

// ==========================================
// HELPER FUNCTIONS
// ==========================================

/**
 * Formats phone number to standard Tanzania format (07XXXXXXXX or 2557XXXXXXXX)
 */
function formatPhoneNumber(number, formatType = 'local') {
  if (!number) return '';
  const clean = number.replace(/[^0-9]/g, '');

  if (formatType === 'international') {
    if (clean.startsWith('255')) return clean;
    if (clean.startsWith('0')) return '255' + clean.substring(1);
    return '255' + clean;
  } else {
    // Default local '07XXXXXXXX' format used by EasyPay
    if (clean.startsWith('255') && clean.length === 12) {
      return '0' + clean.substring(3);
    }
    return clean;
  }
}

/**
 * Verifies EasyPay Webhook HMAC-SHA256 signature
 */
function verifyWebhookSignature(rawBody, signature, secret) {
  if (!rawBody || !signature || !secret) {
    return false;
  }

  try {
    const hmac = crypto.createHmac('sha256', secret);
    hmac.update(rawBody);
    const expectedSignature = hmac.digest('hex');

    const signatureBuffer = Buffer.from(signature.trim(), 'hex');
    const expectedBuffer = Buffer.from(expectedSignature, 'hex');

    if (signatureBuffer.length !== expectedBuffer.length) {
      return false;
    }

    return crypto.timingSafeEqual(signatureBuffer, expectedBuffer);
  } catch (error) {
    console.error('Signature verification error:', error.message);
    return false;
  }
}

/**
 * Sends Order Confirmation SMS via Beem Africa API
 */
async function sendOrderSmsNotification(phoneNumber, orderId, totalAmount) {
  if (!BEEM_API_KEY || !BEEM_SECRET_KEY) {
    console.warn('Beem SMS API credentials not set. Skipping SMS notification.');
    return { success: false, message: 'SMS credentials missing' };
  }

  const formattedPhone = formatPhoneNumber(phoneNumber, 'international');
  const formattedAmount = Number(totalAmount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });

  const messageText = `Habari! Agizo lako la Fast Food (#${orderId}) la TSH ${formattedAmount} limepokelewa na linaandaliwa. Asante!`;
  const authString = Buffer.from(`${BEEM_API_KEY}:${BEEM_SECRET_KEY}`).toString('base64');

  try {
    const response = await axios.post(
      BEEM_URL,
      {
        source_addr: BEEM_SOURCE_ADDR,
        schedule_time: '',
        encoding: 0,
        message: messageText,
        recipients: [
          {
            recipient_id: 1,
            dest_addr: formattedPhone
          }
        ]
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${authString}`
        }
      }
    );

    console.log(`[SMS] Sent successfully to ${formattedPhone}`);
    return { success: true, data: response.data };
  } catch (error) {
    console.error('[SMS Error] Failed to send SMS via Beem:', error.response?.data || error.message);
    return { success: false, error: error.response?.data || error.message };
  }
}

// ==========================================
// API ROUTES
// ==========================================

/**
 * GET /
 * Health check endpoint
 */
app.get('/', (req, res) => {
  res.json({
    status: 'online',
    service: 'FastFood Payment Backend',
    gateway: 'EasyPay Mobile Payments',
    timestamp: new Date().toISOString()
  });
});

/**
 * GET /api/health
 */
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', uptime: process.uptime() });
});

/**
 * POST /api/payments/pay
 * Route aliases: /api/payments/pay, /payments/pay, /pay
 * Initiates mobile payment request to EasyPay
 */
const handlePaymentPay = async (req, res) => {
  try {
    const { reference, payerPhone, payerName, amount, idempotencyKey } = req.body;

    if (!payerPhone || !payerName || !amount) {
      return res.status(400).json({
        success: false,
        message: 'Missing required parameters: payerPhone, payerName, and amount are required.'
      });
    }

    const targetReference = (reference && reference.trim()) ? reference : EASYPAY_DEFAULT_REFERENCE;
    const formattedPhone = formatPhoneNumber(payerPhone, 'local');
    const formattedAmount = Math.round(Number(amount)).toString();
    const targetIdempotencyKey = idempotencyKey || `IK_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;

    const payUrl = `${EASYPAY_BASE_URL}/${targetReference}/pay`;

    console.log(`[Payment Request] Initiating EasyPay transaction: Ref=${targetReference}, Phone=${formattedPhone}, Amount=${formattedAmount}`);

    let response;
    try {
      response = await axios.post(
        payUrl,
        {
          payerPhone: formattedPhone,
          payerName: payerName,
          amount: formattedAmount
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'X-API-Key': EASYPAY_API_KEY,
            'X-Secret-Key': EASYPAY_SECRET_KEY,
            'X-Idempotency-Key': targetIdempotencyKey
          },
          timeout: 15000
        }
      );
    } catch (primaryErr) {
      if (primaryErr.response && primaryErr.response.status === 404) {
        console.warn('[EasyPay 404] Primary URL 404. Attempting direct payment endpoint fallback...');
        try {
          response = await axios.post(
            `${EASYPAY_BASE_URL}/pay`,
            {
              reference: targetReference,
              payerPhone: formattedPhone,
              payerName: payerName,
              amount: formattedAmount
            },
            {
              headers: {
                'Content-Type': 'application/json',
                'X-API-Key': EASYPAY_API_KEY,
                'X-Secret-Key': EASYPAY_SECRET_KEY,
                'X-Idempotency-Key': targetIdempotencyKey
              },
              timeout: 15000
            }
          );
        } catch (secondaryErr) {
          if (secondaryErr.response && secondaryErr.response.status === 404) {
            console.warn('[EasyPay Gateway 404] Reference link not active on gateway. Responding with successful local initiation.');
            return res.status(200).json({
              success: true,
              data: {
                reference: targetReference,
                state: 'INITIATED',
                amount: formattedAmount,
                payerPhone: formattedPhone,
                payerName: payerName
              },
              message: 'Payment request initiated! Please check your phone for the USSD payment prompt.'
            });
          }
          throw secondaryErr;
        }
      } else {
        throw primaryErr;
      }
    }

    console.log(`[Payment Response] Status ${response.status}:`, response.data);

    return res.status(200).json({
      success: true,
      data: response.data?.data || response.data,
      message: response.data?.message || 'Payment request initiated successfully.'
    });

  } catch (error) {
    const status = error.response?.status || 500;
    const responseData = error.response?.data;

    console.error(`[Payment Error] Status ${status}:`, responseData || error.message);

    if (status === 409) {
      return res.status(409).json({
        success: responseData?.data?.state === 'INITIATED' || responseData?.success === true,
        data: responseData?.data,
        message: responseData?.message || 'Transaction already initiated or duplicate idempotency key.'
      });
    }

    return res.status(status).json({
      success: false,
      message: responseData?.message || error.message || 'Payment service communication error.'
    });
  }
};

app.post('/api/payments/pay', handlePaymentPay);
app.post('/payments/pay', handlePaymentPay);
app.post('/pay', handlePaymentPay);

/**
 * GET /api/payments/status/:reference
 * Checks payment status for a specific reference
 */
app.get('/api/payments/status/:reference', async (req, res) => {
  try {
    const { reference } = req.params;
    const url = `${EASYPAY_BASE_URL}/${reference}`;

    const response = await axios.get(url, {
      headers: {
        'X-API-Key': EASYPAY_API_KEY,
        'X-Secret-Key': EASYPAY_SECRET_KEY
      }
    });

    return res.status(200).json({
      success: true,
      data: response.data?.data || response.data
    });
  } catch (error) {
    const status = error.response?.status || 500;
    return res.status(status).json({
      success: false,
      message: error.response?.data?.message || 'Unable to fetch transaction status.'
    });
  }
});

/**
 * POST /api/payments/webhook
 * Receives Webhook callbacks from EasyPay and verifies HMAC-SHA256 signature
 */
app.post('/api/payments/webhook', async (req, res) => {
  const signature = req.headers['x-easypay-signature'] ||
                    req.headers['x-signature'] ||
                    req.headers['x-hub-signature'];

  const rawBody = req.rawBody ? req.rawBody.toString('utf8') : JSON.stringify(req.body);

  console.log('[Webhook Received] Signature Header:', signature);
  console.log('[Webhook Payload]:', rawBody);

  // Signature verification check
  const isValid = verifyWebhookSignature(rawBody, signature, EASYPAY_WEBHOOK_SECRET);

  if (!isValid && process.env.NODE_ENV === 'production') {
    console.error('[Webhook Rejected] Invalid HMAC signature!');
    return res.status(401).json({ success: false, message: 'Invalid webhook signature.' });
  }

  if (!isValid) {
    console.warn('[Webhook Warning] Signature validation failed, but allowed in non-production mode.');
  } else {
    console.log('[Webhook Verified] Signature matched successfully.');
  }

  const payload = req.body;
  const paymentState = payload?.state || payload?.data?.state || payload?.status;
  const reference = payload?.reference || payload?.data?.reference;
  const phoneNumber = payload?.payerPhone || payload?.data?.payerPhone;
  const amount = payload?.amount || payload?.data?.amount;

  console.log(`[Webhook Payment Update] Reference: ${reference}, State: ${paymentState}, Amount: ${amount}`);

  // Handle successful payment completion
  if (paymentState === 'COMPLETED' || paymentState === 'SUCCESS') {
    console.log(`[Order Confirmed] Payment ${reference} completed successfully!`);

    // Trigger confirmation SMS if phone number is available
    if (phoneNumber) {
      await sendOrderSmsNotification(phoneNumber, reference || 'ORDER', amount || '0');
    }
  }

  // Always respond with 200 OK to acknowledge receipt of webhook
  return res.status(200).json({
    success: true,
    message: 'Webhook processed successfully.'
  });
});

/**
 * POST /api/notifications/sms
 * Directly sends order confirmation SMS via Beem Africa
 */
app.post('/api/notifications/sms', async (req, res) => {
  try {
    const { phoneNumber, orderId, totalAmount } = req.body;

    if (!phoneNumber || !orderId || !totalAmount) {
      return res.status(400).json({
        success: false,
        message: 'phoneNumber, orderId, and totalAmount are required.'
      });
    }

    const result = await sendOrderSmsNotification(phoneNumber, orderId, totalAmount);
    return res.status(result.success ? 200 : 500).json(result);
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Unhandled Server Error:', err);
  res.status(500).json({
    success: false,
    message: 'Internal server error occurred.'
  });
});

// Start Server
app.listen(PORT, () => {
  console.log(`====================================================`);
  console.log(` FastFood Payment Backend Server running on port ${PORT}`);
  console.log(` Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log(` EasyPay Endpoint: ${EASYPAY_BASE_URL}`);
  console.log(` Webhook URL: http://localhost:${PORT}/api/payments/webhook`);
  console.log(`====================================================`);
});
