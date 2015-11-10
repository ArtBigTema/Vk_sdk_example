package com.vk.infographic;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.vktestapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class InfoGraphicFriendActivity extends ActionBarActivity {

    private VKRequest myRequest;
    VKApiUser[] vkApiUsers;
    CharSequence[] vkApiUsersNames;

    private PieChart mPieChart;
    private BarChart mBarChart;
    private BarChart mWBarChart;

    private float[] yData;
    private String[] xData;
    int[] maleAge;
    int[] femaleAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_graphic_friend);
        processRequestIfRequired();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        // outState.putCharSequence("response", getFragment().textView.getText());
        if (myRequest != null) {
            outState.putLong("request", myRequest.registerObject());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CharSequence response = savedInstanceState.getCharSequence("response");


        long requestId = savedInstanceState.getLong("request");
        myRequest = VKRequest.getRegisteredRequest(requestId);
        if (myRequest != null) {
            myRequest.unregisterObject();
            myRequest.setRequestListener(mRequestListener);
        }
    }


    VKRequest.VKRequestListener mRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            //setResponseText(response.json.toString());
            JSONArray jsonArray = null;
            try {
                jsonArray = response.json.getJSONObject("response").getJSONArray("items");

                int length = jsonArray.length();
                vkApiUsers = new VKApiUser[length];
                vkApiUsersNames = new CharSequence[length];
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < length; i++) {
                    VKApiUser user = new VKApiUser(jsonArray.getJSONObject(i));
                    vkApiUsers[i] = user;
                    vkApiUsersNames[i] = user.first_name + " " + user.last_name + " " + ((user.sex == 1) ? "Female" : "Male");
                    sb.append(vkApiUsersNames[i]);
                    sb.append("\n");
                }
                //  Toast.makeText(InfoGraphicFriendActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                //  setResponseText(sb.toString());
                constructPie();
                constructBar();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(VKError error) {
            Toast.makeText(InfoGraphicFriendActivity.this, "err", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
            // you can show progress of the request if you want
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            Toast.makeText(InfoGraphicFriendActivity.this, "err", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //  Toast.makeText(InfoGraphicFriendActivity.this, myRequest.toString(), Toast.LENGTH_SHORT).show();
    }


    private void makeData() {
        int male = 0;
        int female = 0;
        int unknown = 0;

        maleAge = new int[vkApiUsers.length];
        femaleAge = new int[vkApiUsers.length];

        for (VKApiUser user : vkApiUsers) {
            if (user.sex == 0) {
                unknown++;
            }
            if (user.sex == 1) {
                femaleAge[female] = getAge(user.bdate);
                female++;
            }
            if (user.sex == 2) {
                maleAge[male] = getAge(user.bdate);
                male++;
            }
        }
        yData = new float[]{male, female, unknown};
        xData = new String[]{"Male", "Female", "Unknown"};
    }

    private int getAge(String age) {
        int a = 2015;
        if (age.split("\\.").length > 2) {
            a = Integer.parseInt(age.split("\\.")[2]);
        }
        return 2015 - a;
    }

    private void addData() {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < yData.length; i++)
            yVals1.add(new Entry(yData[i], i));

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < xData.length; i++)
            xVals.add(xData[i]);

        // create pie data set
        PieData pdata = null;

        PieDataSet dataSet = new PieDataSet(yVals1, "Gender");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        dataSet.setColors(ColorTemplate.LIBERTY_COLORS);

        // instantiate pie data object now
        pdata = new PieData(xVals, dataSet);
        pdata.setValueFormatter(new PercentFormatter());
        pdata.setValueTextSize(11f);
        pdata.setValueTextColor(Color.GRAY);

        mPieChart.setData(pdata);

        // undo all highlights
        mPieChart.highlightValues(null);
        mPieChart.animateXY(500, 500);
        // update pie chart
        mPieChart.invalidate();
    }


    private void constructPie() {
        mPieChart = new PieChart(this);
        // add pie chart to main layout
        FrameLayout pieLayout = (FrameLayout) findViewById(R.id.fl_pie);
        // FrameLayout pieLayout = null;
        pieLayout.addView(mPieChart);
        pieLayout.setBackgroundColor(Color.parseColor("#55656C"));

        // configure pie chart
        mPieChart.setUsePercentValues(true);
        mPieChart.setDescription("Your friends");

        // enable hole and configure
        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColorTransparent(true);
        mPieChart.setHoleRadius(17);
        mPieChart.setTransparentCircleRadius(10);

        // enable rotation of the chart by touch
        mPieChart.setRotationAngle(0);
        mPieChart.setRotationEnabled(true);

        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                // display msg when value selected
                if (e == null)
                    return;

                Toast.makeText(InfoGraphicFriendActivity.this,
                        xData[e.getXIndex()] + " = " + e.getVal() + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        makeData();
        addData();
    }

    private void constructBar() {
        constructManBar();
        constructWoManBar();
    }

    private void constructWoManBar() {
        mWBarChart = new BarChart(this);

        FrameLayout barWManLayout = (FrameLayout) findViewById(R.id.fl_bar_woman);
        //FrameLayout barManLayout = null;

        barWManLayout.addView(mWBarChart);

        barWManLayout.setBackgroundColor(Color.parseColor("#55656C"));

        BarData data = getBarDataWoMan();
        mWBarChart.setData(data);
        mWBarChart.setDescription("");
        mWBarChart.animateXY(2000, 2000);
        mWBarChart.invalidate();
    }

    private void constructManBar() {
        mBarChart = new BarChart(this);

        FrameLayout barManLayout = (FrameLayout) findViewById(R.id.fl_bar_man);
        //FrameLayout barManLayout = null;

        barManLayout.addView(mBarChart);

        barManLayout.setBackgroundColor(Color.parseColor("#55656C"));

        BarData data = getBarDataMan();
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) Math.floor(v));
            }
        });
        mBarChart.setData(data);
        mBarChart.setDescription("");
        mBarChart.animateXY(2000, 2000);
        mBarChart.invalidate();
    }

    public BarData getBarDataMan() {
        ArrayList<String> xAxis = new ArrayList<>();
        ArrayList<BarEntry> valueSet = new ArrayList<>();

        int[] h = new int[100];
        int j = 0;
        for (int i : maleAge) {
            h[i]++;
        }
        for (int i = 0; i < h.length; i++) {
            if (h[i] > 0) {
                xAxis.add(String.valueOf(i));
                BarEntry v = new BarEntry(h[i], j++); // Jun
                valueSet.add(v);
            }
        }
        xAxis.set(0, "No");//FIXME

        BarDataSet barDataSet = new BarDataSet(valueSet, "Male");
        barDataSet.setColors(ColorTemplate.LIBERTY_COLORS);

        return new BarData(xAxis, barDataSet);
    }

    public BarData getBarDataWoMan() {
        ArrayList<String> xAxis = new ArrayList<>();
        ArrayList<BarEntry> valueSet = new ArrayList<>();

        int[] h = new int[100];
        int j = 0;
        for (int i : femaleAge) {
            h[i]++;
        }
        for (int i = 0; i < h.length; i++) {
            if (h[i] > 0) {
                xAxis.add(String.valueOf(i));
                BarEntry v = new BarEntry(h[i], j++); // Jun
                valueSet.add(v);
            }
        }
        xAxis.set(0, "No");//FIXME

        BarDataSet barDataSet = new BarDataSet(valueSet, "FeMale");
        barDataSet.setColors(ColorTemplate.LIBERTY_COLORS);

        return new BarData(xAxis, barDataSet);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRequest.cancel();
        Log.d(VKSdk.SDK_TAG, "On destroy");
    }
}

