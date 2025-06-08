package com.keyauth.loader.utils;

import android.content.Context;

public class SessionManager {
    
    private final SecurePreferences securePreferences;
    
    public SessionManager(Context context) {
        securePreferences = new SecurePreferences(context);
    }
    
    public void clearSession() {
        securePreferences.clearAll();
    }
    
    public boolean isLoggedIn() {
        return securePreferences.getLicenseKey() != null && !securePreferences.getLicenseKey().isEmpty();
    }
}
