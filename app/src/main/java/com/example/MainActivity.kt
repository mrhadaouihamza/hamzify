package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.data.local.AppDatabase
import com.example.data.repository.HabitRepository
import com.example.ui.MainAppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = HabitRepository(database.habitDao())

        setContent {
            MainAppScreen(repository = repository)
        }
    }
}
