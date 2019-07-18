package ru.file.manager.module;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;

import es.dmoral.toasty.Toasty;

import ru.file.manager.R;
import ru.file.manager.data.Preferences;

public class Dialogs
{
    public static void rate(final Context context)
	{
        View view = LayoutInflater.from(context).inflate(R.layout.rate, null);
        RatingBar ratingBar = view.findViewById(R.id.rating_bar);

        final AlertDialog dialog = new AlertDialog.Builder(context).setCancelable(false).setView(view).create();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
				@Override
				public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
				{
					if (rating > 3)
					{
						context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ru.file.manager")));
					}
					else Toasty.info(context, R.string.thanks).show();
					dialog.dismiss();
					Preferences.setRated(true);
				}
			});
        dialog.show();
    }
}
