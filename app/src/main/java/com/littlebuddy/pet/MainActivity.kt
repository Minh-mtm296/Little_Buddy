package com.littlebuddy.pet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.littlebuddy.pet.data.PetPreferences
import com.littlebuddy.pet.data.PetRepository
import com.littlebuddy.pet.ui.screens.PetHomeScreen
import com.littlebuddy.pet.ui.theme.LittleBuddyTheme
import com.littlebuddy.pet.ui.viewmodel.PetViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: PetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo ViewModel thông qua ViewModelProvider.Factory để sống sót qua configuration changes (xoay màn hình)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val preferences = PetPreferences(applicationContext)
                val repository = PetRepository(preferences)
                return PetViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[PetViewModel::class.java]

        setContent {
            LittleBuddyTheme {
                PetHomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppPause()
    }
}
