package com.grabthesevehicles.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.AtomicFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/** Exposes only the bundled chime, so the system sound player can open a real audio file. */
public final class SoundProvider extends ContentProvider {
    public static final String AUTHORITY = "com.grabthesevehicles.app.sounds";
    public static final Uri SOUND_URI = Uri.parse("content://" + AUTHORITY + "/gta-notification.ogg");

    @Override public boolean onCreate() { return true; }

    static synchronized File prepareAudio(Context context) throws IOException {
        byte[] audio;
        try (InputStream input = context.getResources().openRawResource(R.raw.gta_notification)) {
            audio = readBytes(input);
        }
        AtomicFile file = new AtomicFile(new File(context.getNoBackupFilesDir(), "gta-notification.ogg"));
        // Recreate missing or damaged files, including after an app update, without opening the UI.
        try (InputStream current = file.openRead()) {
            if (Arrays.equals(audio, readBytes(current))) return file.getBaseFile();
        } catch (FileNotFoundException missing) { /* First use: extract the bundled clip. */ }
        FileOutputStream output = null;
        try {
            output = file.startWrite();
            output.write(audio);
            file.finishWrite(output);
        } catch (IOException error) {
            file.failWrite(output);
            throw error;
        }
        return file.getBaseFile();
    }

    private static byte[] readBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!SOUND_URI.equals(uri) || !"r".equals(mode)) {
            throw new FileNotFoundException("Only the GTA notification sound is available, read-only");
        }
        try {
            return ParcelFileDescriptor.open(prepareAudio(getContext()), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException error) {
            FileNotFoundException unavailable = new FileNotFoundException("Cannot open the GTA notification sound");
            unavailable.initCause(error);
            throw unavailable;
        }
    }

    @Override public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        ParcelFileDescriptor descriptor = openFile(uri, mode);
        return new AssetFileDescriptor(descriptor, 0, descriptor.getStatSize());
    }

    @Override public String getType(Uri uri) { return SOUND_URI.equals(uri) ? "audio/ogg" : null; }
    @Override public Uri canonicalize(Uri uri) { return SOUND_URI.equals(uri) ? SOUND_URI : null; }
    @Override public Uri uncanonicalize(Uri uri) { return canonicalize(uri); }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] arguments, String order) {
        if (!SOUND_URI.equals(uri)) throw new IllegalArgumentException("Unknown sound URI");
        String[] columns = projection == null
            ? new String[] {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE} : projection;
        MatrixCursor cursor = new MatrixCursor(columns, 1);
        Object[] values = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) values[i] = "GTA Online notification.ogg";
            else if (OpenableColumns.SIZE.equals(columns[i])) {
                try { values[i] = prepareAudio(getContext()).length(); }
                catch (IOException error) { throw new IllegalStateException("Cannot read sound metadata", error); }
            }
        }
        cursor.addRow(values);
        return cursor;
    }

    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException("Read-only sound"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] arguments) { throw new UnsupportedOperationException("Read-only sound"); }
    @Override public int delete(Uri uri, String selection, String[] arguments) { throw new UnsupportedOperationException("Read-only sound"); }
}
