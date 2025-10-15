package com.rimaro.musify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.edit
import androidx.navigation.NavOptions
import com.rimaro.musify.databinding.ActivityMainBinding
import com.rimaro.musify.ui.fragments.miniplayer.MiniplayerFragment
import com.rimaro.musify.utils.CallListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var callListener: CallListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.searchFragment,
                R.id.libraryFragment,
                R.id.authFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.playerFragment) {
                binding.toolbar.visibility = View.GONE
                binding.navView.visibility = View.GONE
            } else {
                binding.toolbar.visibility = View.VISIBLE
                binding.navView.visibility = View.VISIBLE
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val miniplaye = findViewById<View>(R.id.miniPlayerContainer)
            if (destination.id == R.id.playerFragment) {
                miniplaye.visibility = View.GONE
            } else {
                miniplaye.visibility = View.VISIBLE
            }
        }

        val navView = binding.navView
        navView.setOnItemSelectedListener { item ->
            val destinationId = when (item.itemId) {
                R.id.homeFragment -> R.id.homeFragment
                R.id.searchFragment -> R.id.searchFragment
                R.id.libraryFragment -> R.id.libraryFragment
                else -> null
            }

            destinationId?.let { dest ->
                val currentDest = navController.currentDestination?.id
                if(currentDest != dest) {
                    navController.navigate(dest)
                }
            }
            true
        }

        callListener = CallListener(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 101)
        } else {
            callListener.startListening()
        }

        // loading miniplayer
        supportFragmentManager.beginTransaction()
            .replace(R.id.miniPlayerContainer, MiniplayerFragment())
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if(grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            callListener.startListening()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
                val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                prefs.edit { remove("auth_code") }
                //TODO: move the appbar to home fragment only
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.authFragment, true)
                    .setLaunchSingleTop(true)
                    .build()
                findNavController(R.id.nav_host_fragment_content_main)
                    .navigate(R.id.authFragment, null, options)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        callListener.stopListening()
    }
}