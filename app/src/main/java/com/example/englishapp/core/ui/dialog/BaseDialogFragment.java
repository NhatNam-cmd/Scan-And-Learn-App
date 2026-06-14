package com.example.englishapp.core.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BaseDialogFragment extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_CONFIRM_TEXT = "confirm_text";
    private static final String ARG_DISMISS_TEXT = "dismiss_text";

    private Runnable onConfirmListener;
    private Runnable onDismissListener;

    public static BaseDialogFragment newInstance(String title, String message,
            String confirmText, @Nullable String dismissText) {
        BaseDialogFragment fragment = new BaseDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_CONFIRM_TEXT, confirmText != null ? confirmText : "Xác nhận");
        args.putString(ARG_DISMISS_TEXT, dismissText);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnConfirmListener(Runnable listener) { this.onConfirmListener = listener; }
    public void setOnDismissListener(Runnable listener) { this.onDismissListener = listener; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(args.getString(ARG_TITLE))
                .setMessage(args.getString(ARG_MESSAGE))
                .setPositiveButton(args.getString(ARG_CONFIRM_TEXT), (d, w) -> {
                    if (onConfirmListener != null) onConfirmListener.run();
                });
        String dismissText = args.getString(ARG_DISMISS_TEXT);
        if (dismissText != null) {
            builder.setNegativeButton(dismissText, (d, w) -> {
                if (onDismissListener != null) onDismissListener.run();
            });
        }
        return builder.create();
    }
}
