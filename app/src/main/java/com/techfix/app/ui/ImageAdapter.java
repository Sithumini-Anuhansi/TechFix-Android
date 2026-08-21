package com.techfix.app.ui;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.model.RepairImage;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {
    private final List<RepairImage> items = new ArrayList<>();

    public void submit(List<RepairImage> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        RepairImage img = items.get(position);
        holder.caption.setText(img.caption == null ? "" : img.caption);
        try {
            holder.image.setImageBitmap(BitmapFactory.decodeFile(img.filePath));
        } catch (Exception ignored) {
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView caption;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgRepair);
            caption = itemView.findViewById(R.id.txtCaption);
        }
    }
}
