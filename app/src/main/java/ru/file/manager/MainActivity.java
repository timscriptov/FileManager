package ru.file.manager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import ru.file.manager.data.Preferences;
import ru.file.manager.module.Dialogs;
import ru.file.manager.recycler.Adapter;
import ru.file.manager.recycler.OnItemSelectedListener;
import ru.file.manager.ui.CreateDialog;
import ru.file.manager.ui.RenameDialog;
import ru.file.manager.ui.SearchDialog;
import ru.file.manager.utils.FileUtils;
import ru.file.manager.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity
{
    private static final String SAVED_DIRECTORY = "ru.file.manager.SAVED_DIRECTORY";
    private static final String SAVED_SELECTION = "ru.file.manager.SAVED_SELECTION";
    public static final String EXTRA_NAME = "ru.file.manager.EXTRA_NAME";
    public static final String EXTRA_TYPE = "ru.file.manager.EXTRA_TYPE";

    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private File currentDirectory;
    private Adapter adapter;
    private String name;
    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        initActivityFromIntent();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initAppBarLayout();
        initCoordinatorLayout();
        initDrawerLayout();
        initFloatingActionButton();
        initNavigationView();
        initRecyclerView();
        loadIntoRecyclerView();
        invalidateToolbar();
        invalidateTitle();

		if (!Preferences.isRated())
		{
			new Timer().schedule(new TimerTask() {
					@Override
					public void run()
					{
						runOnUiThread(new Runnable() {
								@Override
								public void run()
								{
									try
									{
										Dialogs.rate(MainActivity.this);
									}
									catch (WindowManager.BadTokenException ignored)
									{
									}
								}
							});
					}
				}, 10000);
		}
    }

    @Override
    public void onBackPressed()
	{
        if (drawerLayout.isDrawerOpen(navigationView))
		{
            drawerLayout.closeDrawers();
            return;
        }

        if (adapter.anySelected())
		{
            adapter.clearSelection();
            return;
        }

        if (!FileUtils.isStorage(currentDirectory))
		{
            setPath(currentDirectory.getParentFile());
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
        if (requestCode == 0)
		{
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
			{
                Snackbar.make(coordinatorLayout, "Permission required", Snackbar.LENGTH_INDEFINITE)
					.setAction("Settings", new View.OnClickListener() {
						@Override
						public void onClick(View v)
						{
							gotoApplicationSettings();
						}
					})
					.show();
            }
			else
			{
                loadIntoRecyclerView();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume()
	{
        if (adapter != null) adapter.refresh();
        super.onResume();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
	{
        adapter.select(savedInstanceState.getIntegerArrayList(SAVED_SELECTION));
        String path = savedInstanceState.getString(SAVED_DIRECTORY, FileUtils.getInternalStorage().getPath());
        if (currentDirectory != null) setPath(new File(path));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
	{
        outState.putIntegerArrayList(SAVED_SELECTION, adapter.getSelectedPositions());
        outState.putString(SAVED_DIRECTORY, FileUtils.getPath(currentDirectory));
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        getMenuInflater().inflate(R.menu.action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
        switch (item.getItemId())
		{
            case R.id.action_delete:
                actionDelete();
                return true;
            case R.id.action_rename:
                actionRename();
                return true;
            case R.id.action_search:
                actionSearch();
                return true;
            case R.id.action_copy:
                actionCopy();
                return true;
            case R.id.action_move:
                actionMove();
                return true;
            case R.id.action_send:
                actionSend();
                return true;
            case R.id.action_sort:
                actionSort();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
	{
        if (adapter != null)
		{
            int count = adapter.getSelectedItemCount();
            menu.findItem(R.id.action_delete).setVisible(count >= 1);
            menu.findItem(R.id.action_rename).setVisible(count >= 1);
            menu.findItem(R.id.action_search).setVisible(count == 0);
            menu.findItem(R.id.action_copy).setVisible(count >= 1 && name == null && type == null);
            menu.findItem(R.id.action_move).setVisible(count >= 1 && name == null && type == null);
            menu.findItem(R.id.action_send).setVisible(count >= 1);
            menu.findItem(R.id.action_sort).setVisible(count == 0);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void initActivityFromIntent()
	{
        name = getIntent().getStringExtra(EXTRA_NAME);
        type = getIntent().getStringExtra(EXTRA_TYPE);
    }

    private void loadIntoRecyclerView()
	{
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission))
		{
            ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
            return;
        }

        final Context context = this;

        if (name != null)
		{
            adapter.addAll(FileUtils.searchFilesName(context, name));
            return;
        }

        if (type != null)
		{
            switch (type)
			{
                case "audio":
                    adapter.addAll(FileUtils.getAudioLibrary(context));
                    break;
                case "image":
                    adapter.addAll(FileUtils.getImageLibrary(context));
                    break;
                case "video":
                    adapter.addAll(FileUtils.getVideoLibrary(context));
                    break;
            }
            return;
        }
        setPath(FileUtils.getInternalStorage());
    }

    private void initAppBarLayout()
	{
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more));
        setSupportActionBar(toolbar);
    }

    private void initCoordinatorLayout()
	{
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    }

    private void initDrawerLayout()
	{
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawerLayout == null) return;

        if (name != null || type != null)
		{
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void initFloatingActionButton()
	{
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_action_button);

        if (fab == null) return;

        fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					actionCreate();
				}
			});

        if (name != null || type != null)
		{
            ViewGroup.LayoutParams layoutParams = fab.getLayoutParams();
            ((CoordinatorLayout.LayoutParams) layoutParams).setAnchorId(View.NO_ID);
            fab.setLayoutParams(layoutParams);
            fab.hide();
        }
    }

    private void initNavigationView()
	{
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        if (navigationView == null) return;

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_external);
        menuItem.setVisible(FileUtils.getExternalStorage() != null);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem item)
				{
					switch (item.getItemId())
					{
						case R.id.navigation_audio:
							setType("audio");
							return true;
						case R.id.navigation_image:
							setType("image");
							return true;
						case R.id.navigation_video:
							setType("video");
							return true;
						case R.id.navigation_feedback:
							gotoFeedback();
							return true;
						case R.id.navigation_settings:
							gotoSettings();
							return true;
					}
					drawerLayout.closeDrawers();

					switch (item.getItemId())
					{
						case R.id.navigation_directory_0:
							setPath(FileUtils.getPublicDirectory("DCIM"));
							return true;
						case R.id.navigation_directory_1:
							setPath(FileUtils.getPublicDirectory("Download"));
							return true;
						case R.id.navigation_directory_2:
							setPath(FileUtils.getPublicDirectory("Movies"));
							return true;
						case R.id.navigation_directory_3:
							setPath(FileUtils.getPublicDirectory("Music"));
							return true;
						case R.id.navigation_directory_4:
							setPath(FileUtils.getPublicDirectory("Pictures"));
							return true;
						default:
							return true;
					}
				}
			});

        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.header);
        textView.setText(FileUtils.getStorageUsage(this));
        textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
				}
			});
    }

    private void initRecyclerView()
	{
        adapter = new Adapter(this);
        adapter.setOnItemClickListener(new OnItemClickListener(this));
        adapter.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected()
				{
					invalidateOptionsMenu();
					invalidateTitle();
					invalidateToolbar();
				}
			});

        if (type != null)
		{
            switch (type)
			{
                case "audio":
                    adapter.setItemLayout(R.layout.list_item_music);
                    adapter.setSpanCount(getResources().getInteger(R.integer.span_count1));
                    break;
                case "image":
                    adapter.setItemLayout(R.layout.list_item_image);
                    adapter.setSpanCount(getResources().getInteger(R.integer.span_count2));
                    break;
                case "video":
                    adapter.setItemLayout(R.layout.list_item_video);
                    adapter.setSpanCount(getResources().getInteger(R.integer.span_count3));
                    break;
            }
        }
		else
		{
            adapter.setItemLayout(R.layout.list_item_files);
            adapter.setSpanCount(getResources().getInteger(R.integer.span_count0));
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        if (recyclerView != null) recyclerView.setAdapter(adapter);
    }

    private void invalidateTitle()
	{
        if (adapter.anySelected())
		{
            int selectedItemCount = adapter.getSelectedItemCount();
            setTitle(String.format("%s selected", selectedItemCount));
        }
		else if (name != null)
		{
            setTitle(String.format("Search for %s", name));
        }
		else if (type != null)
		{
            switch (type)
			{
                case "image":
                    setTitle("Images");
                    break;
                case "audio":
                    setTitle("Music");
                    break;
                case "video":
                    setTitle("Videos");
                    break;
            }
        }
		else if (currentDirectory != null && !currentDirectory.equals(FileUtils.getInternalStorage()))
		{
            setTitle(FileUtils.getName(currentDirectory));
        }
		else
		{
            setTitle(getResources().getString(R.string.app_name));
        }
    }

    private void invalidateToolbar()
	{
        if (adapter.anySelected())
		{
            toolbar.setNavigationIcon(R.drawable.ic_clear);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						adapter.clearSelection();
					}
				});
        }
		else if (name == null && type == null)
		{
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						drawerLayout.openDrawer(navigationView);
					}
				});
        }
		else
		{
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						MainActivity.this.finish();
					}
				});
        }
    }

    private void actionCreate()
	{
        new CreateDialog(this, adapter, currentDirectory).show();
    }

    private void actionDelete()
	{
        actionDelete(adapter.getSelectedItems());
        adapter.clearSelection();
    }

    private void actionDelete(final List<File> files)
	{
        final File sourceDirectory = currentDirectory;
        adapter.removeAll(files);
        String message = String.format("%s files deleted", files.size());

        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
			.setAction("Undo", new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					if (currentDirectory == null || currentDirectory.equals(sourceDirectory))
					{
						adapter.addAll(files);
					}
				}
			})
			.addCallback(new Snackbar.Callback() {
				@Override
				public void onDismissed(Snackbar snackbar, int event)
				{
					if (event != DISMISS_EVENT_ACTION)
					{
						try
						{
							for (File file : files) FileUtils.deleteFile(file);
						}
						catch (Exception e)
						{
							showMessage(e);
						}
					}
					super.onDismissed(snackbar, event);
				}
			})
			.show();
    }

    private void actionRename()
	{
        new RenameDialog(this, adapter).show();
    }

    private void actionSearch()
	{
        new SearchDialog(this).show();
    }

    private void actionCopy()
	{
        List<File> selectedItems = adapter.getSelectedItems();
        adapter.clearSelection();
        transferFiles(selectedItems, false);
    }

    private void actionMove()
	{
        List<File> selectedItems = adapter.getSelectedItems();
        adapter.clearSelection();
        transferFiles(selectedItems, true);
    }

    private void actionSend()
	{
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        ArrayList<Uri> uris = new ArrayList<>();

        for (File file : adapter.getSelectedItems())
		{
            if (file.isFile()) uris.add(Uri.fromFile(file));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(intent);
    }

    private void actionSort()
	{
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int checkedItem = PreferenceUtils.getInteger(this, "pref_sort", 0);
        String[] sorting = {"Name", "Last modified", "Size (high to low)"};
        final Context context = this;

        builder.setSingleChoiceItems(sorting, checkedItem, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					adapter.update(which);
					PreferenceUtils.putInt(context, "pref_sort", which);
					dialog.dismiss();
				}
			});
        builder.setTitle("Sort by");
        builder.show();
    }

    private void transferFiles(final List<File> files, final Boolean delete)
	{
        String paste = delete ? "moved" : "copied";
        String message = String.format(Locale.getDefault(), "%d items waiting to be %s", files.size(), paste);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v)
			{
                try
				{
                    for (File file : files)
					{
                        adapter.addAll(FileUtils.copyFile(file, currentDirectory));
                        if (delete) FileUtils.deleteFile(file);
                    }
                }
				catch (Exception e)
				{
                    MainActivity.this.showMessage(e);
                }
            }
        };
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE).setAction("Paste", onClickListener).show();
    }

    private void showMessage(Exception e)
	{
        showMessage(e.getMessage());
    }

    private void showMessage(String message)
	{
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void gotoFeedback()
	{
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"elmurzaev.ram@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "DevSchool");
        intent.setDataAndType(Uri.parse("email"), "message/rfc822");
        Intent chooser = Intent.createChooser(intent, "Email");
        startActivity(chooser);
    }

    private void gotoSettings()
	{
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void gotoApplicationSettings()
	{
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", "ru.file.manager", null));
        startActivity(intent);
    }

    private void setPath(File directory)
	{
        if (!directory.exists())
		{
            Toast.makeText(this, "Directory doesn't exist", Toast.LENGTH_SHORT).show();
            return;
        }
        currentDirectory = directory;
        adapter.clear();
        adapter.clearSelection();
        adapter.addAll(FileUtils.getChildren(directory));
        invalidateTitle();
    }

    private void setName(String name)
	{
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        startActivity(intent);
    }

    private void setType(String type)
	{
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_TYPE, type);

        if (Build.VERSION.SDK_INT >= 21)
		{
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        startActivity(intent);
    }

    private final class OnItemClickListener implements ru.file.manager.recycler.OnItemClickListener
	{
        private final Context context;

        private OnItemClickListener(Context context)
		{
            this.context = context;
        }

        @Override
        public void onItemClick(int position)
		{
            final File file = adapter.get(position);
            if (adapter.anySelected())
			{
                adapter.toggle(position);
                return;
            }

            if (file.isDirectory())
			{
                if (file.canRead())
				{
                    setPath(file);
                }
				else
				{
                    showMessage("Cannot open directory");
                }
            }
			else
			{
                if (Intent.ACTION_GET_CONTENT.equals(getIntent().getAction()))
				{
                    Intent intent = new Intent();
                    intent.setDataAndType(Uri.fromFile(file), FileUtils.getMimeType(file));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
				else if (FileUtils.FileType.getFileType(file) == FileUtils.FileType.ZIP)
				{
                    final ProgressDialog dialog = ProgressDialog.show(context, "", "Unzipping", true);
                    Thread thread = new Thread(new Runnable() {
							@Override
							public void run()
							{
								try
								{
									setPath(FileUtils.unzip(file));
									runOnUiThread(new Runnable() {
											@Override
											public void run()
											{
												dialog.dismiss();
											}
										});
								}
								catch (Exception e)
								{
									showMessage(e);
								}
							}
						});
                    thread.run();
                }
				else
				{
                    try
					{
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), FileUtils.getMimeType(file));
                        startActivity(intent);
                    }
					catch (Exception e)
					{
                        showMessage(String.format("Cannot open %s", FileUtils.getName(file)));
                    }
                }
            }
        }

        @Override
        public boolean onItemLongClick(int position)
		{
            adapter.toggle(position);
            return true;
        }
    }
}
