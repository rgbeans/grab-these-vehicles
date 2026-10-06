package com.grabthesevehicles.app;

import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Managed Telecom calls use the existing Phone app, its audio routes, and its call-volume controls. */
public final class CallTelecom {
    private static final Map<String,NativeCallService.GtaConnection> connections=new HashMap<>();
    private CallTelecom() {}
    static PhoneAccountHandle handle(Context c) { return new PhoneAccountHandle(new ComponentName(c,NativeCallService.class),"gta-incoming"); }
    public static void register(Context c) {
        TelecomManager manager=c.getSystemService(TelecomManager.class);
        if(manager==null) return;
        manager.registerPhoneAccount(new PhoneAccount.Builder(handle(c),"GTA Calls")
            .setShortDescription("Local simulated GTA calls")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .setSupportedUriSchemes(Collections.singletonList(PhoneAccount.SCHEME_SIP))
            .setIcon(Icon.createWithResource(c,R.drawable.ic_message)).setHighlightColor(Color.rgb(255,176,71)).build());
    }
    public static boolean ready(Context c) {
        if(!CallerContacts.permitted(c) || !CallScheduler.prefs(c).getBoolean("native_contacts_ready",false)) return false;
        try { TelecomManager manager=c.getSystemService(TelecomManager.class); return manager!=null && manager.isIncomingCallPermitted(handle(c)); }
        catch(SecurityException denied) { android.util.Log.w("GtaCalls","Cannot inspect GTA calling account",denied); return false; }
    }
    static void incoming(Context c,int contact,String token) throws Exception {
        CallerContacts.ensure(c,contact);
        Bundle extras=new Bundle(); extras.putString("gta_token",token); extras.putInt("gta_contact",contact);
        extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS,Uri.fromParts(PhoneAccount.SCHEME_TEL,CallerContacts.address(contact),null));
        c.getSystemService(TelecomManager.class).addNewIncomingCall(handle(c),extras);
    }
    static synchronized void attach(String token,NativeCallService.GtaConnection connection) { connections.put(token,connection); }
    static synchronized void detach(String token) { connections.remove(token); }
    static synchronized void end(String token,int cause) { NativeCallService.GtaConnection connection=connections.remove(token); if(connection!=null) connection.complete(cause); }
}



