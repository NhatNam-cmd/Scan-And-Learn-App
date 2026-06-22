package com.example.englishapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = getSharedPreferences("scan_learn_settings", MODE_PRIVATE)
                .getBoolean("dark_theme", false);
        AppCompatDelegate.setDefaultNightMode(darkTheme
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();
            boolean hideBottomNav = destinationId == R.id.nav_splash
                    || destinationId == R.id.nav_login
                    || destinationId == R.id.nav_settings;
            bottomNav.setVisibility(hideBottomNav ? View.GONE : View.VISIBLE);
            if (destinationId == R.id.nav_story_word_selection
                    || destinationId == R.id.nav_story_session
                    || destinationId == R.id.nav_story_result) {
                bottomNav.getMenu().findItem(R.id.nav_story).setChecked(true);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
