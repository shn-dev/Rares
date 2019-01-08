package com.patiserrie.rares;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ssepa on 8/29/2017.
 */

class Item {

    private static final String TAG = "Item Class";
    private String mName;
    private String mValue;

    public ItemProperties getmItemProperties() {
        return mItemProperties;
    }

    public void setmItemProperties(ItemProperties mItemProperties) {
        this.mItemProperties = mItemProperties;
    }

    private ItemProperties mItemProperties;

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmValue() {
        return mValue;
    }

    public void setmValue(String mValue) {
        this.mValue = mValue;
    }

    public String getmBS() {
        return mBS;
    }

    public void setmBS(String mBS) {
        this.mBS = mBS;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    private String mBS;
    private String mDate;


    public static final String B = "B";
    public static final String S = "S";

    //LOWERS
    static final String HWEEN_G = "HWEEN_G";
    static final String HWEEN_B = "HWEEN_B";
    static final String HWEEN_R = "HWEEN_R";
    static final String RSH = "RSH";
    static final String BSH = "BSH";

    //EDIBLES
    static final String EASTER_EGG = "EGG";
    static final String PUMPKIN = "PUMPKIN";
    static final String DISK = "DISK";
    static final String WINE = "WINE";

    //XMAS
    static final String MHT = "MHT";
    static final String OHT = "OHT";
    static final String SCYTHE = "SCYTHE";
    static final String WREATH = "WREATH";

    //Traditional uppers
    static final String PHAT_P = "PHAT_P";
    static final String PHAT_Y = "PHAT_Y";
    static final String PHAT_G = "PHAT_G";
    static final String PHAT_R = "PHAT_R";
    static final String PHAT_W = "PHAT_W";
    static final String PHAT_B = "PHAT_B";
    static final String CRACKER = "CRACKER";


    //region Convert ParseObject("Item") object(s) into Item(s). Unused, untested as of 10/24/2017
    /**
     * Thrown when an error occurs generating an Item object.
     */
    static class ItemGenerationException extends Exception{
        ItemGenerationException(String ex){
            super(ex);
        }
    }

    static class ItemList{

        List<Item> mItemList;

        public List<Item> getList() {
            return mItemList;
        }

        /**
         *
         * @return True if there were no objects skipped over during the list generation due to errors.
         */
        public boolean isListComplete() {
            return mSuccess;
        }

        boolean mSuccess;

        ItemList(List<Item> items, boolean success){
            mItemList = items;
            mSuccess = success;
        }
    }

    /**
     * Gets a list of Item objects from a list of ParseObject objects. The number of
     * items in the ItemList object may be less than the number of Parse objects passed into the method
     * should there be an error generating an Item. Check whether the list is full
     * by calling isListComplete() after the list has been generated.
     * @param objList The list of Parse objects.
     * @return An ItemList object containing the list of Items.
     */
    static ItemList getItemListFromParseObjects(List<ParseObject> objList){

        List<Item> itemList = new ArrayList<>(objList.size());
        boolean skipped = false;

        for (ParseObject obj:
             objList) {
            try {
                itemList.add(getItemFromParseObject(obj));
            }
            catch(ItemGenerationException ex){
                Log.e(TAG, "getItemListFromParseObjects: An Item object failed to be generated-" +
                        " skipping item. Details: ", ex);
                skipped=true;
            }
        }
        return new ItemList(itemList, !skipped);
    }

    static Item getItemFromParseObject(ParseObject obj) throws ItemGenerationException{
        if(obj.getClassName().equals("Item")){
            Item item = new Item();
            item.setmBS(obj.getString("Item_BS"));
            item.setmName(obj.getString("Item_Name"));
            item.setmValue(obj.getString("Item_Value"));
            item.setmDate(obj.getString("Item_Date"));
            if(item.getmBS() != null && item.getmDate() != null && item.getmName() != null && item.getmValue() != null){
                if(Arrays.asList(getRareItemList()).contains(item.getmName())){
                    item.setmItemProperties(getItemProperties(item.getmName()));
                    return item;
                }
                else{
                    throw new ItemGenerationException("The item name is invalid.");
                }
            }
            else{
                throw new ItemGenerationException("One or more item properties missing.");
            }
        }
        else{
            throw new ItemGenerationException("Parse object was not an instance of \"item\".");
        }
    }

    //endregion


    static class ItemProperties {
        String mName;
        boolean mBuyable;
        int mItemId;

        ItemProperties(String name, boolean buyable, int itemId) {
            this.setmName(name);
            this.setmBuyable(buyable);
            this.setmItemId(itemId);
        }

        public String getmName() {
            return mName;
        }

        public void setmName(String mName) {
            this.mName = mName;
        }

        public boolean ismBuyable() {
            return mBuyable;
        }

        public void setmBuyable(boolean mBuyable) {
            this.mBuyable = mBuyable;
        }

        public int getmItemId() {
            return mItemId;
        }

        public void setmItemId(int mItemId) {
            this.mItemId = mItemId;
        }


    }

    static CharSequence[] getRareItemList() {

        return new CharSequence[]{
                HWEEN_G,
                HWEEN_B,
                HWEEN_R,
                RSH,
                BSH,
                EASTER_EGG,
                PUMPKIN,
                DISK,
                WINE,
                MHT,
                OHT,
                SCYTHE,
                WREATH,
                PHAT_P,
                PHAT_Y,
                PHAT_G,
                PHAT_R,
                PHAT_W,
                PHAT_B,
                CRACKER
        };
    }

    static ItemProperties getItemProperties(String item) {

        switch (item) {
            case RSH:
                return new ItemProperties(item, true, 1050);
            case HWEEN_G:
                return new ItemProperties(item, true, 1053);
            case HWEEN_B:
                return new ItemProperties(item, true, 1055);
            case HWEEN_R:
                return new ItemProperties(item, true, 1057);
            case EASTER_EGG:
                return new ItemProperties(item, true, 1961);
            case PUMPKIN:
                return new ItemProperties(item, true, 1959);
            case DISK:
                return new ItemProperties(item, true, 981);
            case MHT:
                return new ItemProperties(item, true, 33619);
            case OHT:
                return new ItemProperties(item, true, 33622);
            case SCYTHE:
                return new ItemProperties(item, true, 33625);
            case WINE:
                return new ItemProperties(item, false, 1989);
            case WREATH:
                return new ItemProperties(item, false, 33628);
            case BSH:
                return new ItemProperties(item, false, 30412);
            case PHAT_P:
                return new ItemProperties(item, false, 1046);
            case PHAT_Y:
                return new ItemProperties(item, false, 1040);
            case PHAT_G:
                return new ItemProperties(item, false, 1044);
            case PHAT_R:
                return new ItemProperties(item, false, 1038);
            case PHAT_W:
                return new ItemProperties(item, false, 1048);
            case PHAT_B:
                return new ItemProperties(item, false, 1042);
            case CRACKER:
                return new ItemProperties(item, false, 962);
            default:
                Log.d(TAG, "getItemProperties: Default sprite called...");
                return null;
        }
    }


    static void setItemSprite(int itemId, final Activity activity, Response.Listener<Bitmap> onFinished) {

        final String ENDPOINT = "http://services.runescape.com/m=itemdb_rs/obj_big.gif?id=";

        RequestQueue rq = Volley.newRequestQueue(activity);
        ImageRequest request = new ImageRequest(ENDPOINT + itemId, onFinished
                , 0, 0, ImageView.ScaleType.CENTER_CROP,Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: Could not load image sprite", error);
            }
        });

        rq.add(request);
    }

}