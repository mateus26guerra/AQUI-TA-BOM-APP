package com.example.aquitabom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.aquitabom.ui.profile.PostDetailDialog
import com.example.aquitabom.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onRestaurantClick: (String) -> Unit = {},
    viewModel: FeedViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CommonTopBar()
        FeedTabs()

        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.refreshPosts() },
            modifier = Modifier.weight(1f)
        ) {
            when (uiState) {
                is FeedUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is FeedUiState.Success -> {
                    if (uiState.posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Nenhuma postagem encontrada", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.posts) { post ->
                                PostItem(
                                    post = post, 
                                    onLikeClick = { viewModel.toggleLike(post) },
                                    avatarColor = themeViewModel.avatarColor,
                                    onClick = { selectedPost = post }
                                )
                            }
                        }

                        selectedPost?.let { post ->
                            PostDetailDialog(
                                post = post,
                                onDismiss = { selectedPost = null },
                                onRestaurantClick = {
                                    selectedPost = null
                                    onRestaurantClick(it)
                                }
                            )
                        }
                    }
                }
                is FeedUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.loadPosts() }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
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
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
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
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    avatarColor: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val userName = post.nomeUsuario ?: "Usuário"
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "U", fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = post.nomeRestaurante ?: "Restaurante",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(text = "⭐️ ${post.nota ?: 0}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                
                if (post.status != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statusColor = when(post.status.uppercase()) {
                                "EMBACADO" -> Color(0xFFE67E22)
                                "CHEIO_QUE_SO" -> Color.Red
                                else -> Color(0xFF2ecc71)
                            }
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = post.status.replace("_", " "), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = post.nomeRestaurante ?: "Restaurante", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Title & Description
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = post.titulo ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = post.descricao ?: "", fontSize = 14.sp)
                
                if (post.dataCriacao != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = post.dataCriacao, fontSize = 10.sp, color = Color.Gray)
                }
            }

            // Actions
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isLiked = post.curtidoPeloUsuario ?: false
                val likesCount = post.likes ?: 0
                
                ActionButton(
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    text = likesCount.toString(),
                    tint = if (isLiked) Color.Red else Color.Gray,
                    onClick = onLikeClick
                )
                
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
fun ActionButton(icon: ImageVector, text: String, tint: Color = Color.Gray, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
