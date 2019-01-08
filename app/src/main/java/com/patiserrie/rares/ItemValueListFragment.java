package com.patiserrie.rares;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.devspark.progressfragment.ProgressFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ItemValueListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemValueListFragment extends ProgressFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mItemName;
    private String mParam2;

    private RecyclerView mRV;
    private AppCompatActivity mActivity;

    public ItemValueListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param itemName Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ItemValueListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ItemValueListFragment newInstance(String itemName, String param2) {
        ItemValueListFragment fragment = new ItemValueListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, itemName);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
        setupTitle();
        setupRecyclerView();
    }

    private void setupTitle(){
        mActivity.getSupportActionBar().setTitle(mItemName);
        mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        Item.setItemSprite(Item.getItemProperties(mItemName).getmItemId(), mActivity, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                mActivity.getSupportActionBar().setIcon(
                        new BitmapDrawable(mActivity.getResources(), response));
            }
        });

    }

    private void setupRecyclerView(){

        ParseQuery<ParseObject> q = new ParseQuery<ParseObject>("Item");
        q.setLimit(500);
        q.whereEqualTo("Item_Name", mItemName);
        q.orderByDescending("Item_Date");
        q.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    mRV = (RecyclerView) getView().findViewById(R.id.itemValueList_RecyclerView);
                    mRV.setAdapter(new RecyclerAdapter(objects));
                    mRV.setLayoutManager(new LinearLayoutManager(getContext()));
                    setContentShown(true);
                }
                else{
                    Toast.makeText(mActivity, "Error retrieving data...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItemName = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mActivity = (AppCompatActivity)getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_item_value_list, container, false);

        //region Format recyclerview Header
        TextView currValHeader = (TextView)v.findViewById(R.id.itemValueList_Header_currval);
        if(Item.getItemProperties(mItemName).ismBuyable()){
            currValHeader.setText("Last Sale");
        }
        else{
            currValHeader.setText("Approx. Value");
        }
        //endregion

        return v;
    }

    private class RecyclerHolder extends RecyclerView.ViewHolder{

        private TextView mDateTV;

        public TextView getmDateTV() {
            return mDateTV;
        }

        public TextView getmValueTV() {
            return mValueTV;
        }

        private TextView mValueTV;

        public LinearLayout getmContainer() {
            return mContainer;
        }

        private LinearLayout mContainer;

        public TextView getmChange() {
            return mChange;
        }

        private TextView mChange;

        RecyclerHolder(View itemView) {
            super(itemView);
            mContainer = (LinearLayout)itemView.findViewById(R.id.itemValueList_Container);
            mDateTV = (TextView)itemView.findViewById(R.id.itemValueList_Date);
            mValueTV = (TextView)itemView.findViewById(R.id.itemValueList_Value);
            mChange = (TextView)itemView.findViewById(R.id.itemValueList_Change);

        }
    }
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerHolder>{

        private List<ParseObject> mParseObjects;

        RecyclerAdapter(List<ParseObject> parseObjects){
            mParseObjects = parseObjects;
        }

        @Override
        public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item_value_list_recyclerview_item,
                    parent, false);

            return new RecyclerHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerHolder holder, int position) {

            holder.getmDateTV().setText(mParseObjects.get(position).getString("Item_Date"));
            holder.getmValueTV().setText(mParseObjects.get(position).getString("Item_Value") + "M");

            int bgColor = position % 2 == 0 ? Color.rgb(248,249,250) : Color.WHITE;
            holder.getmContainer().setBackgroundColor(bgColor);

            try{
                double curr = Double.valueOf(mParseObjects.get(position).getString("Item_Value"));
                double old = Double.valueOf(mParseObjects.get(position+ 1).getString("Item_Value"));
                double percentChange = 100*((curr-old)/old);
                DecimalFormat df2 = new DecimalFormat("#.##");
                if(percentChange>0){
                    //green
                    holder.getmChange().setTextColor(Color.rgb(0,100,0));
                }
                else if(percentChange<0){
                    holder.getmChange().setTextColor(Color.RED);
                }
                else{
                    throw new Exception("0% change");
                }
                holder.getmChange().setText(df2.format(percentChange) + "%");
            }catch (Exception ex){ //Captures IndexOutOfBounds exception and also case where percent change is 0
                holder.getmChange().setTextColor(Color.BLACK);
                holder.getmChange().setText("--");
            }

        }

        @Override
        public int getItemCount() {
            return mParseObjects.size();
        }
    }

}
