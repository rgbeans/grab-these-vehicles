package com.grabthesevehicles.app;

import static org.junit.Assert.*;
import android.app.Application;
import android.content.ContentResolver;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={26,36})
public class SoundProviderTest {
    private Application app;
    private ContentResolver resolver;

    @Before public void setUp() {
        app=RuntimeEnvironment.getApplication();
        resolver=app.getContentResolver();
        File extracted=new File(app.getNoBackupFilesDir(),"gta-notification.ogg");
        extracted.delete();
        new File(extracted.getPath()+".bak").delete();
        new File(extracted.getPath()+".new").delete();
    }

    @Test public void soundPlayerCanOpenTheCompleteAudioWithoutLaunchingAnActivity() throws Exception {
        ProviderInfo provider=app.getPackageManager().resolveContentProvider(SoundProvider.AUTHORITY,0);
        assertNotNull(provider);
        assertTrue(provider.exported);
        assertNull(provider.readPermission);
        assertEquals(SoundProvider.class.getName(),provider.name);
        assertEquals("audio/ogg",resolver.getType(SoundProvider.SOUND_URI));
        assertEquals(SoundProvider.SOUND_URI,resolver.canonicalize(SoundProvider.SOUND_URI));
        byte[] expected;
        try(InputStream original=app.getResources().openRawResource(R.raw.gta_notification)) {
            expected=readBytes(original);
        }
        try(AssetFileDescriptor audio=resolver.openAssetFileDescriptor(SoundProvider.SOUND_URI,"r")) {
            assertNotNull(audio);
            assertEquals(0,audio.getStartOffset());
            assertEquals(expected.length,audio.getDeclaredLength());
            assertEquals(expected.length,audio.getParcelFileDescriptor().getStatSize());
            try(InputStream stream=audio.createInputStream()) { assertArrayEquals(expected,readBytes(stream)); }
        }
        assertTrue(new File(app.getNoBackupFilesDir(),"gta-notification.ogg").isFile());
    }

    @Test public void damagedAudioIsRepairedOnBackgroundAccess() throws Exception {
        File extracted=new File(app.getNoBackupFilesDir(),"gta-notification.ogg");
        try(FileOutputStream broken=new FileOutputStream(extracted)) { broken.write(new byte[]{0,1,2}); }
        byte[] expected;
        try(InputStream original=app.getResources().openRawResource(R.raw.gta_notification)) {
            expected=readBytes(original);
        }
        try(InputStream stream=resolver.openInputStream(SoundProvider.SOUND_URI)) {
            assertArrayEquals(expected,readBytes(stream));
        }
    }

    @Test public void onlyTheSoundCanBeReadAndNoFilesCanBeWritten() throws Exception {
        for(Uri invalid:new Uri[]{
                Uri.parse("content://"+SoundProvider.AUTHORITY+"/../shared_prefs/simeon.xml"),
                Uri.parse("content://"+SoundProvider.AUTHORITY+"/another-file"),
                SoundProvider.SOUND_URI.buildUpon().appendQueryParameter("file","simeon.xml").build()}) {
            try(AssetFileDescriptor ignored=resolver.openAssetFileDescriptor(invalid,"r")) {
                fail("Unknown paths must not be opened");
            } catch(FileNotFoundException expected) { /* Fixed sound URI only. */ }
        }
        for(String mode:new String[]{"w","rw","rwt","wa"}) {
            try(AssetFileDescriptor ignored=resolver.openAssetFileDescriptor(SoundProvider.SOUND_URI,mode)) {
                fail("The public sound must be read-only");
            } catch(FileNotFoundException expected) { /* Read-only provider. */ }
        }
    }

    @Test public void soundHasUsefulMetadata() throws Exception {
        try(Cursor metadata=resolver.query(SoundProvider.SOUND_URI,null,null,null,null)) {
            assertNotNull(metadata);
            assertTrue(metadata.moveToFirst());
            assertEquals("GTA Online notification.ogg",metadata.getString(metadata.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)));
            assertTrue(metadata.getLong(metadata.getColumnIndexOrThrow(OpenableColumns.SIZE))>1000);
            assertFalse(metadata.moveToNext());
        }
    }

    private static byte[] readBytes(InputStream input) throws Exception {
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        byte[] buffer=new byte[8192];
        int count;
        while((count=input.read(buffer))!=-1) output.write(buffer,0,count);
        return output.toByteArray();
    }
}
