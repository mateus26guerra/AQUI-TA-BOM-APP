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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aquitabom.R
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
                AsyncImage(
                    model = post.userProfilePic,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = post.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFE67E22),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "${post.restaurantName} • ${post.location}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(text = "⭐️ ${post.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                }
            }

            // Image
            Box {
                AsyncImage(
                    model = post.postImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = post.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

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
                        Text(text = post.restaurantName, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tags
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusTag(text = post.status, color = Color.Red.copy(alpha = 0.1f), textColor = Color.Red)
                StatusTag(text = post.timeAgo, color = Color.Gray.copy(alpha = 0.1f), textColor = Color.Gray, icon = Icons.Outlined.AccessTime)
                post.waitTime?.let {
                    StatusTag(text = "ESPERA ~$it", color = Color.Red.copy(alpha = 0.1f), textColor = Color.Red, icon = Icons.Outlined.HourglassEmpty)
                }
            }

            // Description
            Text(
                text = post.description,
                modifier = Modifier.padding(horizontal = 12.dp),
                fontSize = 14.sp
            )
            Text(
                text = "... mais",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = Color.Gray
            )

            // Actions
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(icon = Icons.Outlined.Favorite, text = post.likes.toString(), tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                ActionButton(icon = Icons.Outlined.ChatBubbleOutline, text = post.comments.toString())
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
