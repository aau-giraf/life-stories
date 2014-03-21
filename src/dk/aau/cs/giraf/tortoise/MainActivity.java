package dk.aau.cs.giraf.tortoise;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.tortoise.PictogramView.OnDeleteClickListener;
import dk.aau.cs.giraf.tortoise.SequenceListAdapter.OnAdapterGetViewListener;
import dk.aau.cs.giraf.oasis.lib.models.App;

public class MainActivity extends Activity {

    private final int DIALOG_DELETE = 1;
    private final int PROFILE_CHANGE = 11; // constant for profile change intent
    private int profileResult;
    private boolean isInEditMode = false;
    private boolean isInTemplateMode = false;
    private boolean canFinish;
    private SequenceListAdapter sequenceAdapter;
    private Bitmap childImage;
    private Bitmap guardianImage;
    private Profile currGuard;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Check which request we're responding to
        if (requestCode == PROFILE_CHANGE)
        {
            // Make sure the request was successful
            if (resultCode == RESULT_OK)
            {
                Uri contactUri = data.getData();
                String[] projection = {"currentChildID"};
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex("currentChildID");
                String number = cursor.getString(column);

            }
        }
    }

    /**
     * Initializes all app elements.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);

        // Warn user and do not execute Tortoise if not launched from Giraf
        if (getIntent().getExtras() == null) {
            GuiHelper.ShowToast(getApplicationContext(), "Tortoise skal startes fra GIRAF");
            finish();
        }
        // If launched from Giraf, then execute!
        else {
            // Initialize image and name of profile
            ImageView profileImage = (ImageView)findViewById(R.id.profileImage);
            TextView profileName = (TextView)findViewById(R.id.child_name);

            Intent i = getIntent();
            Helper h = new Helper(this);

            // Set guardian- and child profiles
            LifeStory.getInstance().setGuardian(
                    h.profilesHelper.getProfileById(i.getLongExtra("currentGuardianID", -1)));

            currGuard = h.profilesHelper.getProfileById(i.getLongExtra("currentGuardianID", -1)); // YIHAAA!
            //GuiHelper.ShowToast(getApplicationContext(), currGuard.toString());

            LifeStory.getInstance().setChild(
                    h.profilesHelper.getProfileById(i.getLongExtra("currentChildID", -1)));
            profileName.setText(LifeStory.getInstance().getChild().getFirstname());
            setProfileImages();
            profileImage.setImageBitmap(childImage);

            // Clear existing life stories
            LifeStory.getInstance().getStories().clear();
            LifeStory.getInstance().getTemplates().clear();

            // Set templates belonging to the chosen guardian and stories belonging to the chosen child
            JSONSerializer js = new JSONSerializer();
            try {
                LifeStory.getInstance().setTemplates(
                        js.loadSettingsFromFile(
                                getApplicationContext(),
                                LifeStory.getInstance().getGuardian().getId()));
                LifeStory.getInstance().setStories(
                        js.loadSettingsFromFile(
                                getApplicationContext(),
                                LifeStory.getInstance().getChild().getId()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Initialize grid view
            GridView sequenceGrid = (GridView)findViewById(R.id.sequence_grid);
            sequenceAdapter = initAdapter();
            sequenceGrid.setAdapter(sequenceAdapter);

            // Creates clean sequence and starts the sequence activity - ready to add pictograms.
            final ImageButton createButton = (ImageButton)findViewById(R.id.add_button);

            createButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    canFinish = false;
                    Intent i = new Intent(getApplicationContext(), EditModeActivity.class);
                    i.putExtra("template", -1);

                    startActivity(i);
                }
            });

            final ImageView homeButton = (ImageView)findViewById(R.id.exitEditMode);
//            homeButton.setOnClickListener(new OnClickListener(){});

            // this is the button with the profile image the user can click to change profiles
            final ImageView changeProfileButton = (ImageView)findViewById(R.id.profileImage);

            changeProfileButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent motionEvent)
                {
                    // set the profile image button to be faded - it is changed back to a solid
                    // color (1.0) in changeProfileButton.setOnClickListener
                    changeProfileButton.setAlpha(0.3f);

                    // return false to indicate that the event for the profile image has not been
                    // consumed. If this is changed to true, the changeProfileButton.setOnClickListener
                    // does not work
                    return false;
                }
            });

            changeProfileButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // change color of profile button from faded back to solid color
                    // 0f is invisible and 1f is solid
                    changeProfileButton.setAlpha(1f);

                    // this is the code for launching the profile selector in the launcher project
                    // the launcher then creates a new instance of the tortoise project

                    // create a new intent
                    Intent intent = new Intent("dk.aau.cs.giraf.launcher.action.SELECTPROFILE");

                    // put package name
                    intent.putExtra("appPackageName", "dk.aau.cs.giraf.tortoise");

                    // put Activity name
                    intent.putExtra("appActivityName", "dk.aau.cs.giraf.tortoise.MainActivity");

                    // put App Background Color
                    intent.putExtra("appBackgroundColor", 0xFF16A765);

                    // Put current guardian id
                    intent.putExtra("currentGuardianID", currGuard.getId());

                    intent.setComponent(new ComponentName("dk.aau.cs.giraf.launcher", "dk.aau.cs.giraf.launcher.activities.ProfileSelectActivity"));

                    // Verify the intent will resolve to at least one activity
                    if (intent.resolveActivity(getPackageManager()) != null)
                    {
                        startActivity(intent);
                    }
                    else
                    {
                        GuiHelper.ShowToast(getApplicationContext(), "Kunne ikke starte profilvælger");
                    }
                }
            });

			// Load Sequence
            sequenceGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                    Intent i;

                    if(isInTemplateMode) {
                        canFinish = false;
                        i = new Intent(getApplicationContext(), EditModeActivity.class);
                        i.putExtra("template", arg2);
                    }
                    else {
                        canFinish = false;
                        i = new Intent(getApplicationContext(), ViewModeActivity.class);
                        i.putExtra("story", arg2);
                    }

                    startActivity(i);
                }
            });

            // Edit mode switcher button
            ToggleButton button = (ToggleButton) findViewById(R.id.edit_mode_toggle);

            button.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ToggleButton button = (ToggleButton)v;
                    isInEditMode = button.isChecked();
                    GridView sequenceGrid = (GridView) findViewById(R.id.sequence_grid);

                    // Make sure that all views currently not visible will have the correct editmode when they become visible
                    sequenceAdapter.setEditModeEnabled(isInEditMode);

                    //createButton.setVisibility(isInEditMode ? View.VISIBLE : View.GONE);

                    // Update the editmode of all visible views in the grid
                    for (int i = 0; i < sequenceGrid.getChildCount(); i++) {
                        View view = sequenceGrid.getChildAt(i);

                        if (view instanceof PictogramView) {
                            ((PictogramView)view).setEditModeEnabled(isInEditMode);
                        }
                    }
                }
            });

            // Template mode switcher button
            ToggleButton templateToggle = (ToggleButton) findViewById(R.id.template_mode_toggle);

            templateToggle.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ToggleButton button = (ToggleButton)v;
                    ImageView profileImage = (ImageView)findViewById(R.id.profileImage);
                    TextView profileName = (TextView)findViewById(R.id.child_name);
                    if(button.isChecked()) {
                        Profile g = LifeStory.getInstance().getGuardian();
                        profileName.setText(
                                g.getFirstname() + " " + g.getSurname());
                        profileImage.setImageBitmap(guardianImage);
                    }
                    else {
                        Profile c = LifeStory.getInstance().getChild();
                        profileName.setText(c.getFirstname());
                        profileImage.setImageBitmap(childImage);
                    }
                    isInTemplateMode = button.isChecked();
                    sequenceAdapter.setTemplateModeEnabled(isInTemplateMode);
                    sequenceAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public SequenceListAdapter initAdapter() {
        final SequenceListAdapter adapter = new SequenceListAdapter(this);

        adapter.setOnAdapterGetViewListener(new OnAdapterGetViewListener() {

            @Override
            public void onAdapterGetView(final int position, View view) {
                if (view instanceof PictogramView) {

                    PictogramView pictoView = (PictogramView) view;

                    pictoView.setOnDeleteClickListener(new OnDeleteClickListener() {

                        @Override
                        public void onDeleteClick() {
                            renderDialog(DIALOG_DELETE, position);
                        }
                    });
                }
            }
        });

        return adapter;
    }

    @Override
    protected void onStart() {
        canFinish = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(canFinish) {
            finish();
        }
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater()
                .inflate(R.menu.activity_tortoise_startup_screen, menu);
        return true;
    }

    @Override
    protected void onResume() {
        ToggleButton templateMode = (ToggleButton)findViewById(R.id.template_mode_toggle);
        ToggleButton editMode = (ToggleButton) findViewById(R.id.edit_mode_toggle);
        ImageView profileImage = (ImageView)findViewById(R.id.profileImage);
        TextView profileName = (TextView)findViewById(R.id.child_name);

        isInEditMode = false;
        isInTemplateMode = false;
        templateMode.setChecked(false);
        editMode.setChecked(false);
        Profile c = LifeStory.getInstance().getChild();
        profileName.setText(c.getFirstname());
        profileImage.setImageBitmap(childImage);
        sequenceAdapter.setEditModeEnabled(isInEditMode);
        sequenceAdapter.setTemplateModeEnabled(isInTemplateMode);
        sequenceAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void setProfileImages() {

        Bitmap bm;
        if(LifeStory.getInstance().getChild().getPicture() != null) {
            bm = LayoutTools.decodeSampledBitmapFromFile(
                    LifeStory.getInstance().getChild().getPicture(), 100, 100);

        }
        else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
        }

        childImage = LayoutTools.getRoundedCornerBitmap(bm, this, 10);

        if(LifeStory.getInstance().getGuardian().getPicture() != null) {
            bm = LayoutTools.decodeSampledBitmapFromFile(
                    LifeStory.getInstance().getGuardian().getPicture(), 100, 100);
        }
        else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
        }

        guardianImage = LayoutTools.getRoundedCornerBitmap(bm, this, 10);
    }

    public void renderDialog(int dialogId, final int position) {


        String storyName;

        // If isInTemplateMode is true then the guardian profile is active. If not, the child profile is active.
        if(isInTemplateMode) {
            storyName = LifeStory.getInstance().getTemplates().get(position).getTitle();
        }
        else {
            storyName = LifeStory.getInstance().getStories().get(position).getTitle();
        }

        // Dialog that prompts for deleting a story or template
        switch (dialogId) {
            case DIALOG_DELETE:
                GDialog gdialog;

                gdialog = new GDialog(this,
                        R.drawable.ic_launcher,
                        getString(R.string.dialog_delete_title),
                        getResources().getString(R.string.dialog_delete_message) + " \"" + storyName + "\"",
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                JSONSerializer js = new JSONSerializer();
                                if(isInTemplateMode) {
                                    LifeStory.getInstance().getTemplates().remove(position);
                                    try {
                                        js.saveSettingsToFile(getApplicationContext(),
                                                LifeStory.getInstance().getTemplates(),
                                                LifeStory.getInstance().getGuardian().getId());
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }else {
                                    LifeStory.getInstance().getStories().remove(position);
                                    try {
                                        js.saveSettingsToFile(getApplicationContext(),
                                                LifeStory.getInstance().getStories(),
                                                LifeStory.getInstance().getChild().getId());
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                                sequenceAdapter.setItems();
                                sequenceAdapter.notifyDataSetChanged();

                            }

                        });

                gdialog.show();
                break;
            default:
                break;

        }


    }

    public void doExit(View v){
//        GuiHelper.ShowToast(getApplicationContext(), "doExit pressed!");
        finish();
    }
}
