package com.example.englishapp.feature.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.englishapp.core.datastore.UserPreferences;
import com.example.englishapp.core.reminder.ReminderScheduler;
import com.example.englishapp.databinding.FragmentSettingsBinding;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

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

        // Restore saved values
        binding.sliderSpeechRate.setValue(userPreferences.getSpeechRate());
        binding.sliderSpeechPitch.setValue(userPreferences.getSpeechPitch());
        binding.sliderDailyGoal.setValue(userPreferences.getDailyGoal());
        binding.switchReminder.setChecked(userPreferences.isReminderEnabled());
        binding.switchAutoSaveScan.setChecked(userPreferences.shouldAutoSaveScannedWords());

        updateSpeechRateLabel(userPreferences.getSpeechRate());
        updateSpeechPitchLabel(userPreferences.getSpeechPitch());
        updateDailyGoalLabel(userPreferences.getDailyGoal());
        updateReminderTimeLabel();

        // ── Theme selection via clickable rows ──
        bindThemeMode();

        // ── TTS sliders ──
        binding.sliderSpeechRate.addOnChangeListener((slider, value, fromUser) -> {
            userPreferences.setSpeechRate(value);
            updateSpeechRateLabel(value);
        });

        binding.sliderSpeechPitch.addOnChangeListener((slider, value, fromUser) -> {
            userPreferences.setSpeechPitch(value);
            updateSpeechPitchLabel(value);
        });

        // ── Daily goal slider ──
        binding.sliderDailyGoal.addOnChangeListener((slider, value, fromUser) -> {
            int goal = Math.round(value);
            userPreferences.setDailyGoal(goal);
            updateDailyGoalLabel(goal);
        });

        // ── Reminder switch ──
        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userPreferences.setReminderEnabled(isChecked);
            if (isChecked) {
                requestNotificationPermissionIfNeeded();
                ReminderScheduler.scheduleReminder(
                        requireContext(),
                        userPreferences.getReminderHour(),
                        userPreferences.getReminderMinute()
                );
            } else {
                ReminderScheduler.cancelReminder(requireContext());
            }
        });

        // ── Reminder time row tap ──
        binding.rowReminderTime.setOnClickListener(v -> showReminderTimePicker());

        // ── Auto-save scan switch ──
        binding.switchAutoSaveScan.setOnCheckedChangeListener((buttonView, isChecked) ->
                userPreferences.setAutoSaveScannedWords(isChecked));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ─────────────────────────────────────────────
    //  Theme binding
    // ─────────────────────────────────────────────

    private void bindThemeMode() {
        applyThemeSelection(userPreferences.getThemeMode());

        binding.rowThemeSystem.setOnClickListener(v -> selectTheme(UserPreferences.THEME_SYSTEM));
        binding.rowThemeLight.setOnClickListener(v -> selectTheme(UserPreferences.THEME_LIGHT));
        binding.rowThemeDark.setOnClickListener(v -> selectTheme(UserPreferences.THEME_DARK));
    }

    private void selectTheme(int themeMode) {
        userPreferences.setThemeMode(themeMode);
        applyThemeSelection(themeMode);
        AppCompatDelegate.setDefaultNightMode(toNightMode(themeMode));
    }

    private void applyThemeSelection(int themeMode) {
        binding.rbThemeSystem.setChecked(themeMode == UserPreferences.THEME_SYSTEM);
        binding.rbThemeLight.setChecked(themeMode == UserPreferences.THEME_LIGHT);
        binding.rbThemeDark.setChecked(themeMode == UserPreferences.THEME_DARK);
    }

    private int toNightMode(int themeMode) {
        if (themeMode == UserPreferences.THEME_LIGHT) return AppCompatDelegate.MODE_NIGHT_NO;
        if (themeMode == UserPreferences.THEME_DARK)  return AppCompatDelegate.MODE_NIGHT_YES;
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    // ─────────────────────────────────────────────
    //  Label updaters
    // ─────────────────────────────────────────────

    private void updateSpeechRateLabel(float value) {
        binding.tvSpeechRateLabel.setText(String.format("%.1fx", value));
    }

    private void updateSpeechPitchLabel(float value) {
        binding.tvSpeechPitchLabel.setText(String.format("%.1fx", value));
    }

    private void updateDailyGoalLabel(int value) {
        binding.tvDailyGoalLabel.setText(value + " từ / ngày");
    }

    private void updateReminderTimeLabel() {
        binding.tvReminderTimeValue.setText(String.format("%02d:%02d",
                userPreferences.getReminderHour(), userPreferences.getReminderMinute()));
    }

    // ─────────────────────────────────────────────
    //  Time picker
    // ─────────────────────────────────────────────

    private void showReminderTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(userPreferences.getReminderHour())
                .setMinute(userPreferences.getReminderMinute())
                .setTitleText("Chọn giờ nhắc học")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            userPreferences.setReminderTime(picker.getHour(), picker.getMinute());
            userPreferences.setReminderEnabled(true);
            binding.switchReminder.setChecked(true);
            updateReminderTimeLabel();
            requestNotificationPermissionIfNeeded();
            ReminderScheduler.scheduleReminder(
                    requireContext(),
                    picker.getHour(),
                    picker.getMinute()
            );
        });

        picker.show(getParentFragmentManager(), "reminder_time_picker");
    }

    // ─────────────────────────────────────────────
    //  Notification permission
    // ─────────────────────────────────────────────

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }
}