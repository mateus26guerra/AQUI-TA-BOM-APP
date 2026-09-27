package com.example.aquitabom.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.StarBorder
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
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

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
    themeViewModel: ThemeViewModel = viewModel(),
    commentsViewModel: CommentsViewModel = viewModel(),
    openComments: Boolean = false
) {
    var commentText by remember { mutableStateOf("") }
    val postId = post.id
    val scrollState = rememberScrollState()

    LaunchedEffect(postId) {
        postId?.let { commentsViewModel.loadComments(it) }
    }
    LaunchedEffect(openComments, commentsViewModel.comments.size) {
        if (openComments) scrollState.animateScrollTo(scrollState.maxValue)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
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
                        Text(
                            text = post.titulo ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            val rating = (post.nota ?: 0).coerceIn(0, 5)
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (index == 0) "Nota $rating de 5" else null,
                                    tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
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
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = "Data da publicação",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatPostDate(post.dataCriacao),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Hora da publicação",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatPostTime(post.dataCriacao),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comentários (${
                                if (commentsViewModel.isLoading) {
                                    post.quantidadeComentarios ?: 0
                                } else {
                                    commentsViewModel.comments.size
                                }
                            })",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (commentsViewModel.isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else if (commentsViewModel.comments.isEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ainda não há comentários. Seja o primeiro!",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        commentsViewModel.comments.forEach { comment ->
                            CommentRow(
                                comment = comment,
                                onDelete = { comment.id?.let(commentsViewModel::deleteComment) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    commentsViewModel.errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escreva um comentário...") },
                        maxLines = 3,
                        trailingIcon = {
                            IconButton(
                                enabled = commentText.isNotBlank() && !commentsViewModel.isSending && postId != null,
                                onClick = {
                                    postId?.let {
                                        commentsViewModel.addComment(it, commentText)
                                        commentText = ""
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Send,
                                    contentDescription = "Enviar comentário",
                                    tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    )
                }
            }

        }
    }
}

@Composable
private fun CommentRow(
    comment: com.example.aquitabom.data.model.Comment,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.nomeUsuario?.firstOrNull()?.uppercase() ?: "U",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(comment.nomeUsuario ?: "Usuário", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(comment.texto, fontSize = 14.sp)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Excluir comentário",
                tint = Color.Gray
            )
        }
    }
}

private fun parsePostDate(value: String): LocalDateTime? {
    return try {
        OffsetDateTime.parse(value).toLocalDateTime()
    } catch (_: DateTimeParseException) {
        try {
            LocalDateTime.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

private fun formatPostDate(value: String): String {
    val date = parsePostDate(value) ?: return value
    return date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")))
}

private fun formatPostTime(value: String): String {
    val date = parsePostDate(value) ?: return "--:--"
    return date.format(DateTimeFormatter.ofPattern("HH:mm", Locale("pt", "BR")))
}
