package com.example

import android.app.Application
import android.util.Log
import com.example.core.AppContainer

class CallfriendsZApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Global crash guard to prevent unexpected force closures
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CallfriendsZ", "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            appContainer = AppContainer.getInstance(this)
        } catch (e: Throwable) {
            Log.e("CallfriendsZ", "Failed to initialize AppContainer: ${e.message}", e)
        }
    }
}
