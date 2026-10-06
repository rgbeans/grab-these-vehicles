package com.grabthesevehicles.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.*;

/** Provides local incoming recordings to the system Phone app. It cannot place outgoing calls. */
public final class NativeCallService extends ConnectionService {
    @Override public Connection onCreateIncomingConnection(PhoneAccountHandle account,ConnectionRequest request) {
        Bundle extras=request.getExtras();
        String token=extras==null?null:extras.getString("gta_token");
        int contact=extras==null?-1:extras.getInt("gta_contact",-1);
        if(!CallTelecom.handle(this).equals(account) || !CallNotifications.matches(this,token) || !CallNotifications.active(this)
            || !"ringing".equals(CallNotifications.phase(this)) || contact<0 || contact>=CallContacts.IDS.length) {
            return Connection.createFailedConnection(new DisconnectCause(DisconnectCause.CANCELED));
        }
        GtaConnection connection=new GtaConnection(token,contact); CallTelecom.attach(token,connection); return connection;
    }
    @Override public Connection onCreateOutgoingConnection(PhoneAccountHandle account,ConnectionRequest request) {
        return Connection.createFailedConnection(new DisconnectCause(DisconnectCause.ERROR,"This account only receives simulated GTA calls"));
    }
    @Override public void onCreateIncomingConnectionFailed(PhoneAccountHandle account,ConnectionRequest request) {
        String token=request.getExtras()==null?null:request.getExtras().getString("gta_token");
        if(token!=null) CallNotifications.end(this,token);
    }
    final class GtaConnection extends Connection {
        private final String token;
        private boolean ended;
        private final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
        private final Runnable timeout;
        GtaConnection(String token,int contact) {
            this.token=token;
            timeout=() -> CallNotifications.end(NativeCallService.this,token,DisconnectCause.MISSED);
            setAddress(Uri.fromParts(PhoneAccount.SCHEME_TEL,CallerContacts.address(contact),null),TelecomManager.PRESENTATION_ALLOWED);
            setCallerDisplayName(CallContacts.NAMES[contact],TelecomManager.PRESENTATION_ALLOWED);
            setAudioModeIsVoip(true);
            setConnectionCapabilities(CAPABILITY_MUTE|CAPABILITY_HOLD|CAPABILITY_SUPPORT_HOLD);
            setRinging();
            handler.postDelayed(timeout,Math.max(0,CallScheduler.prefs(NativeCallService.this).getLong("active_until",0)-android.os.SystemClock.elapsedRealtime()));
        }
        @Override public void onAnswer() {
            if(ended || !CallNotifications.answer(NativeCallService.this,token)) return;
            handler.removeCallbacks(timeout);
            setActive();
            try { startForegroundService(new Intent(NativeCallService.this,CallPlaybackService.class).putExtra("token",token)); }
            catch(RuntimeException denied) { CallNotifications.end(NativeCallService.this,token,DisconnectCause.ERROR); }
        }
        @Override public void onAnswer(int videoState) { onAnswer(); }
        @Override public void onReject() { CallNotifications.end(NativeCallService.this,token,DisconnectCause.REJECTED); }
        @Override public void onDisconnect() { CallNotifications.end(NativeCallService.this,token,DisconnectCause.LOCAL); }
        @Override public void onAbort() { CallNotifications.end(NativeCallService.this,token,DisconnectCause.CANCELED); }
        @Override public void onHold() { CallScheduler.prefs(NativeCallService.this).edit().putBoolean("active_held",true).commit(); CallPlaybackService.hold(true); setOnHold(); }
        @Override public void onUnhold() { CallScheduler.prefs(NativeCallService.this).edit().putBoolean("active_held",false).commit(); setActive(); CallPlaybackService.hold(false); }
        void complete(int cause) {
            if(ended) return; ended=true;
            handler.removeCallbacks(timeout);
            setDisconnected(new DisconnectCause(cause)); destroy(); CallTelecom.detach(token);
        }
    }
}
