package com.example.aquitabom.data.remote

import com.example.aquitabom.data.local.AuthEventBus
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://aqui-ta-bom-api.onrender.com/"

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val authorization = request.header("Authorization")
        val authenticatedRequest = if (!authorization.isNullOrBlank() &&
            !authorization.startsWith("Bearer ", ignoreCase = true)
        ) {
            request.newBuilder()
                .header("Authorization", "Bearer $authorization")
                .build()
        } else {
            request
        }

        val response = chain.proceed(authenticatedRequest)
        if (response.code == 401) {
            runBlocking {
                AuthEventBus.emitUnauthorized()
            }
        }
        response
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authService: AuthService = retrofit.create(AuthService::class.java)
    val restaurantService: RestaurantService = retrofit.create(RestaurantService::class.java)
    val postService: PostService = retrofit.create(PostService::class.java)
    val commentService: CommentService = retrofit.create(CommentService::class.java)
}
