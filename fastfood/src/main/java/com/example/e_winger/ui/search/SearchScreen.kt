package com.example.e_winger.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.e_winger.R
import com.example.e_winger.data.model.Product
import com.example.e_winger.data.model.sampleProducts
import com.example.e_winger.ui.home.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onProductClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchHistory by remember { mutableStateOf(listOf("chips", "chips yai", "mishikaki", "water")) }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val resources = LocalContext.current.resources

    val filteredProducts = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else sampleProducts.filter { 
            resources.getString(it.nameRes).contains(searchQuery, ignoreCase = true) || 
            resources.getString(it.categoryRes).contains(searchQuery, ignoreCase = true) 
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { 
                if (it.isNotBlank()) {
                    if (!searchHistory.contains(it)) {
                        searchHistory = (listOf(it) + searchHistory).take(10)
                    }
                    isSearchActive = true
                    focusManager.clearFocus()
                }
            },
            onBack = { 
                isSearchActive = false
                searchQuery = ""
            }
        )

        if (searchQuery.isEmpty() && !isSearchActive) {
            SearchHistorySection(
                history = searchHistory,
                onHistoryItemClick = { 
                    searchQuery = it
                    isSearchActive = true
                },
                onClearHistory = { searchHistory = emptyList() }
            )
        } else if (filteredProducts.isEmpty() && searchQuery.isNotEmpty()) {
            NoResultsView()
        } else {
            SearchResultsGrid(
                products = filteredProducts,
                onProductClick = onProductClick
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { 
            if (query.isNotEmpty()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.LightGray.copy(alpha = 0.1f),
            focusedContainerColor = Color.LightGray.copy(alpha = 0.1f),
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun SearchHistorySection(
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Text("Clear All", color = Color.Gray)
                }
            }
        }
        
        if (history.isEmpty()) {
            Text(
                "No recent searches",
                modifier = Modifier.padding(top = 8.dp),
                color = Color.Gray
            )
        } else {
            Column {
                history.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHistoryItemClick(item) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = item, style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun SearchResultsGrid(
    products: List<Product>,
    onProductClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            // Reusing a simplified horizontal card or the same ProductCard
            // Let's use the same for now for consistency
            ProductCard(product = product, onClick = { onProductClick(product.id) })
        }
    }
}

@Composable
fun NoResultsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No results found", color = Color.Gray)
        }
    }
}
