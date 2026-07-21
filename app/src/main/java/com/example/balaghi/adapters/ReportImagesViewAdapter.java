package com.example.balaghi.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.balaghi.R;

import java.util.List;

public class ReportImagesViewAdapter extends RecyclerView.Adapter<ReportImagesViewAdapter.ImageViewHolder> {

    private final List<Uri> images;

    public ReportImagesViewAdapter(List<Uri> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_image_square, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = images.get(position);

        holder.btnRemove.setVisibility(View.GONE);

        try {
            holder.imgItem.setImageURI(uri);
        } catch (SecurityException e) {
            holder.imgItem.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        View btnRemove;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgItem);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);
        }
    }
}