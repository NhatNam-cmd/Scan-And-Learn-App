package com.example.englishapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.englishapp.core.datastore.UserPreferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeMode = getSharedPreferences(UserPreferences.PREF_NAME, MODE_PRIVATE)
                .getInt(UserPreferences.KEY_THEME_MODE, UserPreferences.THEME_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(toNightMode(themeMode));
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

    private int toNightMode(int themeMode) {
        if (themeMode == UserPreferences.THEME_LIGHT) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        if (themeMode == UserPreferences.THEME_DARK) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

}
