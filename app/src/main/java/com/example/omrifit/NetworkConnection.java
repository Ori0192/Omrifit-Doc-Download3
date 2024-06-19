package com.example.omrifit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;

/**
 * NetworkConnection is a LiveData class that monitors the network connectivity status.
 */
public class NetworkConnection extends LiveData<Boolean> {

    private static NetworkConnection instance;
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkConnectionCallback;

    /**
     * Private constructor for initializing NetworkConnection.
     *
     * @param context The application context.
     */
    public NetworkConnection(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Returns a singleton instance of NetworkConnection.
     *
     * @param context The application context.
     * @return The singleton instance of NetworkConnection.
     */
    public static synchronized NetworkConnection getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkConnection(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateNetworkConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(getConnectivityManagerCallback());
        } else {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(networkReceiver, filter);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (networkConnectionCallback != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkConnectionCallback);
            networkConnectionCallback = null;
        }
    }

    /**
     * Returns the ConnectivityManager.NetworkCallback instance for network changes.
     *
     * @return The NetworkCallback instance.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private ConnectivityManager.NetworkCallback getConnectivityManagerCallback() {
        networkConnectionCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onLost(Network network) {
                super.onLost(network);
                postValue(false);
            }

            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                if (networkIsStrong()) {
                    postValue(true);
                }
            }
        };
        return networkConnectionCallback;
    }

    /**
     * Checks if the current network connection is strong.
     *
     * @return True if the network connection is strong, false otherwise.
     */
    private boolean networkIsStrong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                boolean isStrong = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                return hasInternet && isStrong;
            }
        }
        return false;
    }

    /**
     * Updates the network connection status.
     */
    private void updateNetworkConnection() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        postValue(activeNetworkInfo != null && activeNetworkInfo.isConnected() && networkIsStrong());
    }

    /**
     * BroadcastReceiver for monitoring network changes on devices running on lower Android versions.
     */
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetworkConnection();
        }
    };
}
