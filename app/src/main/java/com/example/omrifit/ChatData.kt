package com.example.omrifit

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatData {


    suspend fun getResponse(prompt: String): Chat {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro", apiKey = BuildConfig.apiKey
        )

        try {
            val editedPrompt = "answer this like you were a " +
                    " personal trainer named omri" +
                    " with light sense of humor" +
                    "chating in whatsapp(don't make it too long): $prompt"

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(editedPrompt)
            }

            return Chat(
                prompt = response.text ?: "error",
                bitmap = null,
                isFromUser = false
            )

        } catch (e: Exception) {
            return Chat(
                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }

    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision", apiKey = BuildConfig.apiKey
        )

        try {
            val editedPrompt = "answer this like you were a " +
                    " personal trainer named omri" +
                    " with light sense of humor" +
                    "chating in whatsapp(don't make it too long): $prompt"
            val inputContent = content {
                image(bitmap)
                text(editedPrompt)
            }

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(inputContent)
            }

            return Chat(
                prompt = response.text ?: "error",
                bitmap = null,
                isFromUser = false
            )

        } catch (e: Exception) {
            return Chat(
                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }

    }

}



















