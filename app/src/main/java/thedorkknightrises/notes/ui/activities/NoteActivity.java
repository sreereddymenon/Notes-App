package thedorkknightrises.notes.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.actions.NoteIntents;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NoteActivity extends AppCompatActivity {
    protected Boolean editMode;
    protected CoordinatorLayout coordinatorLayout;
    protected NotesDbHelper dbHelper;
    protected EditText titleText;
    protected EditText subtitleText;
    protected EditText contentText;
    protected TextView timeText;
    FloatingActionButton fab;
    View toolbar_note, toolbar, bottom_bar;
    View archive_hint;
    SharedPreferences pref;
    boolean lightTheme;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    float radius;
    private int cx, cy;
    private int id = -1, archived = 0, notified = 0, encrypted = 0, pinned = 0, tag = 0;
    private String title, subtitle, content, time, created_at, color = Constants.COLOR_NONE, reminder = Constants.REMINDER_NONE;
    private boolean backPressFlag = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.NoteLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        titleText = (EditText) findViewById(R.id.title);
        subtitleText = (EditText) findViewById(R.id.subtitle);
        contentText = (EditText) findViewById(R.id.content);
        timeText = (TextView) findViewById(R.id.note_date);
        fab = (FloatingActionButton) findViewById(R.id.fab_note);
        toolbar_note = findViewById(R.id.toolbar_note);
        toolbar = findViewById(R.id.toolbar);
        bottom_bar = findViewById(R.id.bottom_bar);
        archive_hint = findViewById(R.id.archive_hint);

        dbHelper = new NotesDbHelper(this);

        Bundle bundle = getIntent().getBundleExtra(Constants.NOTE_DETAILS_BUNDLE);
        if (bundle != null) {
            editMode = false;
            id = bundle.getInt(NotesDb.Note._ID);
            title = bundle.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = bundle.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = bundle.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
            time = bundle.getString(NotesDb.Note.COLUMN_NAME_TIME);
            created_at = bundle.getString(NotesDb.Note.COLUMN_NAME_CREATED_AT);
            archived = bundle.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = bundle.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
            color = bundle.getString(NotesDb.Note.COLUMN_NAME_COLOR);
            encrypted = bundle.getInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED);
            pinned = bundle.getInt(NotesDb.Note.COLUMN_NAME_PINNED);
            tag = bundle.getInt(NotesDb.Note.COLUMN_NAME_TAG);
            reminder = bundle.getString(NotesDb.Note.COLUMN_NAME_REMINDER);
        } else {
            editMode = true;
        }

        if (savedInstanceState != null) {
            editMode = savedInstanceState.getBoolean("editMode");
            id = savedInstanceState.getInt(NotesDb.Note._ID);
            title = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
            time = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TIME);
            created_at = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_CREATED_AT);
            archived = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
            color = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_COLOR);
            encrypted = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED);
            pinned = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_PINNED);
            tag = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_TAG);
            reminder = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_REMINDER);
            bottom_bar.setVisibility(View.VISIBLE);
        }

        if (bundle == null && savedInstanceState == null) {
            Calendar c = Calendar.getInstance();
            //get date and time, specifically in 24-hr format suitable for sorting
            created_at = sdf.format(c.getTime());
        }

        if (!editMode) {
            if (TextUtils.isEmpty(title))
                titleText.setVisibility(View.GONE);
            else {
                titleText.setText(title);
                titleText.setEnabled(false);
                if (lightTheme)
                    titleText.setTextColor(getResources().getColor(R.color.black));
                else titleText.setTextColor(getResources().getColor(R.color.white));
            }
            if (TextUtils.isEmpty(subtitle))
                subtitleText.setVisibility(View.GONE);
            else {
                subtitleText.setText(subtitle);
                subtitleText.setEnabled(false);
                if (lightTheme)
                    subtitleText.setTextColor(getResources().getColor(R.color.dark_gray));
                else subtitleText.setTextColor(getResources().getColor(R.color.light_gray));
            }

            contentText.setText(content);
            edit(contentText, false);
            if (lightTheme)
                contentText.setTextColor(getResources().getColor(R.color.black));
            else contentText.setTextColor(getResources().getColor(R.color.white));

            timeText.setText(time);

            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupWindowAnimations();
            }
            bottom_bar.setVisibility(View.GONE);
            timeText.setText("");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setVisibility(View.VISIBLE);
                if (savedInstanceState == null && !editMode) revealToolbar();
            }
        }, 350);

        if (archived == 1) {
            archive_hint.setVisibility(View.VISIBLE);
            ((ImageButton) findViewById(R.id.archive_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_unarchive_white_24dp));
        }

        if (notified == 1) {
            ((ImageButton) findViewById(R.id.notif_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications_off_white_24dp));
        }

        Log.e("Note:", "id: " + id + " created_at: " + created_at);
        Intent intent = getIntent();
        if (NoteIntents.ACTION_CREATE_NOTE.equals(intent.getAction())) {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                content = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
                if (!TextUtils.isEmpty(content)) contentText.setText(content);
            }
            if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
                title = getIntent().getExtras().getString(Intent.EXTRA_SUBJECT);
                if (!TextUtils.isEmpty(title)) titleText.setText(title);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbar.setVisibility(View.VISIBLE);
                    if (savedInstanceState == null && !editMode) revealToolbar();
                    onClick(fab);
                }
            }, 350);
        }
    }

    @Override
    protected void onResume() {
        View deleteBtn = findViewById(R.id.delete);
        if (!editMode) {
            deleteBtn.setVisibility(View.VISIBLE);
        } else deleteBtn.setVisibility(View.GONE);
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("editMode", editMode);
        bundle.putInt(NotesDb.Note._ID, id);
        bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
        bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, time);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CREATED_AT, created_at);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
        bundle.putString(NotesDb.Note.COLUMN_NAME_COLOR, color);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
        bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);

        super.onSaveInstanceState(bundle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Slide slide = new Slide(Gravity.TOP);
        slide.addTarget(R.id.note_card);
        slide.addTarget(R.id.note_title);
        slide.addTarget(R.id.note_subtitle);
        slide.addTarget(R.id.note_content);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
        getWindow().setReenterTransition(slide);
    }

    private void revealToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            cx = bottom_bar.getWidth() / 2;
            cy = bottom_bar.getHeight() / 2;
            radius = (float) Math.hypot(cx, cy);

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(bottom_bar, cx, cy, 0, radius);

            // make the view visible and start the animation
            bottom_bar.setVisibility(View.VISIBLE);
            anim.start();
        } else bottom_bar.setVisibility(View.VISIBLE);
    }

    private void hideToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            cx = bottom_bar.getWidth() / 2;
            cy = bottom_bar.getHeight() / 2;
            radius = (float) Math.hypot(cx, cy);

            // create the animation (the final radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(bottom_bar, cx, cy, radius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    bottom_bar.setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();
        } else bottom_bar.setVisibility(View.INVISIBLE);
    }

    public void close(View v) {
        if (titleText.getText().toString().isEmpty()) titleText.setVisibility(View.INVISIBLE);
        if (subtitleText.getText().toString().isEmpty()) subtitleText.setVisibility(View.INVISIBLE);
        backPressFlag = true;
        onBackPressed();
    }

    public void delete(View v) {
        dbHelper.deleteNote(created_at);
        MainActivity.changed = true;
        notif(0);
        finish();
    }

    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editMode) {
            title = titleText.getText().toString().trim();
            subtitle = subtitleText.getText().toString().trim();
            content = contentText.getText().toString().trim();
            if (content.equals("")) {
                contentText.requestFocus();
                Snackbar.make(coordinatorLayout, R.string.incomplete, Snackbar.LENGTH_LONG).show();
            } else {
                Calendar c = Calendar.getInstance();
                //get date and time, specifically in 24-hr format suitable for sorting
                time = sdf.format(c.getTime());
                Log.d("TIME", time);
                archived = 0;
                notif(0);
                id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
                editMode = false;
                MainActivity.changed = true;
                if (!title.equals("")) {
                    titleText.setEnabled(false);
                    if (lightTheme)
                        titleText.setTextColor(getResources().getColor(R.color.black));
                    else titleText.setTextColor(getResources().getColor(R.color.white));
                } else titleText.setVisibility(View.GONE);
                if (!subtitle.equals("")) {
                    subtitleText.setEnabled(false);
                    if (lightTheme)
                        subtitleText.setTextColor(getResources().getColor(R.color.dark_gray));
                    else subtitleText.setTextColor(getResources().getColor(R.color.light_gray));
                } else subtitleText.setVisibility(View.GONE);
                edit(contentText, false);
                if (lightTheme)
                    contentText.setTextColor(getResources().getColor(R.color.black));
                else contentText.setTextColor(getResources().getColor(R.color.white));

                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));

                archive_hint.setVisibility(View.GONE);
                revealToolbar();
                findViewById(R.id.note_update).setVisibility(View.VISIBLE);
                timeText.setText(time);
                editMode = false;

                if (MainActivity.archive == 1) {
                    MainActivity.archive = 0;
                }

                notif(notified);

                // Hide the keyboard
                imm.hideSoftInputFromWindow(contentText.getWindowToken(), 0);
            }
        } else {
            titleText.setEnabled(true);
            subtitleText.setEnabled(true);
            edit(contentText, true);
            contentText.setSelection(contentText.getText().length());
            titleText.setVisibility(View.VISIBLE);
            subtitleText.setVisibility(View.VISIBLE);
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_done_white_24dp));
            hideToolbar();
            if (archived == 1) {
                archive_hint.setVisibility(View.VISIBLE);
            }
            findViewById(R.id.note_update).setVisibility(View.GONE);
            timeText.setText("");
            editMode = true;
            contentText.requestFocusFromTouch();
            // Show the keyboard
            imm.showSoftInput(contentText, InputMethodManager.SHOW_IMPLICIT);
        }
        onResume();

    }

    private void edit(EditText editText, boolean enabled) {
        if (!enabled) {
            Linkify.addLinks(editText, Linkify.ALL);
        } else {
            // Workaround to remove links when editing
            editText.setText(editText.getText().toString());
        }
        editText.setFocusable(enabled);
        editText.setFocusableInTouchMode(enabled);
        editText.setClickable(enabled);
        editText.setLongClickable(enabled);
        editText.setLinksClickable(!enabled);
    }

    public void share(View v) {
        Intent share = new Intent(Intent.ACTION_SEND);
        if (subtitle.equals(""))
            share.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content);
        else
            share.putExtra(Intent.EXTRA_TEXT, title + "\n(" + subtitle + ")\n\n" + content);
        share.setType("text/plain");
        startActivity(Intent.createChooser(share, getResources().getString(R.string.share_title)));
    }

    public void notifBtn(View v) {
        notif(0);
        if (notified == 1) {
            notified = 0;
            id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
            MainActivity.changed = true;
            notif(notified);
        } else {
            notified = 1;
            id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
            MainActivity.changed = true;
            notif(notified);
        }
    }

    public void notif(int notified) {
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notified == 0) {
            mNotifyMgr.cancel(id);
            ((ImageButton) findViewById(R.id.notif_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications_active_white_24dp));
        } else {
            ((ImageButton) findViewById(R.id.notif_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications_off_white_24dp));
            String info;
            if (!subtitle.equals("")) info = subtitle;
            else info = time;
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setSubText(info)
                            .setShowWhen(false)
                            .setColor(Color.argb(255, 32, 128, 200));
            notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));
            // Sets an ID for the notification
            Log.d("NOTIFICATION ID", String.valueOf(id));
            Intent resultIntent = new Intent(this, NoteActivity.class);
            resultIntent.putExtra(NotesDb.Note._ID, id);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TITLE, title);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_CONTENT, content);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TIME, time);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_CREATED_AT, created_at);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_COLOR, color);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TAG, tag);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
            resultIntent.setAction("ACTION_NOTE_" + id);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(NoteActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setContentIntent(resultPendingIntent);
            notif.setOngoing(true);

            Log.e("Note:", "id: " + id + " created_at: " + created_at);
            // Builds the notification and issues it.
            mNotifyMgr.notify(id, notif.build());
        }
    }

    public void archive(View v) {
        notif(0);
        if (archived == 1) {
            Toast.makeText(this, R.string.removed_archive, Toast.LENGTH_SHORT).show();
            archived = 0;
            id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
            MainActivity.changed = true;
            notif(notified);
            finish();
        } else {
            Toast.makeText(this, R.string.added_archive, Toast.LENGTH_SHORT).show();
            archived = 1;
            id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
            MainActivity.changed = true;
            notif(notified);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (editMode && !backPressFlag) {
            Toast.makeText(getApplicationContext(), getText(R.string.back_press), Toast.LENGTH_SHORT).show();
            backPressFlag = true;

            // Thread to change backPressedFlag to false after 3000ms
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        backPressFlag = false;
                    }
                }
            }).start();
            return;
        }
        hideToolbar();
        toolbar.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }
}