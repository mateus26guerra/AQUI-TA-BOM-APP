package com.example.aquitabom.ui.post

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.local.SessionManager
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.data.repository.PostRepository
import com.example.aquitabom.data.repository.PostRepositoryImpl
import com.example.aquitabom.data.repository.RestaurantRepository
import com.example.aquitabom.data.repository.RestaurantRepositoryImpl
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class CreatePostUiState {
    object Idle : CreatePostUiState()
    object Loading : CreatePostUiState()
    object Success : CreatePostUiState()
    data class Error(val message: String) : CreatePostUiState()
}

class CreatePostViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository: PostRepository = PostRepositoryImpl()
    private val restaurantRepository: RestaurantRepository = RestaurantRepositoryImpl()
    private val sessionManager = SessionManager(application)

    var titulo by mutableStateOf("")
    var descricao by mutableStateOf("")
    var nota by mutableIntStateOf(5)
    var status by mutableStateOf<String?>(null)
    var selectedRestaurant by mutableStateOf<Restaurant?>(null)
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var restaurants by mutableStateOf<List<Restaurant>>(emptyList())
    var uiState by mutableStateOf<CreatePostUiState>(CreatePostUiState.Idle)
        private set

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        val token = sessionManager.fetchAuthToken() ?: return
        viewModelScope.launch {
            restaurantRepository.getNearbyRestaurants(token).onSuccess {
                restaurants = it
            }
        }
    }

    fun onCreatePostClick() {
        val token = sessionManager.fetchAuthToken()
        val uri = selectedImageUri
        val rest = selectedRestaurant

        if (token == null) {
            uiState = CreatePostUiState.Error("Usuário não autenticado")
            return
        }

        val restaurantId = rest?.id

        if (uri == null || restaurantId == null || titulo.isBlank()) {
            uiState = CreatePostUiState.Error("Preencha todos os campos e selecione uma imagem")
            return
        }

        viewModelScope.launch {
            uiState = CreatePostUiState.Loading
            try {
                val file = getFileFromUri(uri)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("imagem", file.name, requestFile)
                
                val result = postRepository.createPost(
                    token = token,
                    titulo = titulo,
                    descricao = if (descricao.isBlank()) null else descricao,
                    restauranteId = restaurantId,
                    nota = nota,
                    status = status,
                    imagem = imagePart
                )
                
                uiState = result.fold(
                    onSuccess = { CreatePostUiState.Success },
                    onFailure = { CreatePostUiState.Error(it.message ?: "Erro desconhecido") }
                )
            } catch (e: Exception) {
                uiState = CreatePostUiState.Error(e.message ?: "Erro ao processar imagem")
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val context = getApplication<Application>().applicationContext
        val inputStream = requireNotNull(context.contentResolver.openInputStream(uri))
        val bitmap = requireNotNull(inputStream.use { BitmapFactory.decodeStream(it) })
        val orientationStream = requireNotNull(context.contentResolver.openInputStream(uri))
        val orientation = orientationStream.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
        val correctedBitmap = applyExifOrientation(bitmap, orientation)
        
        val file = File(context.cacheDir, "temp_image.jpg")
        FileOutputStream(file).use { outputStream ->
            correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        }
        if (correctedBitmap !== bitmap) {
            correctedBitmap.recycle()
        }
        bitmap.recycle()
        return file
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
