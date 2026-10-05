package com.grabthesevehicles.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

/** A notification opens this brief foreground window to copy safely on modern Android. */
public final class CopyMessageActivity extends Activity {
    public static final String MESSAGE = "simeon_message";
    private boolean copied;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(new FrameLayout(this));
        if (getIntent().getStringExtra(MESSAGE) == null) finish();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus || copied || isFinishing()) return;
        copied = true;
        String message = getIntent().getStringExtra(MESSAGE);
        if (message != null) {
            MessageClipboard.copy(this, message);
        }
        finish();
    }
}
