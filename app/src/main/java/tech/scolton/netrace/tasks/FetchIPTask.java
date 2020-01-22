package tech.scolton.netrace.tasks;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

import tech.scolton.netrace.fragments.MainFragment;


public class FetchIPTask extends AsyncTask<Void, Void, String> {
    private WeakReference<MainFragment> activityRef;

    public FetchIPTask(WeakReference<MainFragment> activityRef) {
        this.activityRef = activityRef;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL("https://api.ipify.org?format=json");
            HttpsURLConnection request = (HttpsURLConnection) url.openConnection();

            InputStream response = new BufferedInputStream(request.getInputStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            int result;
            while ((result = response.read()) != -1)
                buf.write(result);

            String rawJson = buf.toString();
            JSONObject obj = new JSONObject(rawJson);

            return obj.getString("ip");
        } catch (IOException | JSONException ignored) {}

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        activityRef.get().onIpLoaded(s);
    }
}
