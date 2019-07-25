package ru.file.manager.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;

import java.io.File;

import ru.file.manager.utils.FileUtils;
import ru.file.manager.utils.PreferenceUtils;
import ru.file.manager.data.Preferences;

class Callback extends SortedListAdapterCallback<File> {
    private String sortCriteria;

    Callback(Context context, RecyclerView.Adapter adapter) {
        super(adapter);
        this.sortCriteria = Preferences.sortCriteria();
    }

    @Override
    public int compare(File file1, File file2) {
        boolean isDirectory1 = file1.isDirectory();
        boolean isDirectory2 = file2.isDirectory();
        if (isDirectory1 != isDirectory2) return isDirectory1 ? -1 : +1;
        switch (sortCriteria) {
            case "name":
                return FileUtils.compareName(file1, file2);
            case "date":
                return FileUtils.compareDate(file1, file2);
            case "size":
                return FileUtils.compareSize(file1, file2);
            default:
                return 0;
        }
    }

    @Override
    public boolean areContentsTheSame(File oldItem, File newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areItemsTheSame(File item1, File item2) {
        return item1.equals(item2);
    }

    boolean update(String sortCriteria) {
        if (sortCriteria == this.sortCriteria) return false;
        this.sortCriteria = sortCriteria;
        return true;
    }
}
