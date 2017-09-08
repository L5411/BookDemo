package com.l_5411.bookdemo.chapter_12;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.chapter_12.imageloader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by L_5411 on 2017/9/7.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {

    private Context context;
    private List<String> data;

    public ImageAdapter(Context context, List<String> data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.image);
        final String uri = data.get(position);

        ImageLoader.build(context)
                .bindBitmap(uri, imageView);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }
}
