package tech.scolton.netrace.tasks;

import android.os.AsyncTask;
import android.util.Log;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tech.scolton.netrace.fragments.PingFragment;
import tech.scolton.netrace.util.PingResult;

@RequiredArgsConstructor
public class PingTask extends AsyncTask<Void, PingResult, List<PingResult>> {
  @NonNull
  private String specified_host;
  @NonNull
  private int count;
  @NonNull
  private int timeout_ms;
  @NonNull
  private WeakReference<Fragment> pingFragment;

  private final List<PingResult> results = new ArrayList<>();
  private boolean failed = false;

  @Override
  protected List<PingResult> doInBackground(Void... voids) {
    // TODO: Show canonical hostname
    Inet4Address host;

    try {
      InetAddress ia = InetAddress.getByName(this.specified_host);
      if (!(ia instanceof Inet4Address)) {
        this.failed = true;
        return this.results;
      }

      host = (Inet4Address) ia;
    } catch (UnknownHostException e) {
      Log.w("PING", "Unknown host");
      return null;
    }

    Runtime runtime = Runtime.getRuntime();
    PingResult pr;

    for (int i = 0; i < this.count; ) {
      float progress = (float) ++i / this.count;

      try {
        String hostname = host.getHostAddress();
        float timeout_seconds = this.timeout_ms / 1000.00f;

        String command = String.format(Locale.ENGLISH, "ping -c 1 -W %.2f %s", timeout_seconds,
                                       hostname);

        Process p = runtime.exec(command);
        p.waitFor();
        int exit = p.exitValue();

        if (exit == 0) {
          StringBuilder sb = new StringBuilder();
          InputStreamReader reader = new InputStreamReader(p.getInputStream());
          BufferedReader buffer = new BufferedReader(reader);

          String line;
          while ((line = buffer.readLine()) != null) {
            sb.append(line).append('\n');
          }

          pr = PingResult.fromStringWithProgress(sb.toString(), progress);
        } else if (exit == 1) {
          pr = PingResult.failedWithProgress(progress);
          Log.w("PING", "Bad exit code (1)");
        } else {
          pr = PingResult.failedWithProgress(progress);
          Log.w("PING", "Bad exit code (2)");
        }
      } catch (InterruptedException | IOException e) {
        pr = PingResult.failedWithProgress(progress);
        Log.e("PING", "Exception", e);
      }

      this.publishProgress(pr);
      this.results.add(pr);
    }

    return this.results;
  }

  @Override
  protected void onProgressUpdate(PingResult... pr) {
    PingResult pingResult = pr[0];

    PingFragment pingFragment = (PingFragment) this.pingFragment.get();

    pingFragment.setPingProgress(pingResult.getProgress());
    pingFragment.addPingResult(pingResult);
  }

  @Override
  protected void onPostExecute(List<PingResult> result) {
    if (!failed) return;

    PingFragment pingFragment = (PingFragment) this.pingFragment.get();
    pingFragment.hostnameError();
  }
}
