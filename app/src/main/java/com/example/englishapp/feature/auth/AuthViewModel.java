package com.example.englishapp.feature.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.firebase.service.AuthService;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final AuthService authService;
    private final MutableLiveData<AuthUiState> uiState = new MutableLiveData<>();

    @Inject
    public AuthViewModel(AuthService authService) {
        this.authService = authService;
        refreshUser();
    }

    public LiveData<AuthUiState> getUiState() {
        return uiState;
    }

    public boolean isSignedIn() {
        return authService.isSignedIn();
    }

    public void refreshUser() {
        FirebaseUser user = authService.getCurrentUser();
        uiState.setValue(buildState(user, false, null));
    }

    public void signInWithGoogleToken(String idToken) {
        uiState.setValue(new AuthUiState(false, true, null, null, null, null));
        authService.signInWithGoogleToken(idToken)
                .addOnSuccessListener(authResult ->
                        uiState.setValue(buildState(authResult.getUser(), false, null)))
                .addOnFailureListener(exception ->
                        uiState.setValue(new AuthUiState(false, false, null, null, null,
                                exception.getLocalizedMessage())));
    }

    public void signOut() {
        authService.signOut();
        uiState.setValue(buildState(null, false, null));
    }

    private AuthUiState buildState(FirebaseUser user, boolean loading, String errorMessage) {
        if (user == null) {
            return new AuthUiState(false, loading, null, null, null, errorMessage);
        }
        return new AuthUiState(true, loading, user.getDisplayName(), user.getEmail(),
                user.getPhotoUrl(), errorMessage);
    }
}
