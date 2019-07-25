package ru.file.manager.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.file.manager.App;

public class PreferenceUtils
{
	private static SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    protected static boolean getBoolean(String key, boolean defaultValue)
	{
        return prefs.getBoolean(key, defaultValue);
    }
	
	protected static void putBoolean(String key, boolean value)
	{
        prefs.edit().putBoolean(key, value).apply();
    }

    protected static int getInteger(String key, int defaultValue)
	{
        return prefs.getInt(key, defaultValue);
    }

    protected static void putInt(String key, int value)
	{
        prefs.edit().putInt(key, value).apply();
    }
	
	protected static String getString(String key, String defaultValue)
	{
        return prefs.getString(key, defaultValue);
    }

    protected static void putString(String key, String value)
	{
        prefs.edit().putString(key, value).apply();
    }
}
