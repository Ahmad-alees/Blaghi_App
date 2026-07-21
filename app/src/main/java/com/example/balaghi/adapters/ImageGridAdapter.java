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

public class ImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_ADD = 2;

    private final List<Uri> images;
    private final int maxImages;
    private final Listener listener;

    public interface Listener {
        void onAddClick();
        void onRemoveClick(int position);
    }

    public ImageGridAdapter(List<Uri> images, int maxImages, Listener listener) {
        this.images = images;
        this.maxImages = maxImages;
        this.listener = listener;
    }

    private boolean showAddSlot() {
        return images.size() < maxImages;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < images.size()) ? TYPE_IMAGE : TYPE_ADD;
    }

    @Override
    public int getItemCount() {
        return images.size() + (showAddSlot() ? 1 : 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_IMAGE) {
            return new ImageViewHolder(inflater.inflate(R.layout.activity_item_image_square, parent, false));
        } else {
            return new AddViewHolder(inflater.inflate(R.layout.activity_item_image_placeholder, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ImageViewHolder h = (ImageViewHolder) holder;
            h.imgItem.setImageURI(images.get(position));
            h.btnRemove.setOnClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) listener.onRemoveClick(pos);
            });
        } else {
            holder.itemView.setOnClickListener(v -> listener.onAddClick());
        }
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

    static class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(@NonNull View itemView) { super(itemView); }
    }
}