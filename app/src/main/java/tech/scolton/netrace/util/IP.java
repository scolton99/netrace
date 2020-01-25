package tech.scolton.netrace.util;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

public class IP {
    public static String render(int ip) {
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

    public static String render(byte[] blks) {
        StringBuilder sb = new StringBuilder();
        for (byte i : blks) {
            sb.append(i & 0xFF);
            sb.append(".");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static int ping(InetAddress ia) {
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
