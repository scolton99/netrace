package tech.scolton.netrace.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
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

import lombok.Getter;
import tech.scolton.netrace.R;
import tech.scolton.netrace.tasks.PingTask;
import tech.scolton.netrace.util.PingResult;

public class PingFragment extends Fragment {
    private Button startButton;
    private TextInputEditText ipField;
    private RecyclerView pingList;
    private ProgressBar progressBar;
    private TextView countField;

    private PingAdapter pingAdapter = new PingAdapter();

    private int ct = 0;

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
        this.countField = view.findViewById(R.id.countField);

        this.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PingFragment.this.ct = 0;

                PingFragment.this.pingList.setItemAnimator(null);
                PingFragment.this.pingAdapter.reset();
                PingFragment.this.pingList.setItemAnimator(new DefaultItemAnimator());
                PingFragment.this.progressBar.setProgress(0);

                PingFragment.this.ipField.setError(null);

                PingTask p = new PingTask(
                        PingFragment.this.ipField.getText().toString(),
                        Integer.parseInt(PingFragment.this.countField.getText().toString()),
                        5000,
                        new WeakReference<Fragment>(PingFragment.this)
                );

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
        ObjectAnimator.ofInt(this.progressBar, "progress", (int)(progress * 10000.00f)).setDuration(150).start();
    }

    public void addPingResult(PingResult pingResult) {
        Log.i("PING", String.format("#%d\t%f\t%d", ++this.ct, pingResult.getRTT_MS(), pingResult.getTTL()));

        this.pingAdapter.addPingResult(pingResult);
    }

    public void hostnameError() {
        this.ipField.setError("This is not a valid IPv4 address.");
    }

    class PingViewHolder extends RecyclerView.ViewHolder {
        @Getter private TextView seqView;
        @Getter private TextView rttView;
        @Getter private TextView ttlView;

        PingViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);

            this.seqView = itemView.findViewById(R.id.pingSeq);
            this.rttView = itemView.findViewById(R.id.pingRTT);
            this.ttlView = itemView.findViewById(R.id.pingTTL);
        }
    }

    class PingAdapter extends RecyclerView.Adapter {
        private List<PingResult> data = new ArrayList<>();

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

            PingViewHolder pingViewHolder = (PingViewHolder) holder;
            pingViewHolder.getRttView().setText(String.format("%.2f ms", pingResult.getRTT_MS()));
            pingViewHolder.getTtlView().setText(String.format("%d", pingResult.getTTL()));
            pingViewHolder.getSeqView().setText(String.format("%d", position + 1));
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
