package ru.file.manager.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import ru.file.manager.R;

public class SettingsFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences(Bundle bundle, String s)
	{
		addPreferencesFromResource(R.xml.preferences);
	}
}
