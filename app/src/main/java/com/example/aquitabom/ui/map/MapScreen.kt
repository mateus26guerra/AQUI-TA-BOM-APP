package com.example.aquitabom.ui.map

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.ui.CommonTopBar
import com.google.gson.Gson

@Composable
fun MapScreen(
    onRestaurantClick: (Restaurant) -> Unit,
    isVisible: Boolean = true,
    viewModel: MapViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val viewMode = viewModel.viewMode
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.onRefresh()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CommonTopBar()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Toggle Buttons and Real Time
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(2.dp)
            ) {
                ToggleButton(
                    text = "Mapa",
                    icon = Icons.Outlined.Map,
                    isSelected = viewMode == MapViewMode.MAP,
                    onClick = { viewModel.viewMode = MapViewMode.MAP }
                )
                ToggleButton(
                    text = "Restaurantes",
                    icon = Icons.Outlined.Restaurant,
                    isSelected = viewMode == MapViewMode.LIST,
                    onClick = { viewModel.viewMode = MapViewMode.LIST }
                )
            }
            
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2ecc71)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TEMPO REAL", 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar - Only show in LIST mode
        if (viewMode == MapViewMode.LIST) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                placeholder = { Text("Buscar restaurantes, culinária...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Outlined.Close, contentDescription = null, tint = Color.Gray)
                            }
                        }
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                },
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is MapUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MapUiState.Success -> {
                    if (viewMode == MapViewMode.MAP) {
                        LeafletMapView(
                            restaurants = uiState.restaurants,
                            onRestaurantClick = { selectedRestaurant = it }
                        )
                    } else {
                        RestaurantList(
                            restaurants = uiState.restaurants,
                            onRestaurantClick = onRestaurantClick
                        )
                    }
                }
                is MapUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = uiState.message, color = Color.Red)
                        Button(onClick = { viewModel.onRefresh() }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }
            selectedRestaurant?.let { restaurant ->
                RestaurantPreviewCard(
                    restaurant = restaurant,
                    onDismiss = { selectedRestaurant = null },
                    onOpenProfile = {
                        selectedRestaurant = null
                        onRestaurantClick(restaurant)
                    }
                )
            }
        }
    }
}

@Composable
private fun BoxScope.RestaurantPreviewCard(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (restaurant.urlImagem != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = restaurant.urlImagem,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    RestaurantPreviewInfo(restaurant, Modifier.weight(1f))
                }
            } else {
                RestaurantPreviewInfo(restaurant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("Ver detalhes do local", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text("Fechar", fontSize = 12.sp)
                }
            }

        }
    }
}

@Composable
private fun RestaurantPreviewInfo(
    restaurant: Restaurant,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = restaurant.nome,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = restaurant.endereco,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1
        )
        restaurant.statusLotacao?.let { status ->
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = statusColor(status).copy(alpha = 0.16f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = statusLabel(status),
                    color = statusColor(status),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun statusLabel(status: String): String = when (status) {
    "DE_BOA" -> "De boa"
    "EMBACADO" -> "Embaçado"
    "CHEIO_QUE_SO" -> "Cheio que só"
    else -> status.replace("_", " ")
}

private fun statusColor(status: String): Color = when (status) {
    "DE_BOA" -> Color(0xFF2ECC71)
    "EMBACADO" -> Color(0xFFF1C40F)
    "CHEIO_QUE_SO" -> Color(0xFFE74C3C)
    else -> Color.Gray
}

@Composable
fun ToggleButton(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFFE67E22) else Color.Transparent,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RestaurantList(restaurants: List<Restaurant>, onRestaurantClick: (Restaurant) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RESULTADO", 
                fontWeight = FontWeight.Bold, 
                color = Color.Gray, 
                fontSize = 12.sp
            )
            Text(
                text = "${restaurants.size} encontrados", 
                color = Color.Gray, 
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (restaurants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhum restaurante encontrado",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(restaurants) { restaurant ->
                    RestaurantListItem(restaurant, onClick = { onRestaurantClick(restaurant) })
                }
            }
        }
    }
}

@Composable
fun RestaurantListItem(restaurant: Restaurant, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (restaurant.urlImagem != null) {
                AsyncImage(
                    model = restaurant.urlImagem,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = restaurant.iniciais, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.nome, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(text = "⭐️ 5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFf1c40f))
                }
                
                Text(text = restaurant.endereco, color = Color.Gray, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = restaurant.descricao.orEmpty(), 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), 
                    fontSize = 10.sp, 
                    maxLines = 1
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(restaurants: List<Restaurant>, onRestaurantClick: (Restaurant) -> Unit) {
    val restaurantsJson = Gson().toJson(restaurants)
    val currentRestaurants by rememberUpdatedState(restaurants)
    val currentOnClick by rememberUpdatedState(onRestaurantClick)
    
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <style>
                body, html { margin: 0; padding: 0; height: 100%; width: 100%; overflow: hidden; }
                #map { width: 100%; height: 100vh; background: #f0f0f0; }
                .pino-restaurante {
                    color: white; font-weight: bold; font-size: 12px;
                    border-radius: 50%; width: 32px; height: 32px; display: flex;
                    align-items: center; justify-content: center; border: 2px solid white;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.4);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script>
                window.onload = function() {
                    try {
                        const restaurants = $restaurantsJson;
                        const map = L.map('map').setView([-8.0631, -34.8711], 15);
                        
                        const positron = L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
                            attribution: '&copy; CARTO',
                            maxZoom: 20
                        }).addTo(map);

                        const bairroGeoJSON = {
                          "type": "FeatureCollection",
                          "features": [
                            {
                              "type": "Feature",
                              "properties": {},
                              "geometry": {
                                "type": "Polygon",
                                "coordinates": [
                                  [
                                    [-34.8741692, -8.0514117],
                                    [-34.8687729, -8.0434751],
                                    [-34.8658207, -8.0448959],
                                    [-34.8672422, -8.0483415],
                                    [-34.8672285, -8.0537111],
                                    [-34.8719734, -8.0673902],
                                    [-34.8748158, -8.0667287],
                                    [-34.8746556, -8.0631961],
                                    [-34.8741553, -8.0597143],
                                    [-34.8741692, -8.0514117]
                                  ]
                                ]
                              }
                            }
                          ]
                        };

                        L.geoJSON(bairroGeoJSON, {
                            style: { color: '#007bff', weight: 2, fillOpacity: 0 }
                        }).addTo(map);

                        restaurants.forEach((rest, index) => {
                            if (!rest.latitude || !rest.longitude) return;
                            let lat = parseFloat(rest.latitude);
                            let lng = parseFloat(rest.longitude);
                            if (lng > 0) lng = -lng; 

                            const statusColors = {
                                "DE_BOA": "#2ecc71",
                                "EMBACADO": "#f1c40f",
                                "CHEIO_QUE_SO": "#e74c3c"
                            };
                            const pinColor = statusColors[rest.statusLotacao] || "#95a5a6";
                            const icon = L.divIcon({
                                className: '',
                                html: '<div class="pino-restaurante" style="background-color: ' + pinColor + ';">' + rest.iniciais + '</div>',
                                iconSize: [32, 32], iconAnchor: [16, 16]
                            });
                            
                            L.marker([lat, lng], { icon: icon })
                                .on('click', function() {
                                    Android.onRestaurantClick(index);
                                })
                                .addTo(map);
                        });

                        setTimeout(() => { map.invalidateSize(); }, 500);
                    } catch (e) {
                        console.error("Erro ao inicializar o mapa: " + e.message);
                    }
                };
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            Log.d("WebViewConsole", "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                        }
                        return true
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                
                // Add Javascript Interface
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onRestaurantClick(index: Int) {
                        val restaurants = currentRestaurants
                        if (index >= 0 && index < restaurants.size) {
                            Handler(Looper.getMainLooper()).post {
                                currentOnClick(restaurants[index])
                            }
                        }
                    }
                }, "Android")
                
                loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.requestLayout()
        },
        modifier = Modifier.fillMaxSize()
    )
}
