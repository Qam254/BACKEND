# FastFood Mobile Payment Backend (Node.js / Express)

This backend server handles **EasyPay Mobile Payment Processing**, **HMAC-SHA256 Webhook Verification**, and **Beem Africa SMS Notifications** for the **EWinger FastFood Android Application**.

---

## 🚀 Features

- **EasyPay Integration**: Proxies payment requests to `https://api.easypay.co.tz/v1/payments/{reference}/pay` with API Key, Secret Key, and Idempotency key headers.
- **Payment Status Check**: Fetch payment status by transaction reference.
- **Webhook Verifier**: Authenticates incoming EasyPay webhooks using `HMAC-SHA256` signature verification (matching `WebhookVerifier.kt`).
- **SMS Notifications**: Dispatches automated order confirmation SMS via Beem Africa API.
- **Security**: Hardened using `helmet`, `cors`, and raw body buffer capture for exact HMAC validation.

---

## 🛠️ Project Structure

```
backend/
├── server.js          # Express backend application
├── package.json       # Project dependencies and npm scripts
├── .env               # Environment configuration file
├── .env.example       # Template for environment variables
└── README.md          # Setup & usage documentation
```

---

## ⚙️ Requirements & Installation

1. **Node.js** (v18.x or higher)
2. **npm** (v9.x or higher)

### Setup Steps

1. Navigate to the `backend` folder:
   ```bash
   cd backend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the backend server:
   - **Development mode** (auto-reload):
     ```bash
     npm run dev
     ```
   - **Production mode**:
     ```bash
     npm start
     ```

The server will start at `http://localhost:3000`.

---

## 📡 API Endpoints

### 1. Health Check
- **`GET /`** or **`GET /api/health`**
  - Returns server health status.

---

### 2. Initiate Payment
- **`POST /api/payments/pay`**
- **Body (`application/json`)**:
  ```json
  {
    "reference": "COL20260109LINK01XYZ",
    "payerPhone": "0712345678",
    "payerName": "John Doe",
    "amount": 15000,
    "idempotencyKey": "IK_UNIQUE_KEY_123"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "data": {
      "reference": "COL20260109LINK01XYZ",
      "state": "INITIATED",
      "amount": "15000"
    },
    "message": "Payment request initiated successfully."
  }
  ```

---

### 3. Payment Status Check
- **`GET /api/payments/status/:reference`**
- **Response**: Returns current payment state (`INITIATED`, `COMPLETED`, `FAILED`, etc.).

---

### 4. EasyPay Webhook Callback
- **`POST /api/payments/webhook`**
- **Headers**:
  - `X-EasyPay-Signature`: Hex-encoded HMAC-SHA256 signature calculated over the raw JSON payload using `EASYPAY_WEBHOOK_SECRET`.
- **Behavior**:
  - Validates HMAC signature.
  - Returns `401 Unauthorized` if signature is invalid.
  - Automatically dispatches SMS notification on `COMPLETED` payments.

---

### 5. Send Order SMS Notification
- **`POST /api/notifications/sms`**
- **Body**:
  ```json
  {
    "phoneNumber": "0712345678",
    "orderId": "ORD-98213",
    "totalAmount": 15000
  }
  ```

---

## 📱 Android Client Integration

To connect the Android application to this backend:

1. In `EasyPayService.kt`, update `BASE_URL` to point to your backend server:
   ```kotlin
   // For local development on Android Emulator (10.0.2.2 points to host localhost)
   private const val BACKEND_URL = "http://10.0.2.2:3000/api/payments/pay"
   ```
2. Or use `ngrok` for testing remote webhook URLs during development:
   ```bash
   ngrok http 3000
   ```
