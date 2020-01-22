package tech.scolton.netrace.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import tech.scolton.netrace.R;

public class PingFragment extends Fragment {
    private Button startButton;
    private TextView ipField;

    public PingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ping, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.startButton = view.findViewById(R.id.startButton);
        this.ipField = view.findViewById(R.id.ipField);

        this.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = String.valueOf(PingFragment.this.ipField.getText());
                String[] strParts = ip.split(".");

                byte[] parts = new byte[4];
                int i = 0;
                for (String s : strParts) {
                    parts[i++] = (byte)(Integer.valueOf(s) & 0xFF);
                }

                try {
                    Inet4Address toPing = Inet4Address.getByAddress(parts);
                } catch (UnknownHostException e) {
                    Log.w("", );
                }
            }
        });
    }

    private static int ping(InetAddress ia) {
        long start = System.nanoTime();

        boolean reachable;
        try {
            reachable = ia.isReachable(5000);
        } catch (IOException e) {
            Log.w("PING", "Ping failed: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }

        long end = System.nanoTime();

        return reachable ? (int)((end - start) / 1000000) : -1;
    }
}
