package com.example.cryonix.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/** Is the phone connected to the internet right now? (CA-05 offline label) */
public class NetworkHelper {

    @SuppressWarnings("deprecation")
    public static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
