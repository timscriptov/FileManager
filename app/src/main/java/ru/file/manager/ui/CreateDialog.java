package ru.file.manager.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import java.io.File;
import ru.file.manager.R;
import ru.file.manager.recycler.Adapter;
import ru.file.manager.utils.FileUtils;

public class CreateDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener
{
    private EditText editText;
	private Adapter adapter;
	private File currentDirectory;

    public CreateDialog(Context context, @Nullable Adapter adapter, File currentDirectory)
	{
        super(context);
        View view = View.inflate(context, R.layout.dialog_edit_text, null);
        editText = view.findViewById(R.id.dialog_edit_text);
        setView(view);
        setNegativeButton("File", this);
        setPositiveButton("Folder", this);
		setNeutralButton(android.R.string.cancel, null);
        setTitle("Create");

		this.adapter = adapter;
		this.currentDirectory = currentDirectory;
    }

	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		if (editText.length() != 0)
		{
			if (p2 == DialogInterface.BUTTON_POSITIVE)
			{
				try
				{
					File directory = FileUtils.createDirectory(currentDirectory, editText.getText().toString());
					adapter.clearSelection();
					adapter.add(directory);
				}
				catch (Exception e)
				{}
			}
			else if (p2 == DialogInterface.BUTTON_NEGATIVE)
			{
				try
				{
					File directory = FileUtils.createFile(currentDirectory, editText.getText().toString());
					adapter.clearSelection();
					adapter.add(directory);
				}
				catch (Exception e)
				{}
			}
		}
	}

	public enum Action
	{
		CREATE_FILE, CREATE_FOLDER;
	}
}
