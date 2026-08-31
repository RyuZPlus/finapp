package com.example.finconapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.finconapp.ui.navigation.AppNavigation
import com.example.finconapp.ui.theme.FinconappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinconappTheme {

                val navController = rememberNavController()

                AppNavigation(
                    navController = navController
                )
            }
        }
    }
}
