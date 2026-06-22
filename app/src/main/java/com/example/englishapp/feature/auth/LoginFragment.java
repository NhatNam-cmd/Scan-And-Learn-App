package com.example.englishapp.feature.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() == null && result.getResultCode() != Activity.RESULT_OK) {
                showError("Dang nhap Google da bi huy.");
                return;
            }
            handleGoogleResult(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        googleSignInClient = buildGoogleSignInClient();

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            if (googleSignInClient == null) {
                showError("Thieu default_web_client_id. Hay them SHA tren Firebase roi tai lai google-services.json.");
                return;
            }
            signInLauncher.launch(googleSignInClient.getSignInIntent());
        });
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        int clientIdResId = getResources().getIdentifier(
                "default_web_client_id", "string", requireContext().getPackageName());
        if (clientIdResId == 0) {
            showError("Thieu default_web_client_id. Hay them SHA tren Firebase roi tai lai google-services.json.");
            return null;
        }
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(clientIdResId))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(requireContext(), options);
    }

    private void handleGoogleResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                showError("Khong lay duoc Google ID token.");
                return;
            }
            viewModel.signInWithGoogleToken(account.getIdToken());
        } catch (ApiException exception) {
            showError(buildGoogleSignInError(exception));
        }
    }

    private String buildGoogleSignInError(ApiException exception) {
        int statusCode = exception.getStatusCode();
        if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
            return "Dang nhap Google da bi huy.";
        }
        return "Dang nhap Google that bai: "
                + GoogleSignInStatusCodes.getStatusCodeString(statusCode)
                + " (" + statusCode + ")";
    }

    private void render(AuthUiState state) {
        if (binding == null) {
            return;
        }
        binding.progressLogin.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        binding.btnGoogleSignIn.setEnabled(!state.isLoading());
        if (state.getErrorMessage() == null || state.getErrorMessage().isEmpty()) {
            binding.tvLoginError.setVisibility(View.GONE);
        } else {
            showError(state.getErrorMessage());
        }
        if (state.isSignedIn()) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_login, true)
                    .build();
            NavHostFragment.findNavController(this).navigate(R.id.nav_dashboard, null, navOptions);
        }
    }

    private void showError(String message) {
        if (binding == null) {
            return;
        }
        binding.tvLoginError.setText(message);
        binding.tvLoginError.setVisibility(View.VISIBLE);
    }
}
