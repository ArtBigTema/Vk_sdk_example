package com.vk.vktestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.vk.FriendAdapter;
import com.vk.Sub.Profile;
import com.vk.infographic.InfoGraphicFriendActivity;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Артем on 27.10.2015.
 */
public class FriendActivity extends ActionBarActivity {

    private VKRequest myRequest;
    CharSequence[] vkApiUsersNames;

    private static final String FRAGMENT_TAG = "response_view";
    FloatingActionButton fab;
    List<Profile> profileFriends;
    RecyclerView rec;
    ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        profileFriends = new ArrayList<Profile>();
        rec = (RecyclerView) findViewById(R.id.rv_friend);

        rec.setHasFixedSize(true);
        rec.setLayoutManager(new LinearLayoutManager(this));
        rec.setItemAnimator(new DefaultItemAnimator());


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(rec);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FriendActivity.this, InfoGraphicFriendActivity.class);
                i.putExtra("request", myRequest.registerObject());
                startActivity(i);
            }
        });
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.pb_friend);
        progressBar.show();

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            processRequestIfRequired();
        }
    }


    private void processRequestIfRequired() {
        VKRequest request = null;

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("request")) {
            long requestId = getIntent().getExtras().getLong("request");
            request = VKRequest.getRegisteredRequest(requestId);
            if (request != null)
                request.unregisterObject();
        }

        if (request == null) return;
        myRequest = request;
        request.executeWithListener(mRequestListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("response", myRequest.toString());
        if (myRequest != null) {
            outState.putLong("request", myRequest.registerObject());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CharSequence response = savedInstanceState.getCharSequence("response");
        if (response != null) {
            //  textView.setText(response);
        }

        long requestId = savedInstanceState.getLong("request");
        myRequest = VKRequest.getRegisteredRequest(requestId);
        if (myRequest != null) {
            myRequest.unregisterObject();
            myRequest.setRequestListener(mRequestListener);
        }
    }

    protected void setResponseText(String text) {
        rec.setAdapter(new FriendAdapter(profileFriends, R.layout.item_friend));
        fab.attachToRecyclerView(rec);
    }

    public VKRequestListener mRequestListener = new VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            //setResponseText(response.json.toString());
            progressBar.hide();
            JSONArray jsonArray = null;
            try {
                jsonArray = response.json.getJSONObject("response").getJSONArray("items");

                int length = jsonArray.length();
                // vkApiUsers = new VKApiUser[length];
                vkApiUsersNames = new CharSequence[length];
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < length; i++) {
                    VKApiUser user = new VKApiUser(jsonArray.getJSONObject(i));
                    vkApiUsersNames[i] = user.first_name + " " + user.last_name;
                    // + " " + ((user.sex == 1) ? "Female" : "Male");
                    sb.append(vkApiUsersNames[i]);
                    sb.append("\n");
                    profileFriends.add(new Profile(vkApiUsersNames[i].toString(), user.photo_50, user.online));
                }
                setResponseText(sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(VKError error) {
            setResponseText(error.toString());
            progressBar.hide();
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
            progressBar.show();  // you can show progress of the request if you want
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            progressBar.hide();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRequest.cancel();
        Log.d(VKSdk.SDK_TAG, "On destroy");
    }
}