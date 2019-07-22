package ru.file.manager.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import ru.file.manager.MainActivity;
import ru.file.manager.R;
import ru.file.manager.recycler.Adapter;

public class SearchDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener
{
	private EditText editText;

	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		Intent intent = new Intent(editText.getContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_NAME, editText.getText().toString());
        editText.getContext().startActivity(intent);
	}

	public SearchDialog(Context context)
	{
        super(context);
        View view = View.inflate(context, R.layout.dialog_edit_text, null);
        editText = view.findViewById(R.id.dialog_edit_text);
        setView(view);
        setNegativeButton(android.R.string.cancel, null);
        setPositiveButton("Find", this);
        setTitle("Search");
    }
}
