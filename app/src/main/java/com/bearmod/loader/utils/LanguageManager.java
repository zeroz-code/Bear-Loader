package com.bearmod.loader.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LanguageManager {
    private static final String PREF_NAME = "language_prefs";
    private static final String KEY_CHINESE_ENABLED = "chinese_enabled";
    
    private final SharedPreferences preferences;
    
    public LanguageManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public boolean isChineseEnabled() {
        return preferences.getBoolean(KEY_CHINESE_ENABLED, false); // Default to English
    }
    
    public void setChineseEnabled(boolean enabled) {
        preferences.edit()
            .putBoolean(KEY_CHINESE_ENABLED, enabled)
            .apply();
    }
    
    public String getString(String chineseText, String englishText) {
        return isChineseEnabled() ? chineseText : englishText;
    }
}
