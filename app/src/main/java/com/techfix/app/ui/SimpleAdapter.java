package com.techfix.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;

import java.util.ArrayList;
import java.util.List;

public class SimpleAdapter<T> extends RecyclerView.Adapter<SimpleAdapter.Holder> {
    public interface Binder<T> {
        void bind(T item, TextView title, TextView subtitle, TextView meta);
    }

    public interface Click<T> {
        void onClick(T item);
    }

    private final List<T> items = new ArrayList<>();
    private final Binder<T> binder;
    private final Click<T> click;

    public SimpleAdapter(Binder<T> binder, Click<T> click) {
        this.binder = binder;
        this.click = click;
    }

    public void submit(List<T> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        T item = items.get(position);
        binder.bind(item, holder.title, holder.subtitle, holder.meta);
        holder.itemView.setOnClickListener(v -> {
            if (click != null) {
                click.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView meta;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            subtitle = itemView.findViewById(R.id.txtSubtitle);
            meta = itemView.findViewById(R.id.txtMeta);
        }
    }
}
