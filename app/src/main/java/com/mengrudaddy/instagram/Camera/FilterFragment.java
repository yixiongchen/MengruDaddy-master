package com.mengrudaddy.instagram.Camera;

/*
FilterFragment.java
This class is activity for showing filters that user can select
 */


import android.content.Intent;
import android.graphics.Bitmap;

import com.mengrudaddy.instagram.Camera.ImageFilter;
import com.mengrudaddy.instagram.R;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mengrudaddy.instagram.Adapter.ThumbNailAdapter;
import com.mengrudaddy.instagram.Interface.FilterListFragmentListener;
import com.mengrudaddy.instagram.utils.BitmapUtils;
import com.mengrudaddy.instagram.utils.SpacesItemDecoration;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends BottomSheetDialogFragment implements FilterListFragmentListener{

    RecyclerView recyclerView;
    ThumbNailAdapter adapter;
    List<ThumbnailItem> nails;
    FilterListFragmentListener listener;
    static FilterFragment instance;

    public static FilterFragment getInstance() {
        if (instance == null){
            instance = new FilterFragment();

        }
        return instance;
    }

    Bitmap bitmap;
    private boolean flag = false;

    private static final String TAG ="FilterFragement:";
    private Handler handler;

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
        Log.d(TAG, "Start:");

        //filters is showed in recycler view
        adapter = new ThumbNailAdapter(nails, this, getActivity());
        recyclerView = (RecyclerView) itemView.findViewById(R.id.filter_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(adapter);

        //retrieve image and convert it to filtered thumbnails
        ImageView new_image = getActivity().findViewById(R.id.new_image);
        if(new_image != null){
            Log.d(TAG, "new image is not null");
            BitmapDrawable drawable = (BitmapDrawable) new_image.getDrawable();
            bitmap = drawable.getBitmap();
        }
        displayFilterNails(bitmap);
        return itemView;
    }

    // display filters in thumbnail layout
    private void displayFilterNails(final Bitmap bitmap) {
        flag = true;
        Runnable run = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "running thread");

                    //thumbImg = BitmapUtils.getBitmapFromAssets(getActivity(), ImageFilter.pic_name,100,100);
                Bitmap thumbImg = Bitmap.createScaledBitmap(bitmap,100,100,false);

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
        Thread thread = new Thread(run);
        thread.start();

    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (listener !=null){
            listener.onFilterSelected(filter);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //displayFilterNails(bitmap);
        Log.d(TAG, "Filter Fragment is resumed");

    }
}
