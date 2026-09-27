package com.example.aquitabom.ui.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.ui.profile.PostDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onBack: () -> Unit,
    viewModel: RestaurantDetailViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var selectedStatus by remember { mutableStateOf(restaurant.statusLotacao) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var reviewNota by remember { mutableIntStateOf(5) }
    var reviewComentario by remember { mutableStateOf("") }
    val currentStatus = viewModel.lotacaoAvaliacao?.status ?: restaurant.statusLotacao

    LaunchedEffect(restaurant.id) {
        restaurant.id?.let {
            viewModel.loadRestaurantPosts(it)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = restaurant.nome, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = {
                restaurant.id?.let { viewModel.refreshRestaurant(it) }
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Restaurant Header
            RestaurantHeader(
                restaurant = restaurant,
                currentStatus = currentStatus,
                selectedStatus = selectedStatus,
                lotacaoAvaliacao = viewModel.lotacaoAvaliacao,
                isSending = viewModel.isSendingLotacao,
                errorMessage = viewModel.lotacaoError,
                onStatusSelected = { status ->
                    selectedStatus = status
                    restaurant.id?.let { id ->
                        viewModel.avaliarLotacao(id, status) {
                            viewModel.loadLotacao(id)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            RestaurantTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            if (selectedTab == 0) {
                when (uiState) {
                    is RestaurantDetailUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                is RestaurantDetailUiState.Success -> {
                    if (uiState.posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Nenhuma avaliação para este restaurante ainda.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
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
                    is RestaurantDetailUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            } else {
                RestaurantReviewsContent(
                    reviews = viewModel.restaurantReviews,
                    isLoading = viewModel.isLoadingReviews,
                    isSending = viewModel.isSendingReview,
                    errorMessage = viewModel.reviewError,
                    nota = reviewNota,
                    comentario = reviewComentario,
                    onNotaChange = { reviewNota = it },
                    onComentarioChange = { reviewComentario = it },
                    onSubmit = {
                        restaurant.id?.let { id ->
                            viewModel.saveRestaurantReview(
                                id,
                                reviewNota,
                                reviewComentario.ifBlank { null }
                            ) {
                                reviewComentario = ""
                            }
                        }
                    },
                    onDelete = { reviewId ->
                        restaurant.id?.let { id -> viewModel.deleteRestaurantReview(reviewId, id) }
                    }
                )
            }
        }
    }

    selectedPost?.let { post ->
        PostDetailDialog(post = post, onDismiss = { selectedPost = null })
    }
}
}

@Composable
fun RestaurantHeader(
    restaurant: Restaurant,
    currentStatus: String?,
    selectedStatus: String?,
    lotacaoAvaliacao: com.example.aquitabom.data.model.LotacaoAvaliacao?,
    isSending: Boolean,
    errorMessage: String?,
    onStatusSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (restaurant.urlImagem != null) {
                AsyncImage(
                    model = restaurant.urlImagem,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
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
                    Text(text = restaurant.iniciais, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = restaurant.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Localização",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = restaurant.endereco,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (!restaurant.descricao.isNullOrBlank()) {
                    Text(
                        text = restaurant.descricao.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                currentStatus?.let { status ->
                    Text(
                        text = "Status: ${statusLabel(status)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor(status)
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Como está a lotação agora?",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LotacaoButton(
                text = "De boa",
                count = lotacaoAvaliacao?.deBoa ?: 0,
                status = "DE_BOA",
                textColor = Color(0xFF065F46),
                backgroundColor = Color(0xFFECFDF5),
                enabled = !isSending,
                onClick = onStatusSelected
            )
            LotacaoButton(
                text = "Embaçado",
                count = lotacaoAvaliacao?.embacado ?: 0,
                status = "EMBACADO",
                textColor = Color(0xFF78350F),
                backgroundColor = Color(0xFFFFFBEB),
                enabled = !isSending,
                onClick = onStatusSelected
            )
            LotacaoButton(
                text = "Cheio que só",
                count = lotacaoAvaliacao?.cheioQueSo ?: 0,
                status = "CHEIO_QUE_SO",
                textColor = Color(0xFF881337),
                backgroundColor = Color(0xFFFFF1F2),
                enabled = !isSending,
                onClick = onStatusSelected
            )
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        
    }

}

private fun statusLabel(status: String): String = when (status) {
    "DE_BOA" -> "De boa"
    "EMBACADO" -> "Embaçado"
    "CHEIO_QUE_SO" -> "Cheio que só"
    else -> status
}

private fun statusColor(status: String): Color = when (status) {
    "DE_BOA" -> Color(0xFF2ECC71)
    "EMBACADO" -> Color(0xFFF1C40F)
    "CHEIO_QUE_SO" -> Color(0xFFE74C3C)
    else -> Color.Gray
}

@Composable
private fun RestaurantTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Postagens")
                }
            }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Avaliações")
                }
            }
        )
    }
}

@Composable
private fun RestaurantReviewsContent(
    reviews: List<com.example.aquitabom.data.model.RestaurantReview>,
    isLoading: Boolean,
    isSending: Boolean,
    errorMessage: String?,
    nota: Int,
    comentario: String,
    onNotaChange: (Int) -> Unit,
    onComentarioChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Sua avaliação", fontWeight = FontWeight.Bold)
                Text(
                    "Compartilhe como foi sua experiência neste restaurante",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        IconButton(onClick = { onNotaChange(index + 1) }) {
                            Icon(
                                imageVector = if (index < nota) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Nota ${index + 1}",
                                tint = if (index < nota) Color(0xFFFF9800) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comentario,
                    onValueChange = onComentarioChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Conte como foi sua experiência...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Button(
                    onClick = onSubmit,
                    enabled = !isSending,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF7417),
                        contentColor = Color.White
                    )
                ) {
                    Text(if (isSending) "Enviando..." else "Publicar avaliação")
                }
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }

        Text("Comentários", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        } else if (reviews.isEmpty()) {
            Text("Ainda não há comentários.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            reviews.forEach { review ->
                RestaurantReviewRow(review, onDelete)
            }
        }
    }
}

@Composable
private fun RestaurantReviewRow(
    review: com.example.aquitabom.data.model.RestaurantReview,
    onDelete: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (review.nomeUsuario ?: "U").take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(review.nomeUsuario ?: "Usuário", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.nota) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (index < review.nota) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                review.id?.let { id ->
                    IconButton(onClick = { onDelete(id) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir avaliação",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (!review.comentario.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(review.comentario)
            }
        }
    }
}

@Composable
private fun LotacaoStatusContent(avaliacao: com.example.aquitabom.data.model.LotacaoAvaliacao?) {
    if (avaliacao == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Status atual", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        StatusCountRow("De boa", avaliacao.deBoa, Color(0xFF2ECC71))
        StatusCountRow("Embaçado", avaliacao.embacado, Color(0xFFF1C40F))
        StatusCountRow("Cheio que só", avaliacao.cheioQueSo, Color(0xFFE74C3C))
        Text(
            "Total de avaliações: ${avaliacao.totalAvaliacoes}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun StatusCountRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = color)
        Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
    }
}

@Composable
private fun RowScope.LotacaoButton(
    text: String,
    count: Int,
    status: String,
    textColor: Color,
    backgroundColor: Color,
    enabled: Boolean,
    onClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .clickable(enabled = enabled) { onClick(status) },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        contentColor = textColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(textColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            Text(
                text = "$count ${if (count == 1) "voto" else "votos"}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        }
    }
}
