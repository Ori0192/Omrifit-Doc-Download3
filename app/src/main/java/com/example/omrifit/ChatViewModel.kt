package com.example.omrifit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omrifit.ChatData
import com.example.omrifit.ChatResponseCallback
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * ViewModel for handling chat-related operations, including converting between Base64 and Bitmap,
 * and fetching chat responses from ChatData.
 */
class ChatViewModel : ViewModel() {

    /**
     * Converts a Base64 encoded string to a Bitmap.
     *
     * @param base64Str The Base64 encoded string.
     * @return The decoded Bitmap.
     */
    fun base64ToBitmap(base64Str: String?): Bitmap {
        val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * Converts a Bitmap to a Base64 encoded string.
     *
     * @param bitmap The Bitmap to encode.
     * @return The Base64 encoded string.
     */
    fun bitmapToBase64(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Fetches a chat response from ChatData based on the given prompt.
     *
     * @param prompt The text prompt to generate a response for.
     * @param callback The callback to handle success or error.
     */
    fun getResponse(prompt: String, callback: ChatResponseCallback) {
        viewModelScope.launch {
            try {
                val chat = ChatData.getResponse(prompt)
                callback.onSuccess(chat)
            } catch (e: Exception) {
                callback.onError(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Fetches a chat response from ChatData based on the given prompt and image.
     *
     * @param prompt The text prompt to generate a response for.
     * @param bitmap The image to include in the prompt.
     * @param callback The callback to handle success or error.
     */
    fun getResponseWithImage(prompt: String, bitmap: Bitmap, callback: ChatResponseCallback) {
        viewModelScope.launch {
            try {
                val chat = ChatData.getResponseWithImage(prompt, bitmap)
                callback.onSuccess(chat)
            } catch (e: Exception) {
                callback.onError(e.message ?: "Unknown error")
            }
        }
    }
}
