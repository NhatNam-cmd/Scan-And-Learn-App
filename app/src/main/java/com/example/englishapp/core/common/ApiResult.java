package com.example.englishapp.core.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Sealed-class pattern in Java: represents the result of an API call.
 *
 * @param <T> the type of data on success / fallback
 */
public abstract class ApiResult<T> {

    // Prevent external subclassing
    private ApiResult() {}

    /**
     * Represents a successful API result containing data.
     */
    public static final class Success<T> extends ApiResult<T> {
        private final T data;

        public Success(@NonNull T data) {
            this.data = data;
        }

        @NonNull
        public T getData() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?> success = (Success<?>) o;
            return Objects.equals(data, success.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

        @NonNull
        @Override
        public String toString() {
            return "ApiResult.Success{data=" + data + '}';
        }
    }

    /**
     * Represents an error result with a message and an optional exception.
     */
    public static final class Error extends ApiResult<Void> {
        private final String message;
        @Nullable
        private final Throwable exception;

        public Error(@NonNull String message, @Nullable Throwable exception) {
            this.message = message;
            this.exception = exception;
        }

        public Error(@NonNull String message) {
            this(message, null);
        }

        @NonNull
        public String getMessage() {
            return message;
        }

        @Nullable
        public Throwable getException() {
            return exception;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Error error = (Error) o;
            return Objects.equals(message, error.message)
                    && Objects.equals(exception, error.exception);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message, exception);
        }

        @NonNull
        @Override
        public String toString() {
            return "ApiResult.Error{message='" + message + "', exception=" + exception + '}';
        }
    }

    /**
     * Singleton representing a loading state.
     */
    public static final class Loading extends ApiResult<Void> {
        private static final Loading INSTANCE = new Loading();

        private Loading() {}

        @NonNull
        public static Loading getInstance() {
            return INSTANCE;
        }

        @NonNull
        @Override
        public String toString() {
            return "ApiResult.Loading";
        }
    }

    /**
     * Represents a fallback result (e.g. when AI quota is exceeded or a network error occurs).
     */
    public static final class Fallback<T> extends ApiResult<T> {
        private final T data;
        private final String reason;

        public Fallback(@NonNull T data, @NonNull String reason) {
            this.data = data;
            this.reason = reason;
        }

        public Fallback(@NonNull T data) {
            this(data, "AI Quota Exceeded or Network Error");
        }

        @NonNull
        public T getData() {
            return data;
        }

        @NonNull
        public String getReason() {
            return reason;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Fallback<?> fallback = (Fallback<?>) o;
            return Objects.equals(data, fallback.data)
                    && Objects.equals(reason, fallback.reason);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data, reason);
        }

        @NonNull
        @Override
        public String toString() {
            return "ApiResult.Fallback{data=" + data + ", reason='" + reason + "'}";
        }
    }
}
