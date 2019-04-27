package com.example.bottomnavigation;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.zxing.Result;

import java.io.IOException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class qrcodefragment extends android.support.v4.app.Fragment implements
        View.OnClickListener,
        View.OnTouchListener,
        ZXingScannerView.ResultHandler,
        OnTaskCompleted{
    private ZXingScannerView mScannerView;
    private ImageButton imageButton,imageButton1;
    private String urls;
    private String responseJSON;
    private int flag=1;
    Wrapper w = new Wrapper();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qrfrag, viewGroup, false);
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 100);
        }
        mScannerView = view.findViewById(R.id.scanner);
        mScannerView.setAutoFocus(true);
        imageButton = view.findViewById(R.id.imageButton);
        imageButton1 = view.findViewById(R.id.imageButton1);
        imageButton.setOnClickListener(this);
        imageButton1.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageButton:
                imageButton.setVisibility(View.INVISIBLE);
                imageButton1.setVisibility(View.VISIBLE);
                mScannerView.setFlash(true);
             break;
            case R.id.imageButton1:
                imageButton1.setVisibility(View.INVISIBLE);
                imageButton.setVisibility(View.VISIBLE);
                mScannerView.setFlash(false);
             break;
             default:
                 break;

        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void handleResult(Result result) {
        urls = result.getText();

        if(flag==1) {
            flag=0;
            if (isOnline()) {
                if (urls != null && !urls.isEmpty() && Patterns.WEB_URL.matcher(urls).matches()) {
                    if ((!urls.startsWith("https://")) && (!urls.startsWith("http://"))) {
                        urls = "https://" + urls;
                    }
                    if (urls.startsWith("http://")) {
                        urls = urls.replaceFirst("^(http|https)://", "https://");
                    }
                    Malicious1234 m = new Malicious1234(this);
                    m.execute(urls);

                } else {
                    showtoast("The QRCODE is not a url");
                    flag=1;
                }

            } else {
                showtoast("No Internet Connection");
                flag=1;
            }
        }mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
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
    private void showtoast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onTaskCompleted(String url) {
        final Intent intent = new Intent(getActivity(), SecondActivity.class);
        Log.d("url", url);
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
                            if(w.responce.length()==3){
                                intent.putExtra("url", w.url);
                                String msg="url:"+w.url+"\n"+"NOT MALICIOUS\n";
                                showDialog(getActivity(),"QRCODE",msg,intent);}
                            }
                            else {
                            String msg1="url:"+w.url+"\n"+"MALICIOUS\n";
                            showDialog1(getActivity(),"QRCODE",msg1);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // handle error
                        Log.d("error", "onError: "+anError);
                        Toast.makeText(getActivity(), "404 ERROR!!!", Toast.LENGTH_SHORT).show();
                        flag=1;
                    }
                });


    }
    class Malicious1234 extends AsyncTask<String, Void, Wrapper> {
        private OnTaskCompleted listener;

        public Malicious1234(OnTaskCompleted listener){
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


            w.responce = responseJSON;
            w.url = args[0];



            return w;
        }


        @Override
        protected void onPostExecute(Wrapper xml) {
            listener.onTaskCompleted(xml.url);
            Log.d("responce", xml.responce);
        }

    }
    public void showDialog(Activity activity, String title, CharSequence message, final Intent act) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (title != null) builder.setTitle(title);

        builder.setMessage(message);
        builder.setPositiveButton("viewurl", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                flag=1;
                startActivity(act);
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                flag=1;
                mScannerView.resumeCameraPreview(qrcodefragment.this);
            }
        });
        builder.show();
    }
    public void showDialog1(Activity activity, String title, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (title != null) builder.setTitle(title);

        builder.setMessage(message);
        builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                flag=1;
                mScannerView.resumeCameraPreview(qrcodefragment.this);

            }
        });
        builder.show();
    }
}
