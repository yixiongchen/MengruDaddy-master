package com.mengrudaddy.instagram;

/*
FilterFragment.java
This class is activity for showing filters that user can select
 */


import android.content.Context;
import android.graphics.Bitmap;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mengrudaddy.instagram.Adapter.ThumbNailAdapter;
import com.mengrudaddy.instagram.Interface.FilterListFragmentListener;
import com.mengrudaddy.instagram.utils.BitmapUtils;
import com.mengrudaddy.instagram.utils.SpacesItemDecoration;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment implements FilterListFragmentListener{

    RecyclerView recyclerView;
    ThumbNailAdapter adapter;
    List<ThumbnailItem> nails;
    FilterListFragmentListener listener;

    public void setListener(FilterListFragmentListener listener1){
        this.listener = listener1;
    }



    public FilterFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_filter, container, false);
        nails = new ArrayList<>();

        //filters is showed in recycler view
        adapter = new ThumbNailAdapter(nails, this, getActivity());
        recyclerView = (RecyclerView) itemView.findViewById(R.id.filter_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(adapter);

        displayFilterNails(null);
        return itemView;
    }

    // display filters in thumbnail layout
    private void displayFilterNails(final Bitmap bitmap) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                Bitmap thumbImg;
                if(bitmap == null){
                    thumbImg = BitmapUtils.getBitmapFromAssets(getActivity(), ImageFilter.pic_name,100,100);
                }
                else {
                    thumbImg = Bitmap.createScaledBitmap(bitmap,100,100,false);
                }
                if(thumbImg == null)
                    return;

                ThumbnailsManager.clearThumbs();
                nails.clear();

                // add normal bitmap fist
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImg;
                thumbnailItem.filterName = "Normal";
                ThumbnailsManager.addThumb(thumbnailItem);

                List<Filter> filters = FilterPack.getFilterPack(getActivity());

                for(Filter filter:filters){
                    ThumbnailItem i = new ThumbnailItem();
                    i.image = thumbImg;
                    i.filter = filter;
                    i.filterName = filter.getName();
                    ThumbnailsManager.addThumb(i);
                }

                nails.addAll(ThumbnailsManager.processThumbs(getActivity()));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };
        new Thread(run).start();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (listener !=null){
            listener.onFilterSelected(filter);
        }
    }
}
