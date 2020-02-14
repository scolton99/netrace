package tech.scolton.netrace.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tech.scolton.netrace.fragments.MainFragment;

public class WifiInfoReceiver extends BroadcastReceiver {
  private final MainFragment fragment;

  public WifiInfoReceiver(MainFragment f) {
    this.fragment = f;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    this.fragment.loadWifiInfo();
  }
}
