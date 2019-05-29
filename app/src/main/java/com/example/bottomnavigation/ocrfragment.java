package com.example.bottomnavigation;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ocrfragment extends android.support.v4.app.Fragment implements
        View.OnClickListener,
        View.OnTouchListener,
        OnTaskCompleted {
    private SurfaceView mCameraView;
    private TextView mTextView;
    private CameraSource mCameraSource;
    private static final String TAG = "SecondActivity";
    private static final int requestPermissionID = 101;
    private String s, s1;
    private ArrayList a;
    private String responseJSON;
    ArrayAdapter<String> arrayAdapter;
    Wrapper w = new Wrapper();
    int flag = 1, flag1 = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 100);
        }
        View view = inflater.inflate(R.layout.ocrfrag, viewGroup, false);
        mCameraView = view.findViewById(R.id.surfaceView);
        mTextView = view.findViewById(R.id.text_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
        startCameraSource();
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getActivity().getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getActivity().getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();


            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (flag == 1) {
                        flag = 0;
                        if (items.size() != 0) {

                            mTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (int i = 0; i < items.size(); i++) {
                                        TextBlock item = items.valueAt(i);
                                        stringBuilder.append(item.getValue());
                                        stringBuilder.append("\n");
                                    }
                                    s = stringBuilder.toString();
                                    mTextView.setText(stringBuilder.toString());
                                    a = pullLinks(s);
                                    if (isOnline()) {
                                        while (!a.isEmpty()) {
                                            if (Patterns.WEB_URL.matcher(a.get(0).toString()).matches()) {
                                                if ((!a.get(0).toString().startsWith("https://")) && (!a.get(0).toString().startsWith("http://"))) {
                                                    s1 = "https://" + a.get(0).toString();
                                                }
                                                if (s1.startsWith("http://")) {
                                                    s1 = s1.replaceFirst("^(http|https)://", "https://");
                                                }
                                                arrayAdapter.add(s1);
                                            }
                                            a.remove(0);
                                            if (a.isEmpty()&&!arrayAdapter.isEmpty()) {
                                                showalertdialog(getActivity());
                                            }
                                        }

                                    } else {
                                        showtoast("NO INTERNET");
                                        flag = 1;
                                    }
                                }
                            });
                        }


                        if (flag1 == 0) {
                            flag = 1;
                        }

                    }

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mCameraSource.stop();
    }

    private ArrayList pullLinks(String text) {
        ArrayList<String> links = new ArrayList<>();

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            links.add(urlStr);
        }
        return links;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:
                break;


        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private void showtoast(String s) {
        if(getActivity()!=null){
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();}
    }

    class Malicious123 extends AsyncTask<String, Void, Wrapper> {
        private OnTaskCompleted listener;

        public Malicious123(OnTaskCompleted listener){
            this.listener=listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Wrapper doInBackground(String... args) {
            String postURL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyDGh1PADFcsgJLFBcNe55KNfVwZXVjgK7Q";

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
    public void onTaskCompleted(final String url123) {final Intent intent = new Intent(getActivity(), SecondActivity.class);

        Log.d("url", url123);
        AndroidNetworking.get(url123)
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
                                showDialog(getActivity(),"OCR",msg,intent);}
                        }
                        else {
                            String msg1="url:"+w.url+"\n"+"MALICIOUS\n";
                            showDialog1(getActivity(),"OCR",msg1);
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
    public void showalertdialog(final Activity act){
        flag1=1;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(act);
        builderSingle.setTitle("OCR");
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(arrayAdapter!= null)
                {
                    arrayAdapter.clear();
                }
                flag=1;
                flag1=0;
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                Malicious123 m = new Malicious123(ocrfragment.this);
                m.execute(strName);
                if(arrayAdapter!= null)
                {
                    arrayAdapter.clear();
                }
                flag=1;
                flag1=0;
            }
        });
        if(!arrayAdapter.isEmpty()){
        builderSingle.show();}
    }
    public void showDialog(Activity activity, String title, CharSequence message, final Intent act) {
        flag1=1;
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);

        if (title != null) builder.setTitle(title);

        builder.setMessage(message);
        builder.setPositiveButton("viewurl", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                startActivity(act);
                flag=1;
                flag1=0;
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                flag=1;
                flag1=0;
                dialog.dismiss();


            }
        });

            builder.show();
    }
    public void showDialog1(Activity activity, String title, CharSequence message) {
        flag1=1;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);

        if (title != null) builder.setTitle(title);


        builder.setMessage(message);
        builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                flag=1;
                flag1=0;
                dialog.dismiss();

            }
        });
        builder.show();
    }


}
