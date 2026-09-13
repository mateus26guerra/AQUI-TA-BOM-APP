package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.Post

interface PostRepository {
    suspend fun getFeedPosts(): List<Post>
}

class PostRepositoryImpl : PostRepository {
    override suspend fun getFeedPosts(): List<Post> {
        return listOf(
            Post(
                id = 1,
                userName = "THIAGO \"BACON\" ROCHA",
                userProfilePic = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100",
                restaurantName = "SMASH JOINT",
                location = "BAIXO AUGUSTA, SP",
                rating = 4.9,
                postImage = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600",
                status = "CHEIO QUE SÓ",
                description = "O melhor smash duplo de SP sem discussão! Pão brioche, carne suculenta e muito queijo.",
                likes = 234,
                comments = 48,
                timeAgo = "há 25 min",
                waitTime = "40 MIN"
            ),
            Post(
                id = 2,
                userName = "LUCAS MENDES",
                userProfilePic = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100",
                restaurantName = "BOTECO DO NENÊ",
                location = "PINHEIROS, SP",
                rating = 4.7,
                postImage = "https://images.unsplash.com/photo-1626082895617-2c6de3476af7?w=600",
                status = "TRANQUILO",
                description = "Sábado passado conheci essa coxinha sem massa... sensacional!",
                likes = 215,
                comments = 39,
                timeAgo = "há 1h",
                waitTime = null
            )
        )
    }
}
