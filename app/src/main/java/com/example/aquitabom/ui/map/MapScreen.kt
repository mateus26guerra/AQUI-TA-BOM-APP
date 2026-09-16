package com.example.aquitabom.ui.map

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    viewModel: MapViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val viewMode = viewModel.viewMode

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
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
                    .background(Color(0xFFEEEEEE))
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
                color = Color.White,
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2ecc71)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "TEMPO REAL", fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFEEEEEE)))
                    }
                },
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEEEEEE),
                    unfocusedContainerColor = Color(0xFFEEEEEE),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
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
                        LeafletMapView(uiState.restaurants)
                    } else {
                        RestaurantList(uiState.restaurants)
                    }
                }
                is MapUiState.Error -> {
                    Text(text = uiState.message, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
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
fun RestaurantList(restaurants: List<Restaurant>) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "RESULTADO", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
            Text(text = "${restaurants.size} encontrados", color = Color.Gray, fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(restaurants) { restaurant ->
                RestaurantListItem(restaurant)
            }
        }
    }
}

@Composable
fun RestaurantListItem(restaurant: Restaurant) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = restaurant.iniciais, color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = restaurant.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                // Score is not in current API, keeping as mocked 5.0 for UI consistency
                Text(text = "⭐️ 5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFf1c40f))
            }
            
            Text(text = restaurant.endereco, color = Color.Gray, fontSize = 12.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = restaurant.descricao, color = Color.DarkGray, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(restaurants: List<Restaurant>) {
    val restaurantsJson = Gson().toJson(restaurants)
    
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
                .seletor-tema {
                    position: absolute; top: 10px; right: 10px; z-index: 1000;
                    background: rgba(255, 255, 255, 0.9); padding: 8px; border-radius: 8px;
                    max-height: 50%; overflow-y: auto; font-family: sans-serif; font-size: 14px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.2);
                }
                .seletor-tema button { 
                    display: block; width: 100%; margin: 5px 0; padding: 8px;
                    background: #fff; border: 1px solid #ccc; border-radius: 4px;
                    text-align: left; font-weight: bold;
                }
                .pino-restaurante {
                    background-color: #007bff; color: white; font-weight: bold; font-size: 12px;
                    border-radius: 50%; width: 32px; height: 32px; display: flex;
                    align-items: center; justify-content: center; border: 2px solid white;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.4);
                }
                .restaurante-card { text-align: center; min-width: 140px; font-family: sans-serif; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <div class="seletor-tema" id="seletor">
                <strong>Temas</strong>
            </div>
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

                        const temas = {
                            "Positron": positron,
                            "Dark": L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', { attribution: '&copy; CARTO' }),
                            "OSM": L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '&copy; OSM' }),
                            "Satélite": L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { attribution: '&copy; Esri' })
                        };
                        
                        let currentLayer = positron;
                        
                        const seletor = document.getElementById('seletor');
                        Object.keys(temas).forEach(name => {
                            const btn = document.createElement('button');
                            btn.innerText = name;
                            btn.onclick = () => {
                                map.removeLayer(currentLayer);
                                currentLayer = temas[name].addTo(map);
                            };
                            seletor.appendChild(btn);
                        });

                        restaurants.forEach(rest => {
                            const icon = L.divIcon({
                                className: '',
                                html: '<div class="pino-restaurante">' + rest.iniciais + '</div>',
                                iconSize: [32, 32], iconAnchor: [16, 16]
                            });
                            
                            const popup = '<div class="restaurante-card">' +
                                '<h3>' + rest.nome + '</h3>' +
                                '<p><b>' + rest.endereco + '</b></p>' +
                                '<p>' + rest.descricao + '</p>' +
                                '</div>';
                            
                            L.marker([parseFloat(rest.latitude), parseFloat(rest.longitude)], { icon: icon }).bindPopup(popup).addTo(map);
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
                loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.requestLayout()
        },
        modifier = Modifier.fillMaxSize()
    )
}
