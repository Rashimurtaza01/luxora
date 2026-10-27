package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.database.LuxoraDatabase
import com.example.data.repository.LuxoraRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.LuxoraTheme
import com.example.ui.viewmodel.LuxoraViewModel
import com.example.ui.viewmodel.LuxoraViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: LuxoraViewModel by viewModels {
        val database = LuxoraDatabase.getDatabase(applicationContext)
        val repository = LuxoraRepository(database)
        LuxoraViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuxoraTheme {
                MainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
