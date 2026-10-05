package com.grabthesevehicles.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

final class MessageClipboard {
    private MessageClipboard() {}
    static void copy(Context context, CharSequence message) {
        context.getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Simeon", message));
        if (Build.VERSION.SDK_INT < 33) Toast.makeText(context, "Simeon's message copied", Toast.LENGTH_SHORT).show();
    }
}
