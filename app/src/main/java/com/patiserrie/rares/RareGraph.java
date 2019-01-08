package com.patiserrie.rares;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by ssepa on 8/29/2017.
 */

abstract class Graph<X,Y,Z> extends AsyncTask<X,Y,Z>{

    Graph(){
        super();
    }

    Graph(IOnFinished completionListener, AppCompatActivity activity, String item){
        setCompletionListener(completionListener);
        mActivity = activity;
        mItem = item;
    }

    interface IOnFinished{
        void graphLoadingCompleted();
    }
    protected IOnFinished listener;
    void setCompletionListener(IOnFinished listener){
        this.listener = listener;
    }
    IOnFinished getCompletionListener(){
        return listener;
    }

    protected LineChart mChart;
    protected AppCompatActivity mActivity;
    protected ChartFragment mChartFragment;
    protected String mItem;
    protected final String DISPLAY_PRICES_PER_DATUM = "DISPLAY_PRICES_PER_DATUM";
    protected final String DISPLAY_LEGEND = "DISPLAY_LEGEND";

    /**
     * Calls the appropriate methods to make
     * the graph visually appealing.
     * @param item
     */
    void setupGraph(String item){

        mActivity.getSupportActionBar().setTitle("RARES - " + MainActivity.DEFAULT_GRAPH_ITEM);

        //set up graph... then load graph

        mChart = (LineChart) mChartFragment.getmChartView().findViewById(R.id.mainChart);
        //mChart.setOnChartValueSelectedListener(activity);
        mChart.setDrawGridBackground(false);

        mChart.setNoDataText("No data was found for \"" + item + "\".");

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        //Following will display the associated x/y value of the touched point.
        /* mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(mActivity, e.getY() + "M : " + new Date((long)e.getX()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        }); */


        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(315); //rotated 45 degrees seven times... label looks like "/"
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date d = new Date((long) value);
                return d.toString();
            }
        });


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return value + "M";
            }
        });


        mChart.getAxisRight().setEnabled(false);


        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
        setData(item);


//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);


        //display legend if preferred
        mChart.getLegend()
                .setEnabled(PreferenceManager
                        .getDefaultSharedPreferences(mActivity)
                        .getBoolean(DISPLAY_LEGEND, true)
                );

        // // dont forget to refresh the drawing
        mChart.invalidate();
    }

    /**
     * Handles the binding of data obtained in doInBackground to the graph, including organizing the data into
     * datasets and formatting how their trend-lines look.
     * @param item
     */
    abstract void setData(String item);

    @Override
    protected void onPostExecute(Z z) {
        if(listener != null) {
            listener.graphLoadingCompleted();
        }
        setupGraph(mItem);
        mChartFragment.setContentShown(true);
    }
}

/**
 * params[0] = item name
 * params[1] = AppCompatActivity (MainActivity)
 */
class RareGraph extends Graph<Object, Void, Void> {

    private AppCompatActivity mActivity;
    private List<Entry> mBuyEntryValues, mSellEntryValues;
    private String mItem;

    private final String INSTANT_BUY_COLOR_KEY = "INSTANT_BUY_COLOR";
    private final String INSTANT_SELL_COLOR_KEY = "INSTANT_SELL_COLOR";
    private final String NON_GE_BUYABLE_COLOR_KEY = "NON_GE_BUYABLE_COLOR";
    private final String DISPLAY_PRICES_PER_DATUM = "DISPLAY_PRICES_PER_DATUM";
    private final String DISPLAY_LEGEND = "DISPLAY_LEGEND";

    private static final String TAG = "GRAPH CLASS";

    @Override
    void setData(String item) {

        //region get preferred colors

        int INSTANT_BUY_COLOR =
                PreferenceManager.getDefaultSharedPreferences(mActivity).getInt(INSTANT_BUY_COLOR_KEY,
                        ContextCompat.getColor(mActivity, R.color.colorPrimary));
        int INSTANT_SELL_COLOR =
                PreferenceManager.getDefaultSharedPreferences(mActivity).getInt(INSTANT_SELL_COLOR_KEY,
                        ContextCompat.getColor(mActivity, R.color.colorAccent));
        int NON_GE_BUYABLE_COLOR =
                PreferenceManager.getDefaultSharedPreferences(mActivity).getInt(NON_GE_BUYABLE_COLOR_KEY,
                        ContextCompat.getColor(mActivity, R.color.colorPrimary));
        //endregion

        LineDataSet buyLineDataSet, sellLineDataSet;

        //Clear previous chart values.
        if (mChart.getData() != null) {
            mChart.getData().clearValues();
            mChart.invalidate();
        }
        //Populate chart if any data exists
        if (mBuyEntryValues.size() > 0 || mSellEntryValues.size() > 0) {

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();


            if (mBuyEntryValues.size() > 0) {

                String dataSet1Name = Item.getItemProperties(item).ismBuyable() ? "Instant Buy" : "Estimated Value";

                buyLineDataSet = new LineDataSet(mBuyEntryValues, dataSet1Name);

                buyLineDataSet.setDrawIcons(false);
                buyLineDataSet.setLineWidth(1f);
                buyLineDataSet.setCircleRadius(3f);
                buyLineDataSet.setDrawCircleHole(false);
                buyLineDataSet.setValueTextSize(9f);
                buyLineDataSet.setDrawFilled(false);

                //region Draw values above instant buy or non-ge-buyables data if preferred
                if (!PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(DISPLAY_PRICES_PER_DATUM, true)) {
                    buyLineDataSet.setDrawValues(false);
                }
                //endregion


                if (!Item.getItemProperties(item).ismBuyable()) {
                    buyLineDataSet.setCircleColor(NON_GE_BUYABLE_COLOR);
                    buyLineDataSet.setColor(NON_GE_BUYABLE_COLOR);
                    buyLineDataSet.setValueTextColor(NON_GE_BUYABLE_COLOR);
                    buyLineDataSet.setFillColor(NON_GE_BUYABLE_COLOR);
                } else {
                    buyLineDataSet.setColor(INSTANT_BUY_COLOR);
                    buyLineDataSet.setCircleColor(INSTANT_BUY_COLOR);
                    buyLineDataSet.setValueTextColor(INSTANT_BUY_COLOR);
                    buyLineDataSet.setFillColor(INSTANT_BUY_COLOR);
                }

                dataSets.add(buyLineDataSet); // add the datasets
            }
            if (mSellEntryValues.size() > 0) {
                sellLineDataSet = new LineDataSet(mSellEntryValues, "Instant Sell");

                //region Draw values above instant sell data if preferred
                if (!PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(DISPLAY_PRICES_PER_DATUM, true)) {
                    sellLineDataSet.setDrawValues(false);
                }
                //endregion

                sellLineDataSet.setDrawIcons(false);

                // set the line to be drawn like this "- - - - - -"
                sellLineDataSet.setColor(INSTANT_SELL_COLOR);
                sellLineDataSet.setCircleColor(INSTANT_SELL_COLOR);
                sellLineDataSet.setLineWidth(1f);
                sellLineDataSet.setCircleRadius(3f);
                sellLineDataSet.setDrawCircleHole(false);
                sellLineDataSet.setValueTextSize(9f);
                sellLineDataSet.setValueTextColor(INSTANT_SELL_COLOR);
                sellLineDataSet.setDrawFilled(false);


                sellLineDataSet.setFillColor(INSTANT_SELL_COLOR);

                dataSets.add(sellLineDataSet); // add the datasets
            }

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }

        //Uncomment to add notification toast if no data was found from parse.
        /*
        if(!(mBuyEntryValues.size()>0 || mSellEntryValues.size() >0)){
            Toast.makeText(mActivity, "No data found for " + item + "...", Toast.LENGTH_SHORT).show();
        }
        */

        mActivity.getSupportActionBar().setTitle(item);
        mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        Item.setItemSprite(Item.getItemProperties(item).getmItemId(), mActivity, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                mActivity.getSupportActionBar().setIcon(
                        new BitmapDrawable(mActivity.getResources(), response));
            }
        });
    }

    @Override
    protected Void doInBackground(Object... params) {

        mItem = (String) params[0];
        mActivity = (AppCompatActivity) params[1];
        mChartFragment = (ChartFragment) params[2];

        mBuyEntryValues = new ArrayList<>();
        mSellEntryValues = new ArrayList<>();

        ParseQuery<ParseObject> buyValsQuery = new ParseQuery<ParseObject>("Item");
        ParseQuery<ParseObject> sellValsQuery = new ParseQuery<ParseObject>("Item");

        int graphTimeSpan = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(mActivity).getString("GRAPH_TIMESPAN", "-1"));
        if(!(graphTimeSpan == -1)){
            //86400000L = number of milliseconds in a day
            java.util.Date d = new java.util.Date(DateTime.now().getMillis() - 86400000L*(long)graphTimeSpan);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
            Log.d(TAG, "doInBackground: Retrieving graph points greater than " + dtf.print(d.getTime()));
            buyValsQuery.whereGreaterThan("Item_Date", dtf.print(d.getTime()));
            sellValsQuery.whereGreaterThan("Item_Date", dtf.print(d.getTime()));
        }

        buyValsQuery.whereEqualTo("Item_BS", "B");
        buyValsQuery.whereEqualTo("Item_Name", mItem);
        buyValsQuery.orderByAscending("Item_Date");
        sellValsQuery.whereEqualTo("Item_BS", "S");
        sellValsQuery.whereEqualTo("Item_Name", mItem);
        sellValsQuery.orderByAscending("Item_Date");

        try {
            List<ParseObject> mBuyValues, mSellValues;
            mBuyValues = buyValsQuery.find();
            mSellValues = sellValsQuery.find();

            for (ParseObject val : mBuyValues) {
                Date d = Date.valueOf(val.getString("Item_Date"));
                mBuyEntryValues.add(new Entry(Date.valueOf(val.getString("Item_Date")).getTime(), Float.parseFloat(val.getString("Item_Value"))));
            }
            for (ParseObject val : mSellValues) {
                Date d = Date.valueOf(val.getString("Item_Date"));
                mSellEntryValues.add(new Entry(Date.valueOf(val.getString("Item_Date")).getTime(), Float.parseFloat(val.getString("Item_Value"))));
            }

            return null;
        } catch (ParseException ex) {
            return null;
        }
    }
}
