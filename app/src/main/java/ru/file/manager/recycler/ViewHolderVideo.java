package ru.file.manager.recycler;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.io.File;
import ru.file.manager.R;
import ru.file.manager.data.Preferences;
import ru.file.manager.utils.FileUtils;

final class ViewHolderVideo extends ViewHolder {
    private TextView name;
    private TextView duration;

    ViewHolderVideo(Context context, OnItemClickListener listener, View view) {
        super(context, listener, view);
    }

    @Override
    protected void loadIcon() {
        image = itemView.findViewById(R.id.list_item_image);
    }

    @Override
    protected void loadName() {
        name = itemView.findViewById(R.id.list_item_name);
    }

    @Override
    protected void loadInfo() {
        duration = itemView.findViewById(R.id.list_item_duration);
    }

    @Override
    protected void bindIcon(File file, Boolean selected) {
        Glide.with(context).load(file).into(image);
    }

    @Override
    protected void bindName(File file) {
        name.setText(Preferences.showExtensions() ? FileUtils.getName(file) : file.getName());
    }

    @Override
    protected void bindInfo(File file) {
        duration.setText(FileUtils.getDuration(file));
    }
}
