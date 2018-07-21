package com.example.ajay0.contacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Fragment fragment = new SettingsScreen();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(savedInstanceState == null){
            fragmentTransaction.add(R.id.settings_layout,fragment,"settings_fragment");
            fragmentTransaction.commit();
        }
        else {
            fragment = getFragmentManager().findFragmentByTag("settings_fragment");
        }
    }

    public static class SettingsScreen extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final String pref_file_type = "file_type";
        public static final String pref_sort_by = "sort_by";
        public static final String pref_contacts_to_display = "contacts_to_display";

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
            /*
            ListPreference file_type = (ListPreference) findPreference("file_type");
            if(file_type.getValue() == null)
                file_type.setValueIndex(0);

            ListPreference sort_by = (ListPreference) findPreference("sort_by");
            if(file_type.getValue() == null)
                file_type.setValueIndex(0);

            ListPreference contacts_to_display = (ListPreference) findPreference("contacts_to_display");
            if(file_type.getValue() == null)
                file_type.setValueIndex(0);

            */

            AccountManager am = AccountManager.get(getActivity());
            Account[] accounts = am.getAccounts();
            //String accountsNames = "";
            CharSequence[] accountsNames = new CharSequence[accounts.length];
            for(int x=0;x<accounts.length;x++)
                accountsNames[x] = accounts[x].type;


            ListPreference contacts_to_display = (ListPreference) findPreference("contacts_to_display");
            contacts_to_display.setEntries(accountsNames);
            contacts_to_display.setEntryValues(accountsNames);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(pref_file_type) || key.equals(pref_contacts_to_display)) {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
            }
            else if(key.equals(pref_sort_by)){
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                /*
                Intent refresh = new Intent("com.example.ajay0.contacts.MainActivity");
                startActivity(refresh);
                */
            }
        }
    }
}
