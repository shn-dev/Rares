package com.patiserrie.rares;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import java.util.List;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class SettingsActivity extends AppCompatActivity{

    private static final String TAG = "Settings";

    /**
     * Clears all saved shared preferences.
     */
    private void clearAllPreferences(){
        SharedPreferences sharedPrefs = getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Settings");

        //region Enable/Disable notifications
        SharedPreferences sharedPrefs = getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                Log.d(TAG, "onSharedPreferenceChanged: Installation id=" + installation.getInstallationId());
                switch (key){
                    case "NOTIFICATIONS_ENABLED":
                        boolean checkValue = sharedPreferences.getBoolean("NOTIFICATIONS_ENABLED", true);
                        SaveCallback saveCallback = new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null) {
                                    Log.d(TAG, "done: Saved notification preference in background");
                                }
                                else{
                                    Log.d(TAG, "done: Problem saving notification preference.");
                                }
                            }
                        };

                        if(checkValue){
                            installation.remove("channels");
                            installation.add("channels", "NOTIFICATIONS_ENABLED");
                            installation.saveInBackground(saveCallback);
                        }else{ //Notifications disabled
                            installation.remove("channels");
                            installation.add("channels", "NOTIFICATIONS_DISABLED");
                            installation.saveInBackground(saveCallback);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        //endregion

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_graph);
        }


        //TODO: Verify if this code can be deleted
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
