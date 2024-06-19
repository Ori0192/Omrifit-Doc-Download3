package com.example.omrifit

import android.app.Application
import com.google.firebase.FirebaseApp

class Omrifit : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
    companion object {
        var omrifit: Omrifit? = null
            private set
        private val TAG = Omrifit::class.java.simpleName
    }
}
