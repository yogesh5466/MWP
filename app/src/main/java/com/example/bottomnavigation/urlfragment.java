package com.example.bottomnavigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;



import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.support.constraint.Constraints.TAG;
import static com.androidnetworking.common.ANConstants.USER_AGENT;

public class urlfragment extends android.support.v4.app.Fragment implements
        View.OnClickListener,
        View.OnTouchListener ,
        OnTaskCompleted{
    Button scan;
    String urls;
    EditText text;
    String responseJSON;
    private AdView mAdView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.urlfrag, viewGroup, false);
        AndroidNetworking.initialize(getActivity().getApplicationContext());
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        scan = view.findViewById(R.id.button);
        text = view.findViewById(R.id.editText);
        scan.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                urls = text.getText().toString();
                if (isOnline()) {
                    if (urls != null && !urls.isEmpty() && Patterns.WEB_URL.matcher(urls).matches()) {
                        if ((!urls.startsWith("https://")) && (!urls.startsWith("http://"))) {
                            urls = "https://" + urls;
                        }
                        if(urls.startsWith("http://")){
                            urls = urls.replaceFirst("^(http|https)://", "https://");
                        }
                        Malicious m = new Malicious(this);
                        m.execute(urls);

                    } else {
                        showtoast("enter a valid url");
                        text.getText().clear();
                    }
                } else {
                    showtoast("No Internet Connection");
                }
                break;


            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @SuppressLint("ShowToast")
    private void showtoast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    public boolean isOnline() {
        if(getActivity()!=null) {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            return cm.getActiveNetworkInfo() != null &&
                    cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
        return false;
    }




    @Override
    public void onTaskCompleted(final String url) {final Intent intent = new Intent(getActivity(), SecondActivity.class);

        AndroidNetworking.get(url)
                .addPathParameter("pageNumber", "0")
                .addQueryParameter("limit", "3")
                .setPriority(Priority.LOW)
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        // do anything with response
                        if (response.isSuccessful()) {
                            intent.putExtra("url", url);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // handle error
                        Toast.makeText(getActivity(), "404 ERROR!!!", Toast.LENGTH_SHORT).show();
                        text.getText().clear();
                    }
                });


    }


    class Malicious extends AsyncTask<String, Void, Wrapper> {
        private OnTaskCompleted listener;

        public Malicious(OnTaskCompleted listener){
            this.listener=listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Wrapper doInBackground(String... args) {
            String postURL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=APIKEY";

            String requestBody = "{" +
                    "    \"client\": {" +
                    "      \"clientId\":      \"MWP\"," +
                    "      \"clientVersion\": \"1.5.2\"" +
                    "    }," +
                    "    \"threatInfo\": {" +
                    "      \"threatTypes\":      [\"MALWARE\", \"SOCIAL_ENGINEERING\"]," +
                    "      \"platformTypes\":    [\"ANY_PLATFORM\"]," +
                    "      \"threatEntryTypes\": [\"URL\"]," +
                    "      \"threatEntries\": [" +
                    "        {\"url\": \"" + args[0] + "\"}," +
                    "      ]" +
                    "    }" +
                    "  }";
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, requestBody);
            Request request = new Request.Builder()
                    .url(postURL)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                responseJSON=response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Wrapper w = new Wrapper();
                w.responce = responseJSON;
                w.url = args[0];



                return w;
            }


        @Override
        protected void onPostExecute(Wrapper xml) {
            Log.d("tagg", String.valueOf(xml.responce.length()));

            if(xml.responce.length()==3){
                showtoast("not malicious");
                listener.onTaskCompleted(xml.url);
            }
            else{


                showtoast("malicious");
                Log.d("taggy",responseJSON);

            }

        }

    }




}