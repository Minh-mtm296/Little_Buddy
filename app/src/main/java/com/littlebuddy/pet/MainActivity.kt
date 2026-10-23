package com.littlebuddy.pet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.littlebuddy.pet.data.PetPreferences
import com.littlebuddy.pet.data.PetRepository
import com.littlebuddy.pet.ui.screens.PetHomeScreen
import com.littlebuddy.pet.ui.theme.LittleBuddyTheme
import com.littlebuddy.pet.ui.viewmodel.PetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo dependencies đơn giản và trực quan
        val preferences = PetPreferences(applicationContext)
        val repository = PetRepository(preferences)
        val viewModel = PetViewModel(repository)

        setContent {
            LittleBuddyTheme {
                PetHomeScreen(viewModel = viewModel)
            }
        }
    }
}
