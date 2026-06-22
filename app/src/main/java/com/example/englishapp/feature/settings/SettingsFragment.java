package com.example.englishapp.feature.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.englishapp.core.datastore.UserPreferences;
import com.example.englishapp.databinding.FragmentSettingsBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {
    @Inject
    UserPreferences userPreferences;

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.switchDarkTheme.setChecked(userPreferences.isDarkTheme());
        binding.sliderSpeechRate.setValue(userPreferences.getSpeechRate());
        updateSpeechRateLabel(userPreferences.getSpeechRate());

        binding.switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userPreferences.setDarkTheme(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });
        binding.sliderSpeechRate.addOnChangeListener((slider, value, fromUser) -> {
            userPreferences.setSpeechRate(value);
            updateSpeechRateLabel(value);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateSpeechRateLabel(float value) {
        binding.tvSpeechRateLabel.setText(String.format("Toc do doc TTS: %.1fx", value));
    }
}
