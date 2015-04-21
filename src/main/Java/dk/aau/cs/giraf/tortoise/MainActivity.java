package dk.aau.cs.giraf.tortoise;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GButtonProfileSelect;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafPictogram;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.oasis.lib.models.Sequence;
import dk.aau.cs.giraf.tortoise.activities.ScheduleEditActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.activities.TortoiseActivity;
import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;
import dk.aau.cs.giraf.tortoise.PictogramView.OnDeleteClickListener;
import dk.aau.cs.giraf.tortoise.SequenceListAdapter.OnAdapterGetViewListener;
import dk.aau.cs.giraf.tortoise.activities.EditModeActivity;

public class MainActivity extends TortoiseActivity {

    private final int DIALOG_DELETE = 1;
    private boolean isInEditMode = false;
    private boolean isInDeleteMode = false;
    private boolean isInScheduleMode = false;
    private boolean canFinish;
    private SequenceListAdapter sequenceAdapter;
    private View currentMainWindow;

    private Profile guardian;
    private Profile selectedChild;

    private boolean nestedMode;
    private boolean assumeMinimize = true;
    private boolean childIsSet = false;

    public static Activity activityToKill;

    private int childId;

    private Helper helper;
    private GirafButton profileSelector;
    private GirafButton addButton;
    private GirafButton editButton;
    private GirafButton deleteButton;
    /**
     * Initializes all app elements.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);

        Intent i = getIntent();
        // Warn user and do not execute Tortoise if not launched from Giraf
        if (i.getExtras() == null) {
            GuiHelper.ShowToast(this, "Livshistorier skal startes fra GIRAF");

            finish();
            return ;
        }
        //Decide to load lifestories or schedules
        if (i.getIntExtra("app_to_start", -1) == 10)
            isInScheduleMode = true;

        // If launched from Giraf, then execute!
        Helper h = new Helper(this);
        // Set guardian- and child profiles
        LifeStory.getInstance().setGuardian(
                h.profilesHelper.getProfileById(i.getIntExtra("currentGuardianID", -1)));

        initializeButtons();

        overrideViews();
        if (i.getIntExtra("currentChildID", -1) == -1) {
            profileSelector.performClick();
        }else {
            LifeStory.getInstance().setChild(
                    h.profilesHelper.getProfileById(i.getIntExtra("currentChildID", -1)));
            // Initialize name of profile
        }
    }

    private void initializeButtons() {
        profileSelector = new GirafButton(this, getResources().getDrawable(R.drawable.icon_change_user));
        addButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_add));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sequence sequence = new Sequence();
                addSchedule(sequence, true, v);
            }
        });
        editButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_edit));
        deleteButton = new GirafButton(this,getResources().getDrawable(R.drawable.bin_closed));

        addGirafButtonToActionBar(profileSelector, LEFT);
        addGirafButtonToActionBar(addButton, RIGHT);
        addGirafButtonToActionBar(editButton, RIGHT);
        addGirafButtonToActionBar(deleteButton,RIGHT);
    }

    private void overrideViews() {
        profileSelector.setOnClickListener(new View.OnClickListener() {
            //Open Child Selector when pressing the Child Select Button
            @Override
            public void onClick(View v) {
                final GProfileSelector childSelector = new GProfileSelector(v.getContext(),
                        LifeStory.getInstance().getGuardian(),
                        null,
                        false);
                childSelector.show();

                childSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        //When child is selected, save Child locally and update application accordingly (Title name and Sequences)
                        LifeStory.getInstance().setChild(
                                new Helper(getApplicationContext()).profilesHelper.getProfileById((int) id));

                        //profileName.setText(LifeStory.getInstance().getChild().getName());

                        loadSeqGrid(LifeStory.getInstance().getChild());

                        childSelector.dismiss();
                    }
                });
                try{childSelector.backgroundCancelsDialog(false);}
                catch (Exception ignored){}
            }
        });

        editButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isInEditMode) { isInEditMode = false; }
                else { isInEditMode = v.isPressed(); }
                GridView sequenceGrid = (GridView) findViewById(R.id.sequence_grid);

                // Make sure that all views currently not visible will have the correct
                // editmode when they become visible
                sequenceAdapter.setEditModeEnabled(isInEditMode);

                //createButton.setVisibility(isInEditMode ? View.VISIBLE : View.GONE);

                // Update the editmode of all visible views in the grid
                for (int i = 0; i < sequenceGrid.getChildCount(); i++) {
                    View view = sequenceGrid.getChildAt(i);

                    if (view instanceof PictogramView) {
                        ((PictogramView) view).setEditModeEnabled(isInEditMode, false);
                    }
                }
            }
        });


        deleteButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View v){
                if(isInDeleteMode) {isInDeleteMode = false;}
                else {isInDeleteMode = v.isPressed();}
                GridView sequenceGrid = (GridView) findViewById(R.id.sequence_grid);

                sequenceAdapter.setDeleteModeEnabled(isInDeleteMode);
                for (int i = 0; i < sequenceGrid.getChildCount(); i++) {
                    View view = sequenceGrid.getChildAt(i);

                    if (view instanceof PictogramView) {
                        ((PictogramView) view).setEditModeEnabled(false, isInDeleteMode);
                    }
                }
            }
        });
    }

    private void setupModeFromIntents() {
        //Create helper to fetch data from database
        helper = new Helper(this);

        //Fetches intents (from Launcher or SequenceActivity)
        Bundle extras = getIntent().getExtras();

        //Makes the Activity killable from SequenceActivity and (Nested) MainActivity
        if (extras.getBoolean("insertSequence") == false) {
            activityToKill = this;
        }

        //Get GuardianId and ChildId from extras
        int guardianId = extras.getInt("currentGuardianID");
        childId = extras.getInt("currentChildID");

        //Save guardian locally (Fetch from Database by Id)
        guardian = helper.profilesHelper.getProfileById(guardianId);

        //Setup nestedMode if insertSequence extra is present
        /*if (extras.getBoolean("insertSequence")) {
            nestedMode = true;
            setupNestedMode();
            setChild();
        }
        //Make user pick a child and set up GuardianMode if ChildId is -1 (= Logged in as Guardian)
        else if (childId == -1) {
            pickAndSetChild();
            setupGuardianMode();
        }
        //Else setup application for a Child
        else {
            setupChildMode();
            setChild();
            childIsSet = true;
        }*/
    }
    private void loadSeqGrid(Profile profile){
        // Clear existing life stories
        LifeStory.getInstance().getStories().clear();
        if (isInScheduleMode)
            DBController.getInstance().loadCurrentProfileSequences(
                    profile.getId(), Sequence.SequenceType.SCHEDULE, getApplicationContext());
        else
            DBController.getInstance().loadCurrentProfileSequences(
                    profile.getId(), Sequence.SequenceType.STORY, getApplicationContext());

        // Initialize grid view
        GridView sequenceGrid = (GridView) findViewById(R.id.sequence_grid);
        sequenceAdapter = initAdapter();
        sequenceGrid.setAdapter(sequenceAdapter);


        // Load Sequence
        sequenceGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                canFinish = false;
                Intent i;
                if (isInScheduleMode){
                    if (isInEditMode) {
                        i = new Intent(getApplicationContext(), ScheduleEditActivity.class);
                        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
                        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
                        i.putExtra("isNew", false);
                        i.putExtra("template", arg2);
                        i.putExtra("EditMode", true);
                        i.putExtra("sequenceId", LifeStory.getInstance().getStories().get(arg2).getId());
                        startActivity(i);
                    } else {
                        i = new Intent(getApplicationContext(), ScheduleViewActivity.class);
                        i.putExtra("story", arg2);
                        startActivity(i);
                    }
                } else {
                    if (isInEditMode) {
                        i = new Intent(getApplicationContext(), EditModeActivity.class);
                        i.putExtra("template", arg2);
                        startActivity(i);
                    } else {
                        i = new Intent();
                        i.setComponent(new ComponentName("dk.aau.cs.giraf.sequenceviewer", "dk.aau.cs.giraf.sequenceviewer.MainActivity"));
                        i.putExtra("sequenceId", LifeStory.getInstance().getStories().get(arg2).getId());
                        Log.e("Tag", Integer.toString(LifeStory.getInstance().getStories().get(arg2).getMediaFrames().get(0).getContent().get(0).getPictogramID()));
                        i.putExtra("landscapeMode", true);
                        i.putExtra("visiblePictogramCount", 4);
                        i.putExtra("callerType", "Tortoise");
                        startActivityForResult(i, 0);
                    }
                }
            }
        });
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
    protected void onResume()
    {
        super.onResume();

        isInEditMode = false;
        editButton.setPressed(false);
        Profile child = LifeStory.getInstance().getChild();
        if(child != null) {
            //profileName.setText(child.getName());
            loadSeqGrid(child);
        }
        if(sequenceAdapter != null) {
            sequenceAdapter.setEditModeEnabled(isInEditMode);
            sequenceAdapter.notifyDataSetChanged();
        }
    }

    public void renderDialog(int dialogId, final int position) {
        final dk.aau.cs.giraf.tortoise.controller.Sequence seq;
        final LifeStory lifeStory = LifeStory.getInstance();
        final DBController dbc = DBController.getInstance();
        final MainActivity parentObj = this;

        // If isInTemplateMode is true then the guardian profile is active. If not, the child profile is active.

        seq = LifeStory.getInstance().getStories().get(position);

        // Dialog that prompts for deleting a story or template
        switch (dialogId) {
            case DIALOG_DELETE:
                GDialogMessage gdialog = new GDialogMessage(this,
                    R.drawable.ic_launcher,
                    getString(R.string.dialog_delete_title),
                    getResources().getString(R.string.dialog_delete_message) + " \"" + seq.getTitle() + "\"",
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dbc.deleteSequence(seq, parentObj);
                            lifeStory.removeStory(seq);

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

    public void addSchedule(Sequence s, boolean isNew, View v)
    {
        canFinish = false;
        Intent i = new Intent(this, ScheduleEditActivity.class);
        i.putExtra("template", -1);
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        i.putExtra("EditMode", true);
        i.putExtra("isNew", isNew);
        i.putExtra("sequenceId", s.getId());
        
        if (i.resolveActivity(getPackageManager()) != null)
        {
            try{
            startActivity(i);
            } catch (Exception ex)
            {
                GuiHelper.ShowToast(this, ex.toString());
            }
        } else
        {
            GuiHelper.ShowToast(getApplicationContext(), "Kunne ikke starte ugeplanlægger");
        }
    }

    public void addStory(View v)
    {
        canFinish = false;
        Intent i;
        if(isInScheduleMode){
            i = new Intent(getApplicationContext(), ScheduleEditActivity.class);
            i.putExtra("template", -1);
        }else {
            i = new Intent(getApplicationContext(), EditModeActivity.class);
            i.putExtra("template", -1);
        }

        startActivity(i);
    }
}
