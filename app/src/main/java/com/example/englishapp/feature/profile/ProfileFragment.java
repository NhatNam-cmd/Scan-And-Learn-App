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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.InputStream;
import java.net.URL;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthViewModel viewModel;

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
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        binding.btnSettings.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_settings));
        binding.btnLogout.setOnClickListener(v -> logout());
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
        viewModel.refreshUser();
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
        binding.tvProfileName.setText(nonEmpty(state.getDisplayName(), "Nguoi hoc"));
        binding.tvProfileEmail.setText(nonEmpty(state.getEmail(), ""));
        loadAvatar(state.getPhotoUrl());
    }

    private void logout() {
        GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        viewModel.signOut();
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
}
