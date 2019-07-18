package ru.file.manager.data;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import ru.file.manager.App;

public class Preferences
{
	private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    public static boolean isRated() {
        return preferences.getBoolean("isRated", false);
    }

    public static void setRated(boolean value){
        preferences.edit().putBoolean("isRated", value).apply();
    }
}
