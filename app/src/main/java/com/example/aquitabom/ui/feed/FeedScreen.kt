package com.example.aquitabom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aquitabom.ui.CommonTopBar
import com.example.aquitabom.data.model.Post

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
        CommonTopBar()
        FeedTabs()

        when (uiState) {
            is FeedUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is FeedUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.posts) { post ->
                        PostItem(post)
                    }
                }
            }
            is FeedUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.message)
                }
            }
        }
    }
}

@Composable
fun FeedTabs() {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = Color(0xFFE67E22),
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFFE67E22)
                )
            }
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Para você", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Seguindo", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
        )
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mock user profile pic since API doesn't provide it yet
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = post.nomeUsuario.take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = post.nomeUsuario, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFE67E22),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = post.nomeRestaurante,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                }
            }

            // Image
            Box {
                AsyncImage(
                    model = post.imagemUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFE67E22))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = post.nomeRestaurante, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Title & Description
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = post.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = post.descricao, fontSize = 14.sp)
                
                if (post.dataCriacao != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = post.dataCriacao, fontSize = 10.sp, color = Color.Gray)
                }
            }

            // Actions (Mocked counts for now as API doesn't have likes/comments yet)
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(icon = Icons.Outlined.Favorite, text = "0", tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                ActionButton(icon = Icons.Outlined.ChatBubbleOutline, text = "0")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Outlined.Send, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun StatusTag(text: String, color: Color, textColor: Color, icon: ImageVector? = null) {
    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = textColor)
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(textColor))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = text, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String, tint: Color = Color.Gray) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
