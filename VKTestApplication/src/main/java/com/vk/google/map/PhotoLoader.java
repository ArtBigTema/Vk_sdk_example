package com.vk.google.map;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoLoader     //   extends Loader<VkPhoto.VkPhotoList>
{/*
    private static final String LOG_TAG = "PhotoLoader";
    private static final int MAX_RESULTS_PER_REQUEST = 1000;
    private long currentTime;
    private double lat;
    private double lon;
    private Context mContext;
    private boolean mIsLoading;
    private boolean mIsNeedInitialize;
    private VkPhoto.VkPhotoList mPhotoResponse;
    private int offset;
    private long rangeTime;
    private int total;
    private int totalIteration;

    public PhotoLoader(Context context) {
        super(context);
        this.mContext = context;
        this.mIsNeedInitialize = true;
    }

    private void initializeLoad() {
        VKRequest vKRequest = new VKRequest("photos.search", new VKParameters(this.getControlParameter(this.param, this.lat, this.lon).parameters()));
        this.mIsNeedInitialize = true;
        vKRequest.executeWithListener(new RequestListener());
    }

    private void loadBackground() {
        this.mIsLoading = true;
        new AsyncTask<Void, Void, Void>() {


            protected  Void doInBackground(Void... arrvoid) {
                new VKRequest("photos.search", new VKParameters(PhotoLoader.this.getSearchParameter(PhotoLoader.this.param, PhotoLoader.this.currentTime - PhotoLoader.this.rangeTime * (long) (1 + PhotoLoader.this.offset), PhotoLoader.this.currentTime - PhotoLoader.this.rangeTime * (long) PhotoLoader.this.offset, PhotoLoader.this.lat, PhotoLoader.this.lon).parameters())).executeWithListener(new RequestListener());
                try {
                    Thread.sleep((long) 333);
                    return null;
                } catch (InterruptedException var2_2) {
                    var2_2.printStackTrace();
                }
                return null;
            }
        }.execute((Object[]) new Void[0]);
    }

    public void deliverResult(VkPhoto.VkPhotoList vkPhotoList) {
        this.mIsLoading = false;
        this.offset = 1 + this.offset;
        super.deliverResult((Object) vkPhotoList);
    }

    public boolean hasMoreResult() {
        if (this.totalIteration > this.offset) {
            return true;
        }
        return false;
    }

    public boolean isLoading() {
        return this.mIsLoading;
    }

    public int offset() {
        return this.offset;
    }

    protected void onForceLoad() {
        if (this.mIsNeedInitialize) {
            this.initializeLoad();
            return;
        }
        this.loadBackground();
    }

    public void setParam(SearchParametersDialog.Param param, double d, double d2) {
        this.param = param;
        this.lat = d;
        this.lon = d2;
        this.totalIteration = 0;
        this.offset = 0;
        this.currentTime = System.currentTimeMillis() / 1000;
        this.mIsNeedInitialize = true;
    }

    public int total() {
        return this.total;
    }

    class RequestListener
            extends VKRequest.VKRequestListener {
        RequestListener() {
        }


        @Override
        public void onComplete(VKResponse vKResponse) {
            VkPhoto.VkPhotoList vkPhotoList = new VkPhoto.VkPhotoList();
            try {
                vkPhotoList.parse(vKResponse.json);
            } catch (JSONException var3_4) {
                var3_4.printStackTrace();
                Logger.e("PhotoLoader", var3_4.getMessage());
            }
            PhotoLoader.this.total = vkPhotoList.getCount();
            if (PhotoLoader.this.mIsNeedInitialize) {
                Log.d((String) "PhotoLoader", (String) ("TOTAL: " + PhotoLoader.this.total));
                PhotoLoader.this.mIsNeedInitialize = false;
                long l = Math.abs((long) (PhotoLoader.access$000((PhotoLoader) PhotoLoader.this).dateTo - PhotoLoader.access$000((PhotoLoader) PhotoLoader.this).dateFrom));
                try {
                    PhotoLoader.this.rangeTime = 1000 * (l / (long) PhotoLoader.this.total);
                } catch (ArithmeticException var13_5) {
                    PhotoLoader.this.rangeTime = l;
                }
                PhotoLoader.this.totalIteration = 1 + (int) (l / PhotoLoader.this.rangeTime);
                PhotoLoader.this.loadBackground();
                return;
            }
            if (vkPhotoList.isEmpty()) return;
            {
                if (vkPhotoList.ids().isEmpty()) {
                    Toast.makeText((Context) PhotoLoader.this.mContext, (CharSequence) "\u0424\u043e\u0442\u043e\u0433\u0440\u0430\u0444\u0438\u0439 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e!", (int) 0).show();
                    PhotoLoader.this.deliverResult(new VkPhoto.VkPhotoList());
                    return;
                }
            }
            HashMap hashMap = new HashMap();
            hashMap.put((Object) "user_ids", (Object) vkPhotoList.ids());
            hashMap.put((Object) "fields", (Object) "photo_50, sex, bdate, relation");
            new VKRequest("users.get", new VKParameters((Map<String, Object>) hashMap), VKRequest.HttpMethod.POST).executeWithListener(new UserInfoRequestListener());
            PhotoLoader.this.mPhotoResponse = vkPhotoList;
        }

        @Override
        public void onError(VKError vKError) {
            super.onError(vKError);
            Log.e((String) "PhotoLoader", (String) vKError.toString());
            Toast.makeText((Context) PhotoLoader.this.mContext, (CharSequence) "\u0417\u0430\u043f\u0440\u043e\u0441 \u043a API \u0412\u043a\u043e\u043d\u0442\u0430\u043a\u0442\u0435 \u0432\u0435\u0440\u043d\u0443\u043b \u043e\u0448\u0438\u0431\u043a\u0443", (int) 0).show();
        }
    }

    class UserInfoRequestListener
            extends VKRequest.VKRequestListener {
        UserInfoRequestListener() {
        }

        @Override
        public void onComplete(VKResponse vKResponse) {
            List<UserInfo> list = ((com.dchuvasov.foreveralone.entity.UserInfo$Parser) new com.google.gson.Gson().fromJson((String) vKResponse.json.toString(), (java.lang.Class) com.dchuvasov.foreveralone.entity.UserInfo$Parser.class)).response;
            PhotoLoader.this.mPhotoResponse.setUserInfoList(list);
            VkPhoto.VkPhotoList vkPhotoList = PhotoLoader.this.mPhotoResponse.getPhotoByParam(PhotoLoader.this.param);
            PhotoLoader.this.deliverResult(vkPhotoList);
        }

        @Override
        public void onError(VKError vKError) {
            super.onError(vKError);
            Log.e((String) "PhotoLoader", (String) vKError.toString());
            Toast.makeText((Context) PhotoLoader.this.mContext, (CharSequence) "\u0417\u0430\u043f\u0440\u043e\u0441 \u043a API \u0412\u043a\u043e\u043d\u0442\u0430\u043a\u0442\u0435 \u0432\u0435\u0440\u043d\u0443\u043b \u043e\u0448\u0438\u0431\u043a\u0443", (int) 0).show();
        }
    }
*/
}

