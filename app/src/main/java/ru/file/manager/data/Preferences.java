package ru.file.manager.data;

import ru.file.manager.utils.PreferenceUtils;

public class Preferences extends PreferenceUtils
{
    public static boolean isRated() {
        return getBoolean("isRated", false);
    }

    public static void setRated(boolean value){
        putBoolean("isRated", value);
    }
	
	public static boolean showExtensions(){
		return getBoolean("showExtensions", false);
	}
	
	public static String sortCriteria(){
		return getString("sortCriteria", "name");
	}
	
	public static void setSortCriteria(String criteria){
		putString("sortCriteria", criteria);
	}
}
