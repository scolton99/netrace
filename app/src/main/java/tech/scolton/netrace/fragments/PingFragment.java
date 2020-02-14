package tech.scolton.netrace.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import tech.scolton.netrace.R;
import tech.scolton.netrace.tasks.PingTask;
import tech.scolton.netrace.util.PingResult;

public class PingFragment extends Fragment {
  private TextInputEditText ipField;
  private RecyclerView pingList;
  private ProgressBar progressBar;
  private TextInputEditText countField;

  private final PingAdapter pingAdapter = new PingAdapter();

  public PingFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_ping, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    this.ipField = view.findViewById(R.id.ipField);
    this.countField = view.findViewById(R.id.countField);

    Button startButton = view.findViewById(R.id.startButton);
    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PingFragment.this.pingList.setItemAnimator(null);
        PingFragment.this.pingAdapter.reset();
        PingFragment.this.pingList.setItemAnimator(new DefaultItemAnimator());
        PingFragment.this.progressBar.setProgress(0);

        PingFragment.this.ipField.setError(null);

        Editable ipString = PingFragment.this.ipField.getText();
        if (ipString == null || ipString.length() == 0) {
          PingFragment.this.hostnameError();
          return;
        }

        Editable countString = PingFragment.this.countField.getText();
        if (countString == null || countString.length() == 0) {
          PingFragment.this.countError();
          return;
        }

        String ip = ipString.toString();

        int count;
        try {
          count = Integer.parseInt(countString.toString());
        } catch (NumberFormatException e) {
          PingFragment.this.countError();
          return;
        }

        WeakReference<Fragment> ref = new WeakReference<Fragment>(PingFragment.this);

        PingTask p = new PingTask(ip, count, 5000, ref);
        p.execute();
      }
    });

    this.pingList = view.findViewById(R.id.pingList);
    this.pingList.setLayoutManager(new LinearLayoutManager(this.getActivity()));
    this.pingList.setAdapter(this.pingAdapter);

    this.progressBar = view.findViewById(R.id.progressBar);
  }

  public void setPingProgress(float progress) {
    Log.i("PING", String.format("%f", progress));

    int realProgress = (int) (progress * 10_000.00f);
    ObjectAnimator.ofInt(this.progressBar, "progress", realProgress).setDuration(150).start();
  }

  public void addPingResult(PingResult pingResult) {
    this.pingAdapter.addPingResult(pingResult);
  }

  public void hostnameError() {
    this.ipField.setError("This is not a valid IPv4 address.");
  }

  private void countError() {
    this.countField.setError("");
  }

  class PingViewHolder extends RecyclerView.ViewHolder {
    @Getter
    private final TextView seqView;
    @Getter
    private final TextView rttView;
    @Getter
    private final TextView ttlView;

    PingViewHolder(@NonNull LinearLayout itemView) {
      super(itemView);

      this.seqView = itemView.findViewById(R.id.pingSeq);
      this.rttView = itemView.findViewById(R.id.pingRTT);
      this.ttlView = itemView.findViewById(R.id.pingTTL);
    }
  }

  class PingAdapter extends RecyclerView.Adapter {
    private final List<PingResult> data = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LinearLayout root = (LinearLayout) LayoutInflater.from(parent.getContext())
              .inflate(R.layout.ping_item, parent, false);

      return new PingViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
      PingResult pingResult = this.data.get(position);

      String rtt = String.format(Locale.ENGLISH, "%.2f ms", pingResult.getRTT_MS());
      String ttl = String.format(Locale.ENGLISH, "%d", pingResult.getTTL());
      String seq = String.format(Locale.ENGLISH, "%d", position + 1);

      PingViewHolder pingViewHolder = (PingViewHolder) holder;
      pingViewHolder.getRttView().setText(rtt);
      pingViewHolder.getTtlView().setText(ttl);
      pingViewHolder.getSeqView().setText(seq);
    }

    @Override
    public int getItemCount() {
      return this.data.size();
    }

    void addPingResult(PingResult p) {
      this.data.add(p);
      this.notifyItemInserted(this.data.size() - 1);
    }

    void reset() {
      int removed = this.data.size();
      this.data.clear();
      this.notifyItemRangeRemoved(0, removed);
    }
  }
}
