package ru.file.manager.recycler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import ru.file.manager.R;
import ru.file.manager.data.Preferences;
import ru.file.manager.utils.FileUtils;
import ru.file.manager.utils.PreferenceUtils;

final class ViewHolder0 extends ViewHolder {
    private TextView name;
    private TextView date;
    private TextView size;

    ViewHolder0(Context context, OnItemClickListener listener, View view) {
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
        date = itemView.findViewById(R.id.list_item_date);
        size = itemView.findViewById(R.id.list_item_size);
    }

    @Override
    protected void bindIcon(File file, Boolean selected) {
            image.setOnClickListener(onActionClickListener);
            image.setOnLongClickListener(onActionLongClickListener);
            if (selected) {
                int color = ContextCompat.getColor(context, R.color.misc_file);
                image.setBackground(getBackground(color));
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_selected);
                DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
                image.setImageDrawable(drawable);
            } else {
                int color = ContextCompat.getColor(context, FileUtils.getColorResource(file));
                image.setBackground(getBackground(color));
                Drawable drawable = ContextCompat.getDrawable(context, FileUtils.getImageResource(file));
                DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
                image.setImageDrawable(drawable);
            }
    }

    @Override
    protected void bindName(File file) {
        name.setText(Preferences.showExtensions() ? FileUtils.getName(file) : file.getName());
    }

    @Override
    protected void bindInfo(File file) {
        date.setText(FileUtils.getLastModified(file));
        size.setText(FileUtils.getSize(context, file));
    }

    private ShapeDrawable getBackground(int color) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        int size = (int) context.getResources().getDimension(R.dimen.avatar_size);
        shapeDrawable.setIntrinsicWidth(size);
        shapeDrawable.setIntrinsicHeight(size);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }
}
