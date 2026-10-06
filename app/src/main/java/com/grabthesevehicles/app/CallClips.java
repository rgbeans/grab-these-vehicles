package com.grabthesevehicles.app;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import java.io.*;
import java.util.*;

/** Local, character-specific recordings. Bundled clips use call_<contact>[_variant]. */
public final class CallClips {
    private CallClips() {}
    static File file(Context c, int contact) {
        return new File(c.getFilesDir(), "call-" + ContactMessages.IDS[contact] + ".audio");
    }
    static List<Integer> resources(int contact) {
        List<Integer> result = new ArrayList<>();
        String prefix = "call_" + ContactMessages.IDS[contact];
        for (java.lang.reflect.Field field : R.raw.class.getFields()) {
            if (field.getName().equals(prefix) || field.getName().startsWith(prefix + "_")) {
                try { result.add(field.getInt(null)); } catch (IllegalAccessException ignored) {}
            }
        }
        return result;
    }
    public static boolean available(Context c, int contact) {
        return file(c,contact).isFile() || !resources(contact).isEmpty();
    }
    public static List<Integer> callers(Context c) {
        List<Integer> result = new ArrayList<>();
        for (int i=0;i<ContactMessages.IDS.length;i++)
            if (CallScheduler.prefs(c).getBoolean("contact_"+ContactMessages.IDS[i],true) && available(c,i)) result.add(i);
        return result;
    }
    static void configure(Context c, int contact, MediaPlayer player) throws IOException {
        File imported=file(c,contact);
        if(imported.isFile()) { player.setDataSource(imported.getPath()); return; }
        List<Integer> clips=resources(contact);
        if(clips.isEmpty()) throw new IOException("No recording for this caller");
        int resource=clips.get(new Random().nextInt(clips.size()));
        try(AssetFileDescriptor fd=c.getResources().openRawResourceFd(resource)) {
            player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
        }
    }
    static void importClip(Context c,int contact,Uri uri) throws IOException {
        File temporary=File.createTempFile("call-import-", ".audio",c.getCacheDir());
        MediaMetadataRetriever metadata=new MediaMetadataRetriever();
        try {
            try(InputStream input=c.getContentResolver().openInputStream(uri); OutputStream output=new FileOutputStream(temporary)) {
                if(input==null) throw new IOException("Cannot open this recording");
                byte[] buffer=new byte[8192]; long size=0; int n;
                while((n=input.read(buffer))!=-1) { size+=n; if(size>20_000_000) throw new IOException("Use a recording smaller than 20 MB"); output.write(buffer,0,n); }
            }
            metadata.setDataSource(temporary.getPath());
            String duration=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String audio=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            if(!"yes".equals(audio) || duration==null || Long.parseLong(duration)<=0 || Long.parseLong(duration)>300_000)
                throw new IOException("Use an audio recording up to five minutes long");
            File target=file(c,contact);
            android.util.AtomicFile atomic=new android.util.AtomicFile(target);
            FileOutputStream output=null;
            try(InputStream input=new FileInputStream(temporary)) {
                output=atomic.startWrite(); byte[] buffer=new byte[8192]; int n;
                while((n=input.read(buffer))!=-1) output.write(buffer,0,n);
                atomic.finishWrite(output);
            } catch(IOException error) { atomic.failWrite(output); throw error; }
        } catch(RuntimeException error) { throw new IOException("This file cannot be played as audio",error); }
        finally { metadata.release(); temporary.delete(); }
    }
}
