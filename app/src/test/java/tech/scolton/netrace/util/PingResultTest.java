package tech.scolton.netrace.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PingResultTest {
  private static final String SUCCESSFUL_PING_1 =
          "PING google.com (172.217.5.110) 56(85) bytes of data.\n" +
          "64 bytes from sfo03s07-in-f14.1e100.net (172.217.5.110): icmp_seq=1 ttl=54 time=7.64 " +
          "ms\n" +
          "\n" + "--- google.com ping statistics ---\n" +
          "1 packets transmitted, 1 received, 0% packet loss, time 0ms\n" +
          "rtt min/avg/max/mdev = 7.648/7.648/7.648/0.000 ms\n";

  private static final String FAILED_PING_1 =
          "PING hku.hk (147.8.2.58) 56(84) bytes of data.\n" + "\n" +
          "--- hku.hk ping statistics ---\n" +
          "1 packets transmitted, 0 received, 100% packet loss, time 0ms\n" + "\n";

  @Test
  public void parses_successful_ping() {
    PingResult p = PingResult.fromStringWithProgress(SUCCESSFUL_PING_1, 0.00f);

    assertNotNull(p);

    float progress = p.getProgress();
    int ttl = p.getTTL();
    float rtt = p.getRTT_MS();

    assert (progress == 0.00f);
    assert (ttl == 54);
    assert (rtt == 7.64f);
  }

  @Test
  public void parses_failed_ping() {
    PingResult p = PingResult.fromStringWithProgress(FAILED_PING_1, 0.00f);

    assertNotNull(p);

    float progress = p.getProgress();
    int ttl = p.getTTL();
    float rtt = p.getRTT_MS();

    assert (progress == 0.00f);
    assert (ttl == -1);
    assert (rtt == -1.00f);
  }
}
