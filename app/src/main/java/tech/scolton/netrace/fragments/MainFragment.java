package tech.scolton.netrace.fragments;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.net.Inet4Address;
import java.net.InetAddress;

import tech.scolton.netrace.R;
import tech.scolton.netrace.receivers.WifiInfoReceiver;
import tech.scolton.netrace.tasks.FetchIPTask;

import static android.content.Context.WIFI_SERVICE;
import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;

public class MainFragment extends Fragment {
    private TextView ipAddressView;
    private TextView ssidView;
    private TextView localIpView;
    private ImageView wifiIcon;
    private WifiInfoReceiver wir;
    private String ipAddress;
    private TextView cellularNameView;
    private TextView cellularIpView;
    private TextView wifiLinkSpeed;
    private ImageView cellularIcon;

    private Handler speedInterval;
    private TelephonyListener telephonyListener = new TelephonyListener();

    private ValueAnimator ipAnimator = ValueAnimator.ofFloat(0, 1, 0, 0, 0, 0, 0, 0);

    private static final int NETWORK_STATE_PERMISSION_REQUEST = 1;
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 2;

    public MainFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.ipAnimator.setDuration(800);
        this.ipAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.ipAnimator.setRepeatMode(ValueAnimator.REVERSE);
        this.ipAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newColor = ColorUtils.blendARGB(getResources().getColor(R.color.textGray), Color.WHITE, animation.getAnimatedFraction());
                MainFragment.this.ipAddressView.setBackgroundColor(newColor);
            }
        });

        this.wir = new WifiInfoReceiver(this);
        IntentFilter filter = new IntentFilter(NETWORK_STATE_CHANGED_ACTION);
        Activity parent = this.getActivity();
        if (parent != null)
            parent.registerReceiver(this.wir, filter);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (this.ipAddress == null) {
            new FetchIPTask(new WeakReference<>(this)).execute();
            this.ipLoadingAnimation();
        }

        this.checkPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Activity parent = this.getActivity();
        if (parent == null)
            return;

        parent.unregisterReceiver(wir);

        TelephonyManager tm = (TelephonyManager) parent.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null)
            return;

        ConnectivityManager cm = (ConnectivityManager) parent.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        cm.unregisterNetworkCallback(this.telephonyListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case NETWORK_STATE_PERMISSION_REQUEST: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    this.getTelephonyInfo();
            }
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    this.loadWifiInfo();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.ipAddressView = view.findViewById(R.id.ipAddressView);
        this.ssidView = view.findViewById(R.id.ssidView);
        this.wifiIcon = view.findViewById(R.id.wifiIcon);

        this.localIpView = view.findViewById(R.id.localIpView);
        this.cellularNameView = view.findViewById(R.id.cellularNameView);
        this.cellularIpView = view.findViewById(R.id.cellularIpView);
        this.wifiLinkSpeed = view.findViewById(R.id.wifiLinkSpeed);
        this.cellularIcon = view.findViewById(R.id.cellularIcon);

        this.speedInterval = new Handler();

        Runnable speedCheck = new Runnable() {
            @Override
            public void run() {
                MainFragment.this.loadWifiSpeed();
                speedInterval.postDelayed(this, 2000);
            }
        };

        this.speedInterval.post(speedCheck);

        if (savedInstanceState != null)
            this.ipAddress = savedInstanceState.getString("ipAddress");

        Activity parent = this.getActivity();
        if (parent == null)
            return;

        TelephonyManager tm = (TelephonyManager) parent.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null)
            return;

        ConnectivityManager cm = (ConnectivityManager) parent.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        NetworkRequest.Builder nrb = new NetworkRequest.Builder();
        nrb.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest nr = nrb.build();
        cm.registerNetworkCallback(nr, this.telephonyListener);
    }

    private void ipLoadingAnimation() {
        this.ipAddressView.setText("");
        this.ipAnimator.start();
    }

    public void onIpLoaded(String ip) {
        this.ipAnimator.end();
        this.ipAddressView.setBackgroundColor(Color.TRANSPARENT);
        this.ipAddressView.setText(ip);
        ViewGroup.LayoutParams lp = this.ipAddressView.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        ipAddressView.setLayoutParams(lp);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("ipAddress", this.ipAddress);
    }

    public void loadWifiInfo() {
        Activity parent = this.getActivity();
        if (parent == null)
            return;

        WifiManager wifiManager = (WifiManager) parent.getApplicationContext().getSystemService(WIFI_SERVICE);

        if (wifiManager == null || wifiManager.getConnectionInfo().getSSID().equals("<unknown ssid>")) {
            this.ssidView.setText(getString(R.string.not_connected));
            this.ssidView.setTextColor(getResources().getColor(R.color.textGray));
            this.wifiIcon.setImageResource(R.drawable.ic_signal_wifi_off_black_24dp);

            this.localIpView.setText("");
            this.localIpView.setVisibility(View.GONE);

            this.wifiLinkSpeed.setText("N/A");
        } else {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            ssid = ssid.substring(1, ssid.length() - 1);

            this.ssidView.setText(ssid);
            this.ssidView.setTextColor(getResources().getColor(android.R.color.primary_text_dark));

            this.wifiIcon.setImageResource(R.drawable.ic_wifi_black_24dp);

            this.localIpView.setText(MainFragment.renderIP(wifiInfo.getIpAddress()));
            this.localIpView.setTextColor(getActivity().getResources().getColor(android.R.color.primary_text_dark));
            this.localIpView.setVisibility(View.VISIBLE);

            this.loadWifiSpeed(wifiInfo);
        }
    }

    private void loadWifiSpeed() {
        Activity parent = this.getActivity();
        if (parent == null)
            return;

        WifiManager wifiManager = (WifiManager) parent.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null || wifiManager.getConnectionInfo().getSSID().equals("<unknown ssid>"))
            return;

        this.loadWifiSpeed(wifiManager.getConnectionInfo());
    }

    private void loadWifiSpeed(WifiInfo wifiInfo) {
        this.wifiLinkSpeed.setText(getString(R.string.speed_mbps, wifiInfo.getLinkSpeed()));
    }

    private static String renderIP(int ip) {
        int[] blks = new int[4];
        blks[0] = ip & 0xFF;
        blks[1] = (ip >> 8) & 0xFF;
        blks[2] = (ip >> 16) & 0xFF;
        blks[3] = (ip >> 24) & 0xFF;

        StringBuilder sb = new StringBuilder();
        for (int i : blks) {
            sb.append(i);
            sb.append(".");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private static String renderIP(byte[] blks) {
        StringBuilder sb = new StringBuilder();
        for (byte i : blks) {
            sb.append(i & 0xFF);
            sb.append(".");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private void checkPermissions() {
        Activity parent = this.getActivity();
        if (parent == null)
            return;

        if (ActivityCompat.checkSelfPermission(parent, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        else
            this.loadWifiInfo();

        if (ActivityCompat.checkSelfPermission(parent, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{ Manifest.permission.ACCESS_NETWORK_STATE }, 1);
        else
            this.getTelephonyInfo();
    }

    private void getTelephonyInfo() {
        Activity parent = this.getActivity();
        if (parent == null)
            return;

        TelephonyManager tm = (TelephonyManager) parent.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            this.cellularDisconnected();
            return;
        }

        this.cellularNameView.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        this.cellularIcon.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
        this.cellularNameView.setText(tm.getNetworkOperatorName());

        ConnectivityManager cm = (ConnectivityManager) parent.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            this.cellularNoIp();
            return;
        }

        Inet4Address address = MainFragment.getCellularIPv4(cm);
        if (address == null) {
            this.cellularNoIp();
            return;
        }

        this.cellularIpView.setText(MainFragment.renderIP(address.getAddress()));
        this.cellularIpView.setVisibility(View.VISIBLE);
    }

    private static Inet4Address getCellularIPv4(@NonNull ConnectivityManager connectivityManager) {
        Network[] networks = connectivityManager.getAllNetworks();

        for (Network n : networks) {
            NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(n);
            if (nc == null) continue;

            if (!nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) continue;

            LinkProperties lp = connectivityManager.getLinkProperties(n);
            if (lp == null) continue;

            for (LinkAddress i : lp.getLinkAddresses()) {
                InetAddress ia = i.getAddress();
                if (ia instanceof Inet4Address)
                    return (Inet4Address) ia;
            }
        }

        return null;
    }

    private void cellularDisconnected() {
        this.cellularNameView.setText(getString(R.string.not_connected));
        this.cellularNameView.setTextColor(getResources().getColor(R.color.textGray));
        this.cellularIpView.setText("");
        this.cellularIpView.setVisibility(View.GONE);
        this.cellularIcon.setImageResource(R.drawable.ic_signal_cellular_off_black_24dp);
    }

    private void cellularNoIp() {
        this.cellularIpView.setText("");
        this.cellularIpView.setVisibility(View.GONE);
        this.cellularIcon.setImageResource(R.drawable.ic_signal_cellular_off_black_24dp);
    }

    private final class TelephonyListener extends ConnectivityManager.NetworkCallback {
        private void run() {
            Activity parent = MainFragment.this.getActivity();
            if (parent == null)
                return;

            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainFragment.this.getTelephonyInfo();
                }
            });
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            this.run();
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            this.run();
        }

        @Override
        public void onLost(@NonNull Network network) {
            this.run();
        }

        @Override
        public void onUnavailable() {
            this.run();
        }
    }
}
