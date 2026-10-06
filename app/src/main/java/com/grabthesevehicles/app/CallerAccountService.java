package com.grabthesevehicles.app;

import android.accounts.*;
import android.app.Service;
import android.content.Intent;
import android.os.*;

/** An on-device account that owns the GTA contacts. There is no login or sync. */
public final class CallerAccountService extends Service {
    private AbstractAccountAuthenticator authenticator;
    @Override public void onCreate() {
        super.onCreate();
        authenticator=new AbstractAccountAuthenticator(this) {
            private Bundle unsupported() { Bundle b=new Bundle(); b.putInt(AccountManager.KEY_ERROR_CODE,AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION); return b; }
            public Bundle editProperties(AccountAuthenticatorResponse r,String type) { return unsupported(); }
            public Bundle addAccount(AccountAuthenticatorResponse r,String type,String token,String[] features,Bundle options) { return unsupported(); }
            public Bundle confirmCredentials(AccountAuthenticatorResponse r,Account a,Bundle options) { return unsupported(); }
            public Bundle getAuthToken(AccountAuthenticatorResponse r,Account a,String type,Bundle options) { return unsupported(); }
            public String getAuthTokenLabel(String type) { return ""; }
            public Bundle updateCredentials(AccountAuthenticatorResponse r,Account a,String type,Bundle options) { return unsupported(); }
            public Bundle hasFeatures(AccountAuthenticatorResponse r,Account a,String[] features) { Bundle b=new Bundle(); b.putBoolean(AccountManager.KEY_BOOLEAN_RESULT,features.length==0); return b; }
        };
    }
    @Override public IBinder onBind(Intent intent) { return authenticator.getIBinder(); }
}
