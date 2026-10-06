package com.grabthesevehicles.app;

import android.Manifest;
import android.accounts.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import java.io.*;
import java.util.ArrayList;

/** Only touches raw contacts owned by the local GTA account; never aggregates with personal contacts. */
public final class CallerContacts {
    static final String TYPE="com.grabthesevehicles.callers", NAME="GTA Callers";
    private CallerContacts() {}
    public static boolean permitted(Context c) {
        return c.checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED
            && c.checkSelfPermission(Manifest.permission.WRITE_CONTACTS)==PackageManager.PERMISSION_GRANTED;
    }
    static String address(int contact) { return "+1202555"+String.format(java.util.Locale.US,"%04d",100+contact); }
    static Account account() { return new Account(NAME,TYPE); }
    private static long rawId(Context c,int contact) {
        try(Cursor cursor=c.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,new String[]{ContactsContract.RawContacts._ID},
            "account_type=? AND account_name=? AND sourceid=? AND deleted=0",new String[]{TYPE,NAME,CallContacts.IDS[contact]},null)) {
            return cursor!=null && cursor.moveToFirst()?cursor.getLong(0):-1;
        }
    }
    static void ensure(Context c,int contact) throws Exception {
        if(!permitted(c)) throw new SecurityException("Contacts access is required for native caller pictures and ringtone");
        AccountManager manager=AccountManager.get(c);
        boolean exists=false; for(Account a:manager.getAccountsByType(TYPE)) if(a.equals(account())) exists=true;
        if(!exists && !manager.addAccountExplicitly(account(),null,null)) throw new IOException("Cannot create the local GTA caller account");
        long id=rawId(c,contact);
        ArrayList<ContentProviderOperation> operations=new ArrayList<>();
        ContentProviderOperation.Builder raw=id<0?ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,TYPE).withValue(ContactsContract.RawContacts.ACCOUNT_NAME,NAME)
            .withValue(ContactsContract.RawContacts.SOURCE_ID,CallContacts.IDS[contact])
            :ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI).withSelection("_id=?",new String[]{Long.toString(id)});
        operations.add(raw.withValue(ContactsContract.RawContacts.AGGREGATION_MODE,ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
            .withValue(ContactsContract.RawContacts.CUSTOM_RINGTONE,SoundProvider.RINGTONE_URI.toString()).build());
        if(id>=0) operations.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
            .withSelection("raw_contact_id=?",new String[]{Long.toString(id)}).build());
        add(operations,id,CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,CommonDataKinds.StructuredName.DISPLAY_NAME,CallContacts.NAMES[contact]);
        add(operations,id,CommonDataKinds.Phone.CONTENT_ITEM_TYPE,CommonDataKinds.Phone.NUMBER,address(contact));
        add(operations,id,CommonDataKinds.Note.CONTENT_ITEM_TYPE,CommonDataKinds.Note.NOTE,"Local GTA simulated caller. Not a real phone number. Managed by Grab These Vehicles.");
        int icon=CallContacts.icon(contact);
        if(icon!=0) {
            byte[] image;
            try(InputStream input=c.getResources().openRawResource(icon); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[4096]; int n; while((n=input.read(buffer))!=-1) output.write(buffer,0,n); image=output.toByteArray();
            }
            add(operations,id,CommonDataKinds.Photo.CONTENT_ITEM_TYPE,CommonDataKinds.Photo.PHOTO,image);
        }
        c.getContentResolver().applyBatch(ContactsContract.AUTHORITY,operations);
    }
    private static void add(ArrayList<ContentProviderOperation> operations,long id,String mime,String column,Object value) {
        ContentProviderOperation.Builder row=ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValue(ContactsContract.Data.MIMETYPE,mime).withValue(column,value);
        if(id<0) row.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0); else row.withValue(ContactsContract.Data.RAW_CONTACT_ID,id);
        operations.add(row.build());
    }
    public static void install(Context c) throws Exception {
        for(int i=0;i<CallContacts.IDS.length;i++) if(CallClips.available(c,i)) ensure(c,i);
        CallScheduler.prefs(c).edit().putBoolean("native_contacts_ready",true).commit();
    }
    public static void remove(Context c) {
        CallScheduler.setEnabled(c,false);
        if(permitted(c)) c.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,"account_type=? AND account_name=?",new String[]{TYPE,NAME});
        AccountManager.get(c).removeAccountExplicitly(account());
        CallScheduler.prefs(c).edit().remove("native_contacts_ready").remove("enable_after_setup").commit();
    }
}


