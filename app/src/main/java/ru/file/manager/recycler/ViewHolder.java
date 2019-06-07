package ru.file.manager.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

abstract class ViewHolder extends RecyclerView.ViewHolder {
    final Context context;
    ImageView image;
    View.OnClickListener onActionClickListener;
    View.OnLongClickListener onActionLongClickListener;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    ViewHolder(Context context, OnItemClickListener listener, View view) {
        super(view);
        this.context = context;
        setClickListener(listener);
        loadIcon();
        loadName();
        loadInfo();
    }

    protected abstract void loadIcon();
    protected abstract void loadName();
    protected abstract void loadInfo();
    protected abstract void bindIcon(File file, Boolean selected);
    protected abstract void bindName(File file);
    protected abstract void bindInfo(File file);

    private void setClickListener(final OnItemClickListener listener) {
        this.onActionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemLongClick(ViewHolder.this.getAdapterPosition());
            }
        };

        this.onActionLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return listener.onItemLongClick(ViewHolder.this.getAdapterPosition());
            }
        };

        this.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(ViewHolder.this.getAdapterPosition());
            }
        };

        this.onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return listener.onItemLongClick(ViewHolder.this.getAdapterPosition());
            }
        };
    }

    void setData(final File file, Boolean selected) {
        itemView.setOnClickListener(onClickListener);
        itemView.setOnLongClickListener(onLongClickListener);
        itemView.setSelected(selected);
        bindIcon(file, selected);
        bindName(file);
        bindInfo(file);
    }

    void setVisibility(View view, Boolean visibility) {
        view.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }
}