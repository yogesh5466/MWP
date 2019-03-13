package com.example.bottomnavigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import okhttp3.Response;
import org.json.JSONArray;

public class urlfragment extends android.support.v4.app.Fragment implements
        View.OnClickListener,
        View.OnTouchListener{
    Button scan;
    String urls;
    EditText text;
    boolean bool;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.urlfrag, viewGroup, false);
        AndroidNetworking.initialize(getActivity().getApplicationContext());
        scan = view.findViewById(R.id.button);
        text = view.findViewById(R.id.editText);
        scan.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), SecondActivity.class);
        switch(view.getId())
        {
            case R.id.button:
                urls=text.getText().toString();
                if(isOnline()){
                    if(urls != null && !urls.isEmpty() && Patterns.WEB_URL.matcher(urls).matches()) {
                        if((!urls.startsWith("https://"))&&(!urls.startsWith("http://"))){
                            urls="https://"+urls;
                        }
                        AndroidNetworking.get(urls)
                                .addPathParameter("pageNumber", "0")
                                .addQueryParameter("limit", "3")
                                .setPriority(Priority.LOW)
                                .build()
                                .getAsOkHttpResponse(new OkHttpResponseListener() {
                                    @Override
                                    public void onResponse(Response response) {
                                        // do anything with response
                                        if (response.isSuccessful()) {
                                            intent.putExtra("url", urls);
                                            startActivity(intent);
                                        }
                                    }
                                    @Override
                                    public void onError(ANError anError) {
                                        // handle error
                                        Toast.makeText(getActivity(),"404 ERROR!!!",Toast.LENGTH_SHORT).show();
                                        text.getText().clear();
                                    }
                                });

                    }
                    else {
                        showtoast("enter a valid url");
                        text.getText().clear();
                    }
                }
                else {
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
    private void showtoast(String s){
        Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void showDialog(Activity activity, String title, CharSequence message,Intent act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (title != null) builder.setTitle(title);

        builder.setMessage(message);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.show();
    }


}