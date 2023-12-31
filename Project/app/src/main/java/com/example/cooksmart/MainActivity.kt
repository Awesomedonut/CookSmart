package com.example.cooksmart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cooksmart.databinding.ActivityMainBinding
import com.example.cooksmart.infra.services.NotificationWorker
import com.example.cooksmart.utils.PermissionCheck
import java.util.concurrent.TimeUnit
import com.example.cooksmart.ui.ingredient.IngredientFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_ingredient, R.id.navigation_recipe, R.id.navigation_saved_recipes, R.id.navigation_calendar
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId)
            true
        }

        navView.setOnItemReselectedListener { item ->
            // Handle navigation item reselection
            when (item.itemId) {
                R.id.navigation_ingredient -> {
                    navController.popBackStack(R.id.navigation_ingredient, false)
                }
            }
        }
        // Check and request microphone permission in IngredientFragment
        PermissionCheck.checkPermissions(this)

    }

    /**
     * onSupportNavigateUp
     * Description: Makes the back button navigate to the previously navigated fragment
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}