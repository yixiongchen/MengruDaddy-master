package com.mengrudaddy.instagram.Adapter;

/*
ThumbNailAdapter.java
This class is adpter for filter display in recycle view
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mengrudaddy.instagram.Interface.FilterListFragmentListener;
import com.mengrudaddy.instagram.R;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

public class ThumbNailAdapter extends RecyclerView.Adapter<ThumbNailAdapter.MyViewHolder>{
    private static final String TAG = "ThumbNailAdapter";

    private List<ThumbnailItem> thumbnailItems;
    private FilterListFragmentListener listener;
    private Context context;
    private int selectedIndex = 0;

    public ThumbNailAdapter(List<ThumbnailItem> thumbnailItems, FilterListFragmentListener listener, Context context){
        this.thumbnailItems = thumbnailItems;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.thumbnail_item,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int position) {
        final ThumbnailItem thumbnailItem = thumbnailItems.get(position);
        myViewHolder.thumbnail.setImageBitmap(thumbnailItem.image);
        myViewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onFilterSelected(thumbnailItem.filter);
                selectedIndex = position;
                notifyDataSetChanged();
            }
        });

        myViewHolder.filter_name.setText(thumbnailItem.filterName);

        if(selectedIndex == position){
            myViewHolder.filter_name.setTextColor(ContextCompat.getColor(context,R.color.selected_filter));
        }
        else{
            myViewHolder.filter_name.setTextColor(ContextCompat.getColor(context,R.color.normal_filter));
        }
    }

    @Override
    public int getItemCount() {
        return thumbnailItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView thumbnail;
        TextView filter_name;
        public MyViewHolder(View itemView){
            super(itemView);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            filter_name = (TextView)itemView.findViewById(R.id.filter_name);
        }
    }
}
