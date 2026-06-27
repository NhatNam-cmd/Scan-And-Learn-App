package com.example.englishapp.feature.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.databinding.FragmentProfileBinding;
import com.example.englishapp.feature.auth.AuthUiState;
import com.example.englishapp.feature.auth.AuthViewModel;
import com.example.englishapp.feature.sync.SyncViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    private SyncViewModel syncViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        syncViewModel = new ViewModelProvider(this).get(SyncViewModel.class);

        // Setup listeners
        binding.btnSettings.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_settings));
        binding.btnLogout.setOnClickListener(v -> logout());

        // Observe auth state
        authViewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
        profileViewModel.getStats().observe(getViewLifecycleOwner(), this::renderStats);

        // Observe sync state
        syncViewModel.isSyncing().observe(getViewLifecycleOwner(), syncing -> {
            if (binding != null) {
                binding.progressSync.setVisibility(syncing ? View.VISIBLE : View.GONE);
                binding.btnSync.setEnabled(!syncing);
                binding.btnSync.setText(syncing ? "Đang đồng bộ..." : "☁️ Đồng bộ dữ liệu");
            }
        });

        syncViewModel.getSyncStatus().observe(getViewLifecycleOwner(), status -> {
            if (binding != null) {
                binding.tvSyncStatusDetail.setText(status);
            }
        });

        // Setup sync button
        binding.btnSync.setOnClickListener(v -> syncViewModel.syncData(requireContext()));

        // Load data
        authViewModel.refreshUser();
        profileViewModel.loadStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void render(AuthUiState state) {
        if (binding == null) {
            return;
        }
        if (!state.isSignedIn()) {
            navigateToLogin();
            return;
        }
        binding.tvProfileName.setText(nonEmpty(state.getDisplayName(), "Người học"));
        binding.tvProfileEmail.setText(nonEmpty(state.getEmail(), ""));
        binding.tvSyncStatus.setText("Đồng bộ Firebase: đã đăng nhập bằng Google");
        loadAvatar(state.getPhotoUrl());
    }

    private void logout() {
        GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        authViewModel.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.nav_login, null, navOptions);
    }

    private void loadAvatar(@Nullable Uri photoUrl) {
        if (photoUrl == null) {
            binding.ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            binding.ivAvatar.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_primary));
            return;
        }
        new Thread(() -> {
            try (InputStream stream = new URL(photoUrl.toString()).openStream()) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                requireActivity().runOnUiThread(() -> {
                    if (binding != null && bitmap != null) {
                        binding.ivAvatar.setImageTintList(null);
                        binding.ivAvatar.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    private String nonEmpty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void renderStats(ProfileStats stats) {
        if (binding == null || stats == null) {
            return;
        }
        binding.tvTotalWords.setText(String.valueOf(stats.getTotalWords()));
        binding.tvStreakDays.setText(String.valueOf(stats.getStreakDays()));
        binding.tvCompletedQuizzes.setText(String.valueOf(stats.getCompletedQuizzes()));
        binding.tvMasteredWords.setText(String.format(Locale.getDefault(),
                "Đã thuộc: %d/%d từ", stats.getMasteredWords(), stats.getTotalWords()));

        String time = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
                .format(new Date(stats.getLastSyncedAt()));
        if (authViewModel != null && authViewModel.isSignedIn()) {
            binding.tvSyncStatus.setText("Đồng bộ Firebase: sẵn sàng • Cập nhật " + time);
        } else {
            binding.tvSyncStatus.setText("Đồng bộ Firebase: chưa đăng nhập");
        }
    }
}