package com.example.englishapp.feature.auth.presentation;

import android.net.Uri;

public class AuthUiState {
    private final boolean signedIn;
    private final boolean loading;
    private final String displayName;
    private final String email;
    private final Uri photoUrl;
    private final String errorMessage;

    public AuthUiState(boolean signedIn, boolean loading, String displayName, String email,
            Uri photoUrl, String errorMessage) {
        this.signedIn = signedIn;
        this.loading = loading;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.errorMessage = errorMessage;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public boolean isLoading() {
        return loading;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
