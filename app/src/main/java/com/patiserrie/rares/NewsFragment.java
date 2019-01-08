package com.patiserrie.rares;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devspark.progressfragment.ProgressFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends ProgressFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "newsfragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView mRV;


    public NewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
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
        ParseQuery<ParseObject> q = new ParseQuery<>("News_Item");
        q.orderByDescending("createdAt");
        q.selectKeys(Arrays.asList("createdAt", "Message"));
        q.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size() > 0){
                    mRV = (RecyclerView)getView().findViewById(R.id.newsRecyclerView);
                    mRV.setLayoutManager(new LinearLayoutManager(getContext()));
                    mRV.setAdapter(new RecyclerAdapter(objects));
                    setContentShown(true);
                }
                else{
                    setContentEmpty(true);
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("News");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    private class RecyclerHolder extends RecyclerView.ViewHolder{

        private TextView mNewsDateTV;

        TextView getmNewsDateTV() {
            return mNewsDateTV;
        }

        public void setmNewsDateTV(TextView mNewsDateTV) {
            this.mNewsDateTV = mNewsDateTV;
        }

        TextView getmNewsMessageTV() {
            return mNewsMessageTV;
        }

        public void setmNewsMessageTV(TextView mNewsMessageTV) {
            this.mNewsMessageTV = mNewsMessageTV;
        }

        private TextView mNewsMessageTV;

        LinearLayout getmLL() {
            return mLL;
        }

        void setmLL(LinearLayout mLL) {
            this.mLL = mLL;
        }

        private LinearLayout mLL;

        RecyclerHolder(View itemView) {
            super(itemView);
            mNewsDateTV = (TextView)itemView.findViewById(R.id.newsDate);
            mNewsMessageTV = (TextView)itemView.findViewById(R.id.newsMessage);
            mLL = (LinearLayout)itemView.findViewById(R.id.newsItem_Container);
        }
    }
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerHolder>{

        List<ParseObject> mData;
        RecyclerAdapter(List<ParseObject> data){
            mData = data;
        }

        @Override
        public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_news_recyclerview_item, parent, false);
            return new RecyclerHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerHolder holder, int position) {

            int bgColor = position % 2 == 0 ? Color.rgb(248,249,250) : Color.WHITE;
            holder.getmLL().setBackgroundColor(bgColor);

            ParseObject obj = mData.get(position);
            holder.getmNewsDateTV().setText(obj.getCreatedAt().toString());
            holder.getmNewsMessageTV().setText(obj.getString("Message"));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

}
