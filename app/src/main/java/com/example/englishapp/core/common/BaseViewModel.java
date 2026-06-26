package com.example.englishapp.core.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Abstract base ViewModel following the MVI (Model-View-Intent) pattern.
 * <p>
 * - Uses {@link MutableLiveData} for UI state (exposed as {@link LiveData}).
 * - Uses {@link SingleLiveEvent} for one-time UI effects (navigation, toasts, etc.).
 * - Subclasses must implement {@link #onEvent(Object)} to handle user actions.
 *
 * @param <S> the UI state type (must implement {@link UiState})
 * @param <E> the UI event type (must implement {@link UiEvent})
 * @param <F> the UI effect type (must implement {@link UiEffect})
 */
public abstract class BaseViewModel<S extends UiState, E extends UiEvent, F extends UiEffect>
        extends ViewModel {

    private final MutableLiveData<S> _uiState;
    private final SingleLiveEvent<F> _uiEffect = new SingleLiveEvent<>();

    /**
     * Publicly observable UI state.
     */
    public LiveData<S> getUiState() {
        return _uiState;
    }

    /**
     * Publicly observable one-time UI effects.
     */
    public SingleLiveEvent<F> getUiEffect() {
        return _uiEffect;
    }

    /**
     * @param initialState the initial UI state
     */
    protected BaseViewModel(@NonNull S initialState) {
        _uiState = new MutableLiveData<>(initialState);
    }

    /**
     * Handle an event from the UI layer.
     *
     * @param event the event to process
     */
    public abstract void onEvent(@NonNull E event);

    /**
     * Update the UI state by applying a reducer function.
     * The reducer receives the current state and must return the new state.
     *
     * @param reducer function that takes the current state and returns the new state
     */
    protected void setState(@NonNull Function<S, S> reducer) {
        S currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(reducer.apply(currentState));
        }
    }

    /**
     * Emit a one-time UI effect (e.g., navigation, toast message).
     *
     * @param builder supplier that creates the effect
     */
    protected void setEffect(@NonNull Supplier<F> builder) {
        _uiEffect.setValue(builder.get());
    }
}
