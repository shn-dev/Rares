package com.patiserrie.rares;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssepa on 9/6/2017.
 */


/**
 * param[0] = IOnObtainItemsTaskFinished listener
 * param[1] = item name
 * param[2] = BS (optional)
 */
class RetrieveItemsAsync extends AsyncTask<Object, Void, List<RetrieveItemsAsync.ValuePoint>>{

    private static final String TAG = "getting item values";

    private String mItem, mBS;
    interface IOnObtainItemsTaskFinished{
        void onCompleted(List<ValuePoint> values);
    }
    private IOnObtainItemsTaskFinished mOnCompleted;

    class ValuePoint {

        String getmValue() {
            return mValue;
        }

        void setmValue(String mValue) {
            this.mValue = mValue;
        }

        String getmDate() {
            return mDate;
        }

        void setmDate(String mDate) {
            this.mDate = mDate;
        }

        private String mValue;
        private String mDate;

    }

    @Override
    protected List<ValuePoint> doInBackground(Object... params) {
        mOnCompleted = (IOnObtainItemsTaskFinished)params[0];
        mItem = (String)params[1];
        if(params.length > 2){
            mBS = (String)params[2];
            return getItemValuePoints(mItem, mBS);
        }
        else if(params.length == 2){
            return getItemValuePoints(mItem);
        }
        else{
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<ValuePoint> valuePoints) {
        mOnCompleted.onCompleted(valuePoints);
    }

    private List<ValuePoint> getItemValuePoints(String item) {

        List<ValuePoint> values = new ArrayList<>();

        try {
            ParseQuery<ParseObject> q = new ParseQuery("Item");
            q.whereEqualTo("Item_Name", item);
            q.orderByAscending("Item_Date");

            for (ParseObject obj:
                    q.find()) {
                ValuePoint p = new ValuePoint();
                p.setmDate(obj.getString("Item_Date"));
                p.setmValue(obj.getString("Item_Value"));
                values.add(p);
            }
            return values;
        }
        catch(ParseException ex){
            //Error retrieving query, handle as necessary!
            Log.e(TAG, "getItemValuePoints: ", ex);
            return null;
        }
    }
    private List<ValuePoint> getItemValuePoints(String item, String BS) {

        List<ValuePoint> values = new ArrayList<>();

        try {
            ParseQuery<ParseObject> q = new ParseQuery("Item");
            q.whereEqualTo("Item_Name", item);
            q.whereEqualTo("Item_BS", BS);
            q.orderByAscending("Item_Date");

            for (ParseObject obj:
                    q.find()) {
                ValuePoint p = new ValuePoint();
                p.setmDate(obj.getString("Item_Date"));
                p.setmValue(obj.getString("Item_Value"));
                values.add(p);
            }
            return values;
        }
        catch(ParseException ex){
            //Error retrieving query, handle as necessary!
            Log.e(TAG, "getItemValuePoints: ", ex);
            return null;
        }
    }
}

/**
 * params[0] = Item
 * params[1] = IOnFinishedListener
 */
class AddItemAsyncTask extends  AsyncTask<Object, Void, Boolean>{

    private static final String TAG = "Add item async";
    interface IOnFinishedListener{
        void onCompleted(boolean success);
    }

    private IOnFinishedListener mListener;
    private Item mItem;
    @Override
    protected Boolean doInBackground(Object... params) {
        mItem = ((Item)params[0]);
        mListener = (IOnFinishedListener)params[1];

        ParseObject obj = new ParseObject("Item");
        obj.put("Item_Name", mItem.getmName());
        obj.put("Item_Date", mItem.getmDate());
        obj.put("Item_Value", mItem.getmValue());
        obj.put("Item_BS", mItem.getmBS());
        try {
            obj.save();
            return true;
        }
        catch(ParseException ex){
            Log.e(TAG, "doInBackground: ", ex);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        mListener.onCompleted(success);
    }
}


/**
 * params[0] = IOnNewsItemObtained
 * params[1] = AppCompatActivity
 * params[2] = boolean (display first most recent only)
 * Returns a String array with the most recent news items.
 */
class GetNewsItem extends AsyncTask<Object, Void, String[]>{

    interface IOnNewsItemObtained{
        void onNewsObtained(String[] newsMessage);
    }

    IOnNewsItemObtained mObtainedListener;
    AppCompatActivity mActivity;


    @Override
    protected String[] doInBackground(Object... params) {


        mObtainedListener = (IOnNewsItemObtained)params[0];
        mActivity = (AppCompatActivity)params[1];
        boolean displayMostRecent = (boolean)params[2];

        try{
            ParseQuery<ParseObject> q= new ParseQuery<>("News_Item");
            q.orderByDescending("createdAt");
            if(displayMostRecent){
                return new String[]{q.getFirst().getString("Message")};
            }
            else{
                List<ParseObject> result = q.find();
                if(result != null && result.size() > 0){
                    String[] resultArr = new String[result.size()];
                    int iteration = 0;
                    for (ParseObject obj:
                         result) {
                        resultArr[iteration] = obj.getString("Message");
                        iteration++;
                    }
                    return resultArr;
                }
                else{
                    throw new ParseException(ParseException.COMMAND_UNAVAILABLE,
                            "No news items could be found in Parse...");
                }
            }
        }catch(ParseException ex){
            return new String[]{"Could not load recent news"};
        }
    }

    @Override
    protected void onPostExecute(String[] newsMsg) {
        mObtainedListener.onNewsObtained(newsMsg);
    }
}
