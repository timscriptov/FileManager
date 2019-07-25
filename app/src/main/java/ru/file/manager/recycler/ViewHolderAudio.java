package ru.file.manager.recycler;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import ru.file.manager.R;
import ru.file.manager.utils.FileUtils;
import ru.file.manager.utils.PreferenceUtils;
import ru.file.manager.data.Preferences;

final class ViewHolderAudio extends ViewHolder {
    private TextView title;
    private TextView artist;
    private TextView album;

    ViewHolderAudio(Context context, OnItemClickListener listener, View view) {
        super(context, listener, view);
    }

    @Override
    protected void loadIcon() {
        image = itemView.findViewById(R.id.list_item_image);
    }

    @Override
    protected void loadName() {
        title = itemView.findViewById(R.id.list_item_title);
    }

    @Override
    protected void loadInfo() {
        artist = itemView.findViewById(R.id.list_item_artist);
        album = itemView.findViewById(R.id.list_item_album);
    }

    @Override
    protected void bindIcon(File file, Boolean selected) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getPath());
            Glide.with(context).load(retriever.getEmbeddedPicture()).into(image);
        } catch (Exception e) {
            image.setImageResource(R.drawable.ic_audio);
        }
    }

    @Override
    protected void bindName(File file) {
        String string = FileUtils.getTitle(file);
        title.setText(string != null && string.isEmpty() ? string : (Preferences.showExtensions() ? FileUtils.getName(file) : file.getName()));
    }

    @Override
    protected void bindInfo(File file) {
        artist.setText(FileUtils.getArtist(file));
        album.setText(FileUtils.getAlbum(file));
    }
}
