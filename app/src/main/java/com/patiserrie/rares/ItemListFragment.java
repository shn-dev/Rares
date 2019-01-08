package com.patiserrie.rares;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.devspark.progressfragment.ProgressFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ItemListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemListFragment extends ProgressFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ItemListFrag";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView mRV;
    private Activity mActivity;


    public ItemListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ItemListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ItemListFragment newInstance(String param1, String param2) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
        setupRecyclerView(getView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mActivity = getActivity();
    }

    private void setupRecyclerView(View v){
        mRV = (RecyclerView)v.findViewById(R.id.itemListRecyclerView);
        mRV.setLayoutManager(new LinearLayoutManager(getContext()));


        ParseQuery<ParseObject> q = new ParseQuery<ParseObject>("Item");
        q.orderByDescending("Item_Date");
        q.setLimit(500);
        //
        q.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    mRV.setAdapter(new RecyclerAdapter(objects));
                    setContentShown(true);
                }
                else{
                    Log.e(TAG, "done: Error getting data" + e.getMessage(), e);
                    //setContentEmpty(true);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //When returned to from the back-stack, fragment resume at onCreateView. Setting the
        //action bar title in onCreate will not be called on resume and the title will still be
        //the item name from the list/chart fragment.
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("All Items");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(null);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list_recyclerview, container, false);
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder{

        private ImageView mImageIconView;
        private TextView mItemName;
        private TextView mItemCurrval;

        public ImageView getmChartIcon() {
            return mChartIcon;
        }

        public ImageView getmListIcon() {
            return mListIcon;
        }

        private ImageView mChartIcon;
        private ImageView mListIcon;
        public LinearLayout getmContainer() {
            return mContainer;
        }

        private LinearLayout mContainer;

        ImageView getmImageIconView() {
            return mImageIconView;
        }

        TextView getmItemName() {
            return mItemName;
        }

        public TextView getmItemCurrval() {
            return mItemCurrval;
        }



        RecyclerViewHolder(View itemView) {
            super(itemView);
            mImageIconView = (ImageView)itemView.findViewById(R.id.listItem_ImageView);
            mItemName = (TextView)itemView.findViewById(R.id.listItem_name);
            mItemCurrval = (TextView)itemView.findViewById(R.id.listItem_currVal);
            mContainer = (LinearLayout)itemView.findViewById(R.id.listItem_Container);
            mChartIcon = (ImageView)itemView.findViewById(R.id.listItem_ChartIcon);
            mListIcon = (ImageView)itemView.findViewById(R.id.listItem_ListIcon);
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{

        CharSequence[] mItems;
        List<ParseObject> mParseObjects;

        RecyclerAdapter(List<ParseObject> parseObjects){
            mItems = Item.getRareItemList();
            mParseObjects = parseObjects;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_item_list_recyclerview_item, parent, false);
            return new RecyclerViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {

            int bgColor = position % 2 == 0 ? Color.rgb(248,249,250) : Color.WHITE;
            holder.getmContainer().setBackgroundColor(bgColor);
            holder.getmItemName().setText(mItems[position]);

            //region Set current Value
            //Code handles setting the current item price in the item list fragment

            //Go through list of parse items to get their last reported value
            StringBuilder currVal = new StringBuilder("");
            for(int i = 0; i<mParseObjects.size();i++){
                if(mItems[position].toString()
                        .equals(mParseObjects.get(i).getString("Item_Name"))){
                    currVal.append(mParseObjects.get(i).getString("Item_Value") + "M");
                    if(Item.getItemProperties(mItems[position].toString()).ismBuyable()){
                        currVal.append(" (" + mParseObjects.get(i).getString("Item_BS") + ")");
                    }
                    break;
                }
            }
            //if no value was found, set the current value to N/A
            if(currVal.toString().isEmpty()){
                currVal.append("N/A");
            }
            holder.getmItemCurrval().setText(currVal.toString());
            //endregion

            Item.setItemSprite(Item.getItemProperties(mItems[position].toString()).getmItemId(),
                    getActivity(),
                    new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    holder.getmImageIconView().setImageBitmap(response);
                }
            });

            holder.getmChartIcon().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AppCompatActivity) mActivity).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainActivityContainer,
                                    ChartFragment.newInstance(mItems[position].toString(), ""))
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            });

            holder.getmListIcon().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AppCompatActivity) mActivity).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainActivityContainer,
                                    ItemValueListFragment.newInstance(mItems[position].toString(), ""))
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return Item.getRareItemList().length;
        }
    }

}
