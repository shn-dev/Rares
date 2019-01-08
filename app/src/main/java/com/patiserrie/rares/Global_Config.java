package com.patiserrie.rares;

import android.app.Application;

import com.patiserrie.rares.R;
import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by ssepa on 9/6/2017.
 */

public class Global_Config extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.parse_app_id))
                .clientKey(getString(R.string.parse_client_key))
                .enableLocalDataStore()
                .server(getString(R.string.parse_server_url))
                .build());

        //region Configure push notifications
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "424441160703");
        installation.saveInBackground();
        //endregion
    }
}
