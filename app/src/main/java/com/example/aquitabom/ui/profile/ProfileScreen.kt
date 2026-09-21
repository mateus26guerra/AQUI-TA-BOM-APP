package com.example.aquitabom.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.ui.CommonTopBar
import com.example.aquitabom.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onRestaurantClick: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var postMenuPost by remember { mutableStateOf<Post?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CommonTopBar()

        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = { viewModel.refreshProfile() },
            modifier = Modifier.weight(1f)
        ) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ProfileUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ProfileHeader(uiState.userName, uiState.posts.size, themeViewModel.avatarColor)
                        
                        if (uiState.posts.isEmpty()) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Você ainda não tem postagens", 
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(1.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                items(uiState.posts) { post ->
                                    AsyncImage(
                                        model = post.imagemUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clickable { selectedPost = post },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
                is ProfileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    selectedPost?.let { post ->
        PostDetailDialog(
            post = post,
            onDismiss = { selectedPost = null },
            onRestaurantClick = {
                selectedPost = null
                onRestaurantClick(it)
            },
            onMoreClick = {
                selectedPost = null
                postMenuPost = post
            }
        )
    }

    postMenuPost?.takeIf { it.id != null }?.let { post ->
        PostActionsSheet(
            onDismiss = { postMenuPost = null },
            onDelete = {
                postMenuPost = null
                deleteError = null
                viewModel.deletePost(post.id!!) { result ->
                    result.onSuccess {
                        deleteError = null
                    }.onFailure {
                        deleteError = it.message ?: "Não foi possível excluir a postagem"
                    }
                }
            }
        )
    }

    deleteError?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            deleteError = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostActionsSheet(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Ações da postagem",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            TextButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Excluir postagem",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Excluir postagem?") },
            text = { Text("Tem certeza de que deseja excluir esta postagem? Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Sim", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Não")
                }
            }
        )
    }
}

@Composable
fun ProfileHeader(userName: String, postCount: Int, avatarColor: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(avatarColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "U",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = userName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$postCount postagens",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
    }
}

@Composable
fun PostDetailDialog(
    post: Post,
    onDismiss: () -> Unit,
    onRestaurantClick: ((String) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    themeViewModel: ThemeViewModel = viewModel()
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // Header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "Publicação",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                // User Info
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val userName = post.nomeUsuario ?: "Usuário"
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(themeViewModel.avatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "U", fontSize = 12.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = post.nomeRestaurante ?: "Restaurante", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (onMoreClick != null) {
                        IconButton(onClick = onMoreClick) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Ações da postagem",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Image
                AsyncImage(
                    model = post.imagemUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )

                // Content
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = post.titulo ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        if (post.status != null) {
                            Surface(
                                color = when(post.status.uppercase()) {
                                    "EMBACADO" -> Color(0xFFE67E22)
                                    "CHEIO_QUE_SO" -> Color.Red
                                    else -> Color(0xFF2ecc71)
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = post.status.replace("_", " "),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = post.descricao ?: "", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = post.nomeRestaurante ?: "Restaurante",
                                fontWeight = FontWeight.Bold,
                                modifier = if (onRestaurantClick != null && post.nomeRestaurante != null) {
                                    Modifier.clickable { onRestaurantClick(post.nomeRestaurante) }
                                } else {
                                    Modifier
                                },
                                color = if (onRestaurantClick != null && post.nomeRestaurante != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                    
                    if (post.dataCriacao != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = post.dataCriacao,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                }
            }
        }
    }
}
