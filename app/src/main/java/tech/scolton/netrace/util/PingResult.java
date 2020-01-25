package tech.scolton.netrace.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PingResult {
    @Getter
    private float progress;

    @Getter
    private float RTT_MS;

    @Getter
    private int TTL;

    public static PingResult fromStringWithProgress(String s, float progress) {
        Pattern individual = Pattern.compile("icmp_seq=\\d+\\sttl=(\\d+)\\stime=(.*?)[\\r\\n]");
        Matcher individual_stat_matches = individual.matcher(s);

        if (s.contains("100% packet loss")) {
            return new PingResult(progress, -1.00f, -1);
        }

        if (!individual_stat_matches.find()) return PingResult.failedWithProgress(progress);

        String ttl_str = individual_stat_matches.group(1);
        String time_str = individual_stat_matches.group(2);

        if (ttl_str == null || time_str == null) return PingResult.failedWithProgress(progress);

        int ttl = Integer.valueOf(ttl_str);
        float time = Float.parseFloat(time_str.substring(0, time_str.length() - 3));

        return new PingResult(progress, time, ttl);
    }

    public static PingResult failedWithProgress(float progress) {
        return new PingResult(progress, -1.00f, -1);
    }
}
