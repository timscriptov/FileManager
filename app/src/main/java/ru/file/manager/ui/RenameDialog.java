package ru.file.manager.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.util.List;

import ru.file.manager.R;
import ru.file.manager.recycler.Adapter;
import ru.file.manager.utils.FileUtils;

public class RenameDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener
{
	private EditText editText;
	private Adapter adapter;

	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		if (editText.length() != 0)
		{
			List<File> selectedItems = adapter.getSelectedItems();
			adapter.clearSelection();

			try
			{
				if (selectedItems.size() == 1)
				{
					File file = selectedItems.get(0);
					int index = adapter.indexOf(file);
					adapter.updateItemAt(index, FileUtils.renameFile(file, editText.getText().toString()));
				}
				else
				{
					int size = String.valueOf(selectedItems.size()).length();
					String format = " (%0" + size + "d)";

					for (int i = 0; i < selectedItems.size(); i++)
					{
						File file = selectedItems.get(i);
						int index = adapter.indexOf(file);
						File newFile = FileUtils.renameFile(file, editText.getText().toString() + String.format(format, i + 1));
						adapter.updateItemAt(index, newFile);
					}
				}
			}
			catch (Exception e)
			{

			}
		}
	}

    public RenameDialog(Context context, Adapter adapter)
	{
        super(context);
        View view = View.inflate(context, R.layout.dialog_edit_text, null);
        editText = view.findViewById(R.id.dialog_edit_text);
        setView(view);
        setNegativeButton(android.R.string.cancel, null);
        setPositiveButton("Rename", this);
        setTitle("Rename");

		this.adapter = adapter;

		setDefault(FileUtils.removeExtension(adapter.getSelectedItems().get(0).getName()));
    }

    public void setDefault(String text)
	{
        editText.setText(text);
        editText.setSelection(editText.getText().length());
    }
}
