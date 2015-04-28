package dk.aau.cs.giraf.tortoise.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GirafInflatableDialog;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.HorizontalSequenceViewGroup;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.MainActivity;
import dk.aau.cs.giraf.tortoise.PictogramView;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.SequenceAdapter;
import dk.aau.cs.giraf.tortoise.SequenceViewGroup;
import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleEditActivity extends ScheduleActivity implements SequenceAdapter.SelectedFrameAware {

    private final String DELETE_SEQUENCES_TAG = "DELETE_SEQUENCES_TAG";

    private Profile guardian;
    private Profile selectedChild;
    private boolean isInEditMode;
    private boolean emblemToggler = true;
    private boolean isNew;
    private boolean assumeMinimize = true;
    private boolean doingDelete;
    private boolean deletingSomething = false;
    public static boolean choiceMode = false;
    private int guardianId;
    private int childId;
    private int sequenceId;
    private int pictogramEditPos = -1;
    private String tempNameHolder;
    private ArrayList<boolean[]> markedFrames = new ArrayList<boolean[]>();

    public static dk.aau.cs.giraf.tortoise.controller.Sequence schedule;
    public static ArrayList<Sequence> daySequences = new ArrayList<Sequence>();
    public static Sequence mondaySequence;
    public static Sequence tuesdaySequence;
    public static Sequence wednesdaySequence;
    public static Sequence thursdaySequence;
    public static Sequence fridaySequence;
    public static Sequence saturdaySequence;
    public static Sequence sundaySequence;
    public static dk.aau.cs.giraf.tortoise.controller.Sequence choice = new dk.aau.cs.giraf.tortoise.controller.Sequence();


    public static SequenceAdapter scheduleAdapter;
    public static SequenceAdapter mondayAdapter;
    public static SequenceAdapter tuesdayAdapter;
    public static SequenceAdapter wednesdayAdapter;
    public static SequenceAdapter thursdayAdapter;
    public static SequenceAdapter fridayAdapter;
    public static SequenceAdapter saturdayAdapter;
    public static SequenceAdapter sundayAdapter;
    public static ArrayList<SequenceAdapter> adapterList;
    public static SequenceAdapter choiceAdapter;


    private List<MediaFrame> tempFrameList;
    private final String PICTO_ADMIN_PACKAGE = "dk.aau.cs.giraf.pictosearch";
    private final String PICTO_ADMIN_CLASS = PICTO_ADMIN_PACKAGE + "." + "PictoAdminMain";
    private final String PICTO_INTENT_CHECKOUT_ID = "checkoutIds";
    private final int PICTO_SEQUENCE_IMAGE_CALL = 2;
    private final int PICTO_EDIT_PICTOGRAM_CALL = 4;
    private final int PICTO_NEW_PICTOGRAM_CALL = 3;
    private final int NESTED_SEQUENCE_CALL = 40;
    private Helper helper;

    public List<List<ImageView>> weekdayLists;
    GDialog exitDialog;
    GirafInflatableDialog acceptDeleteDialog;
    private GirafButton scheduleImage;
    private GirafButton saveButton;
    private GirafButton binClosed;
    private EditText scheduleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null) {
            GuiHelper.ShowToast(this, "Ingen data modtaget");
            finish();
        }


        setContentView(R.layout.schedule_edit_activity);
        loadIntents();
        loadProfiles();
        initSequences(intent);
        setupFramesGrid();
        //setupButtons();
        initializeButtons();


        exitDialog = new GDialog(this, LayoutInflater.from(this).inflate(R.layout.dialog_schedule_exit, null));
    }

    private void loadIntents() {
        Bundle extras = getIntent().getExtras();
        childId = extras.getInt("currentChildID");
        sequenceId = extras.getInt("sequenceId");
        guardianId = extras.getInt("currentGuardianID");
        isInEditMode = extras.getBoolean("EditMode");
        isNew = extras.getBoolean("isNew");
    }

    private void loadProfiles() {
        //Create helper to load Child from Database
        helper = new Helper(this);
        selectedChild = helper.profilesHelper.getProfileById(childId);
        guardian = helper.profilesHelper.getProfileById(guardianId);
    }

    private void initSequences(Intent intent) {

        // Create new array of sequences.
        helper = new Helper(this);

        // Test if we get a template. Create new (empty) sequences if not.
        int template = intent.getIntExtra("template", -1);

        daySequences.clear();

        scheduleName = (EditText) findViewById(R.id.sequenceName);

        if(isNew)
        {
            LifeStory.getInstance().setCurrentStory(new Sequence());

            // add empty sequences for each week day
            mondaySequence = new Sequence();
            daySequences.add(0, mondaySequence);
            markedFrames.add(new boolean[0]);

            tuesdaySequence = new Sequence();
            daySequences.add(1, tuesdaySequence);
            markedFrames.add(new boolean[0]);

            wednesdaySequence = new Sequence();
            daySequences.add(2, wednesdaySequence);
            markedFrames.add(new boolean[0]);

            thursdaySequence = new Sequence();
            daySequences.add(3, thursdaySequence);
            markedFrames.add(new boolean[0]);

            fridaySequence = new Sequence();
            daySequences.add(4, fridaySequence);
            markedFrames.add(new boolean[0]);

            saturdaySequence = new Sequence();
            daySequences.add(5, saturdaySequence);
            markedFrames.add(new boolean[0]);

            sundaySequence = new Sequence();
            daySequences.add(6, sundaySequence);
            markedFrames.add(new boolean[0]);

            schedule = new Sequence();

            //scheduleName.setText("");
        }
        else
        {
            List<Sequence> seqs = DBController.getInstance().loadCurrentProfileSequencesAndFrames(childId, dk.aau.cs.giraf.dblib.models.Sequence.SequenceType.SCHEDULE, getApplicationContext());
            schedule = seqs.get(template);
            LifeStory.getInstance().setCurrentStory(schedule);

            int dayID = schedule.getMediaFrames().get(0).getNestedSequenceID();
            mondaySequence = DBController.getInstance().getSequenceFromID(dayID, this);
            Collections.sort(mondaySequence.getMediaFrames(), new Comparator<MediaFrame>() {
                public int compare(MediaFrame x, MediaFrame y) {
                    return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                }
            });
            daySequences.add(0, mondaySequence);
            markedFrames.add(new boolean[mondaySequence.getMediaFrames().size()]);

            int day1ID = schedule.getMediaFrames().get(1).getNestedSequenceID();
            tuesdaySequence = DBController.getInstance().getSequenceFromID(day1ID, this);
            Collections.sort(tuesdaySequence.getMediaFrames(), new Comparator<MediaFrame>() {
                public int compare(MediaFrame x, MediaFrame y) {
                    return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                }
            });
            daySequences.add(1, tuesdaySequence);
            markedFrames.add(new boolean[tuesdaySequence.getMediaFrames().size()]);

            int day2ID = schedule.getMediaFrames().get(2).getNestedSequenceID();
            wednesdaySequence = DBController.getInstance().getSequenceFromID(day2ID, this);
            Collections.sort(wednesdaySequence.getMediaFrames(), new Comparator<MediaFrame>() {
                public int compare(MediaFrame x, MediaFrame y) {
                    return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                }
            });
            daySequences.add(2, wednesdaySequence);
            markedFrames.add(new boolean[wednesdaySequence.getMediaFrames().size()]);

            int day3ID = schedule.getMediaFrames().get(3).getNestedSequenceID();
            thursdaySequence = DBController.getInstance().getSequenceFromID(day3ID, this);
            Collections.sort(thursdaySequence.getMediaFrames(), new Comparator<MediaFrame>() {
                public int compare(MediaFrame x, MediaFrame y) {
                    return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                }
            });
            daySequences.add(3, thursdaySequence);
            markedFrames.add(new boolean[thursdaySequence.getMediaFrames().size()]);

            int day4ID = schedule.getMediaFrames().get(4).getNestedSequenceID();
            fridaySequence = DBController.getInstance().getSequenceFromID(day4ID, this);
            Collections.sort(fridaySequence.getMediaFrames(), new Comparator<MediaFrame>() {
                public int compare(MediaFrame x, MediaFrame y) {
                    return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                }
            });
            daySequences.add(4, fridaySequence);
            markedFrames.add(new boolean[fridaySequence.getMediaFrames().size()]);

            int day5ID = schedule.getMediaFrames().get(5).getNestedSequenceID();
            saturdaySequence = DBController.getInstance().getSequenceFromID(day5ID, this);
            Collections.sort(saturdaySequence.getMediaFrames(), new Comparator<MediaFrame>() {
                public int compare(MediaFrame x, MediaFrame y) {
                    return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                }
            });
            daySequences.add(5, saturdaySequence);
            markedFrames.add(new boolean[saturdaySequence.getMediaFrames().size()]);

            int day6ID = schedule.getMediaFrames().get(6).getNestedSequenceID();
            sundaySequence = DBController.getInstance().getSequenceFromID(day6ID, this);
            Collections.sort(sundaySequence.getMediaFrames(), new Comparator<MediaFrame>() {
                public int compare(MediaFrame x, MediaFrame y) {
                    return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                }
            });
            daySequences.add(6, sundaySequence);
            markedFrames.add(new boolean[sundaySequence.getMediaFrames().size()]);

            //Set title text
            scheduleName.setText(schedule.getTitle(), EditText.BufferType.EDITABLE);
        }

        // Create listener on Parent View(s) to remove focus when touched
        createClearFocusListener(findViewById(R.id.parent_container));

        scheduleName.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        scheduleName.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    v.clearFocus();
                }
                return false;
            }
        });
    }

    private void createClearFocusListener(View view){
        // Create listener to remove focus from EditText when something else is touched
        if (!(view instanceof EditText)){
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    EditText editText = (EditText) findViewById(R.id.sequenceName);
                    editText.clearFocus();
                    return false;
                }
            });
        }

        if (view instanceof ViewGroup){
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++){
                View innerView = ((ViewGroup) view).getChildAt(i);
                createClearFocusListener(innerView);
            }
        }
    }

    private void setupFramesGrid() {
        // Create Adapter for the SequenceViewGroup (The Grid displaying the Sequence)

        mondayAdapter = new SequenceAdapter(this, daySequences.get(0), this);
        mondayAdapter = setupAdapter(mondayAdapter, daySequences.get(0));
        setupSequenceViewGroup(mondayAdapter, R.id.sequenceViewGroupMon);

        tuesdayAdapter = new SequenceAdapter(this, daySequences.get(1), this);
        tuesdayAdapter = setupAdapter(tuesdayAdapter, daySequences.get(1));
        setupSequenceViewGroup(tuesdayAdapter, R.id.sequenceViewGroup2);

        wednesdayAdapter = new SequenceAdapter(this, daySequences.get(2), this );
        wednesdayAdapter = setupAdapter(wednesdayAdapter, daySequences.get(2));
        setupSequenceViewGroup(wednesdayAdapter, R.id.sequenceViewGroup3);

        thursdayAdapter = new SequenceAdapter(this, daySequences.get(3), this);
        thursdayAdapter = setupAdapter(thursdayAdapter, daySequences.get(3));
        setupSequenceViewGroup(thursdayAdapter, R.id.sequenceViewGroup4);

        fridayAdapter = new SequenceAdapter(this, daySequences.get(4), this);
        fridayAdapter = setupAdapter(fridayAdapter, daySequences.get(4));
        setupSequenceViewGroup(fridayAdapter, R.id.sequenceViewGroup5);

        saturdayAdapter = new SequenceAdapter(this, daySequences.get(5), this);
        saturdayAdapter = setupAdapter(saturdayAdapter, daySequences.get(5));
        setupSequenceViewGroup(saturdayAdapter, R.id.sequenceViewGroup6);

        sundayAdapter = new SequenceAdapter(this, daySequences.get(6), this);
        sundayAdapter = setupAdapter(sundayAdapter, daySequences.get(6));
        setupSequenceViewGroup(sundayAdapter, R.id.sequenceViewGroup7);

        scheduleAdapter = new SequenceAdapter(this, schedule, this);
        scheduleAdapter = setupAdapter(scheduleAdapter, schedule);

        adapterList = new ArrayList<SequenceAdapter>();
        adapterList.add(mondayAdapter);
        adapterList.add(tuesdayAdapter);
        adapterList.add(wednesdayAdapter);
        adapterList.add(thursdayAdapter);
        adapterList.add(fridayAdapter);
        adapterList.add(saturdayAdapter);
        adapterList.add(sundayAdapter);
    }

    private void initializeButtons() {
        //scheduleImage = new GirafButton(this, getResources().getDrawable(R.drawable.no_image_big));
        scheduleImage = (GirafButton) findViewById(R.id.sequenceThumbnail);
        scheduleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditMode) {
                    callPictoAdmin(v, PICTO_SEQUENCE_IMAGE_CALL);
                }
            }
        });
        saveButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_save));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndShowSaveDialog(v);
            }
        });
        binClosed = new GirafButton(this,getResources().getDrawable(R.drawable.bin_closed));
        binClosed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(doingDelete){
                    if(deletingSomething) {

                        acceptDeleteDialog = GirafInflatableDialog.newInstance(
                                getApplicationContext().getString(R.string.delete_schedules),
                                getApplicationContext().getString(R.string.delete_this) + " "
                                        + getApplicationContext().getString(R.string.marked_schedules),
                                R.layout.dialog_delete);
                        acceptDeleteDialog.show(getSupportFragmentManager(), DELETE_SEQUENCES_TAG);
                    }
                    else {
                        deleteClick(v);
                    }
                }
                else {

                    initializeMarkingArrayList();
                    // Initialize marking array (use markedActivities)
                    doingDelete = true;
                    saveButton.setVisibility(View.INVISIBLE);
                    scheduleImage.setVisibility(View.GONE);
                    tempNameHolder = scheduleName.getText().toString();
                    scheduleName.setText("Deleting");
                    scheduleName.setEnabled(false);
                    for(SequenceAdapter a : adapterList){
                        a.setDraggability(false);
                        a.setMode(false);
                    }
                    setNewMode(false);
                }
            }
        });
        addGirafButtonToActionBar(saveButton, LEFT);
        addGirafButtonToActionBar(binClosed, RIGHT);

        //Set title image.

        int xy = 200;

        if (schedule.getTitlePictoId() != 0) {
            helper = new Helper(this);
            Drawable d = new BitmapDrawable(getResources(), helper.pictogramHelper.getPictogramById(schedule.getTitlePictoId()).getImage());
            scheduleImage.setIcon(resizeDrawable(d,xy,xy));
        }
        else {
            Drawable titleDrawable = getResources().getDrawable(R.drawable.add_sequence_picture);
            scheduleImage.setIcon(resizeDrawable(titleDrawable, xy, xy));
        }
    }

    private SequenceViewGroup setupSequenceViewGroup(final SequenceAdapter sAdapter, final int i) {
        //The SequenceViewGroup class takes care of most of the required functionality, including size properties, dragging and rearranging

        //Set up adapter to display the Sequence
        final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(i);
        sequenceGroup.setEditModeEnabled(isInEditMode);
        sequenceGroup.setAdapter(sAdapter);

        //When clicking the big "+", lift up the view and show the Add Dialog
        sequenceGroup.setOnNewButtonClickedListener(new SequenceViewGroup.OnNewButtonClickedListener() {
            @Override
            public void onNewButtonClicked(View v) {
                // update weekdaySelected
                currentWeekSection(v);
                if(!doingDelete){
                    final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(i);
                    sequenceGroup.liftUpAddNewButton();
                    createAndShowAddDialog(sequenceGroup, sAdapter);
                }
            }
        });

        //If clicking an item, save the position, save the Frame, and find out what kind of Frame it is. Then perform relevant action
        sequenceGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                // update weekdaySelected
                currentWeekSection(view);
                //Save Frame and Position
                MediaFrame frame = daySequences.get(weekdaySelected).getMediaFrames().get(position);

                if(doingDelete){
                    if(!isFrameMarked(frame)) {
                        deletingSomething = true;
                        markSequence(position, (PictogramView) view);
                        sAdapter.notifyDataSetChanged();
                    }
                    else{
                        unMarkSequence(position, view);
                        sAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    pictogramEditPos = position;
                    //Perform action depending on the type of pictogram clicked.
                    checkFrameMode(frame, view, sAdapter);
                }
            }
        });

        //Handle Rearrange
        sequenceGroup.setOnRearrangeListener(new SequenceViewGroup.OnRearrangeListener() {
            @Override
            public void onRearrange(int indexFrom, int indexTo) {
                sAdapter.notifyDataSetChanged();
            }
        });

        return sequenceGroup;
    }

    private void markSequence(int index, View view) {
        markedFrames.get(weekdaySelected)[index] = true;
        view.setBackgroundColor(getResources().getColor(R.color.giraf_page_indicator_active));
    }

    private void unMarkSequence(int index, View view) {
        markedFrames.get(weekdaySelected)[index] = false;
        view.setBackgroundDrawable(null);
    }

    public void deleteClick(View v) {

        if(deletingSomething) {
            acceptDeleteDialog.dismiss();
            deletingSomething = false;
            for(int i = 0; i < daySequences.size(); i++){
                for(int j = 0; j <daySequences.get(i).getMediaFrames().size(); j++){
                    if(markedFrames.get(i)[j]){
                        daySequences.get(i).getMediaFrames().remove(j);
                    }
                }
                markedFrames.set(i, new boolean[daySequences.get(i).getMediaFrames().size()]);
                adapterList.get(i).notifyDataSetChanged();
            }

        }
        doingDelete = false;
        //setNewMode(false);
        saveButton.setVisibility(View.VISIBLE);
        scheduleImage.setVisibility(View.VISIBLE);
        scheduleName.setText(tempNameHolder);
        scheduleName.setEnabled(true);
        for(SequenceAdapter a : adapterList){
            a.setDraggability(true);
            a.setMode(true);
        }
        setNewMode(true);


    }

    public void cancelDeleteClick(View v) {
        // Button to cancel delete of sequences
        acceptDeleteDialog.dismiss();
    }

    private void toggleEmblems(){
        List<SequenceViewGroup> seqVs = new ArrayList<SequenceViewGroup>();
        SequenceViewGroup seqV1 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroupMon);
        SequenceViewGroup seqV2 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup2);
        SequenceViewGroup seqV3 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup3);
        SequenceViewGroup seqV4 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup4);
        SequenceViewGroup seqV5 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup5);
        SequenceViewGroup seqV6 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup6);
        SequenceViewGroup seqV7 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup7);

        seqVs.add(seqV1);
        seqVs.add(seqV2);
        seqVs.add(seqV3);
        seqVs.add(seqV4);
        seqVs.add(seqV5);
        seqVs.add(seqV6);
        seqVs.add(seqV7);

        for(int i = 0; i < seqVs.size(); i++){
            for(int j = 0; j < seqVs.get(i).getChildCount(); j++){
                View view = seqVs.get(i).getChildAt(j);

                if (view instanceof PictogramView) {
                    ((PictogramView) view).setEditModeEnabled(emblemToggler);
                }
            }
        }
    }

    private void setNewMode(boolean value){
        if(emblemToggler != value){
            emblemToggler = value;
            toggleEmblems();
        }
    }

    private SequenceAdapter setupAdapter(final SequenceAdapter adapter, final Sequence s) {

        //Adds a Delete Icon to all Frames which deletes the relevant Frame on click.
        adapter.setOnAdapterGetViewListener(new SequenceAdapter.OnAdapterGetViewListener() {
            @Override
            public void onAdapterGetView(final int position, final View view) {
                if (view instanceof PictogramView) {
                    //Cast view to PictogramView so the onDeleteClickListener can be set
                    PictogramView v = (PictogramView) view;
                    v.setOnDeleteClickListener(new PictogramView.OnDeleteClickListener() {
                        @Override
                        public void onDeleteClick() {
                            //Remove frame and update Adapter
                            s.getMediaFrames().remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        return adapter;
    }

    private SequenceAdapter setupChoiceAdapter() {
        //Sets up the adapter for the Choice Frames
        final SequenceAdapter adapter = new SequenceAdapter(this, choice, this);

        //Adds a Delete Icon to all Frames which deletes the relevant Frame on click.
        adapter.setOnAdapterGetViewListener(new SequenceAdapter.OnAdapterGetViewListener() {
            @Override
            public void onAdapterGetView(final int position, final View view) {
                if (view instanceof PictogramView) {
                    //Cast view to PictogramView so the onDeleteClickListener can be set
                    PictogramView v = (PictogramView) view;
                    v.setOnDeleteClickListener(new PictogramView.OnDeleteClickListener() {
                        @Override
                        public void onDeleteClick() {
                            //Remove frame and update Adapter
                            choice.getMediaFrames().remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        return adapter;
    }

    private void initializeMarkingArrayList(){
        markedActivities = new ArrayList<boolean[]>();

        for(int i = 0; i < daySequences.size(); i++){
            boolean[] b = new boolean[daySequences.get(i).getMediaFrames().size()];
            markedActivities.add(i, b);
        }
    }


    @Override
    public void onResume() {
        // this method is also called after oncreate()
        // makes sure that current weekday is also marked after resume of the app
        super.onResume();
        List<SequenceViewGroup> seqVs = new ArrayList<SequenceViewGroup>();
        SequenceViewGroup seqV1 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroupMon);
        SequenceViewGroup seqV2 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup2);
        SequenceViewGroup seqV3 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup3);
        SequenceViewGroup seqV4 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup4);
        SequenceViewGroup seqV5 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup5);
        SequenceViewGroup seqV6 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup6);
        SequenceViewGroup seqV7 = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup7);

        seqVs.add(seqV1);
        seqVs.add(seqV2);
        seqVs.add(seqV3);
        seqVs.add(seqV4);
        seqVs.add(seqV5);
        seqVs.add(seqV6);
        seqVs.add(seqV7);

        for(int i = 0; i < seqVs.size(); i++){
            for(int j = 0; j < seqVs.get(i).getChildCount(); j++){
                View view = seqVs.get(i).getChildAt(j);

                if (view instanceof PictogramView) {
                    ((PictogramView) view).placeDown();
                }
            }
        }

        // mark the current weekday in the scheduler
        markCurrentWeekday();
    }

    public void weekdaySelected(View view) {
        markCurrentWeekday();
    }

    // this method handles pictograms sent back via an intent from pictosearch
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 2) {
            OnEditSequenceImageResult(data);
        }
        //Add content to NEW frame.
        else if (resultCode == RESULT_OK && requestCode == 3) {
            int i = currentViewGroup();
            final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(i);
            sequenceGroup.placeDownAddNewButton();

            OnNewPictogramResult(data);
        }
        //Edit content of frame.
        else if (resultCode == RESULT_OK && requestCode == 4) {

            OnEditPictogramResult(data);
        }
    }

    private void OnEditSequenceImageResult(Intent data) {

        int[] checkoutIds = data.getExtras().getIntArray(
                PICTO_INTENT_CHECKOUT_ID);

        if (checkoutIds.length == 0)
            return;

        schedule.setTitlePictoId(checkoutIds[0]);
        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
        Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
        bitmap = LayoutTools.getSquareBitmap(bitmap);
        bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 40);
        schedule.setTitleImage(bitmap);
        int xy = 120;
        scheduleImage.setIcon(resizeDrawable(new BitmapDrawable(getResources(), bitmap), xy, xy));
        scheduleAdapter.notifyDataSetChanged();
    }

    private int currentViewGroup(){
        switch(weekdaySelected){
            case 0:
                return R.id.sequenceViewGroupMon;
            case 1:
                return R.id.sequenceViewGroup2;
            case 2:
                return R.id.sequenceViewGroup3;
            case 3:
                return R.id.sequenceViewGroup4;
            case 4:
                return R.id.sequenceViewGroup5;
            case 5:
                return R.id.sequenceViewGroup6;
            case 6:
                return R.id.sequenceViewGroup7;
            default:
                throw new IllegalStateException("Illegal return from pictosearch.");
        }
    }

    private void OnNewPictogramResult(Intent data) {

        //If no pictures are returned, assume user canceled and nothing is supposed to change.
        int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");
        if (checkoutIds.length == 0 || checkoutIds == null) {
            return;
        }
        if (choiceMode) {

            for (int id : checkoutIds) {
                Pictogram pictogram = PictoFactory.getPictogram(getApplicationContext(), id);
                pictogram.setId(id);

                MediaFrame frame = new MediaFrame();
                frame.setPictogramId(id);
                frame.addContent(pictogram);

                choice.addFrame(frame);

                if (choice.getId() == 0) {
                    choice.setId(checkoutIds[0]);
                }
            }

            choiceAdapter.notifyDataSetChanged();
        } else {
            for (int id : checkoutIds) {
                Pictogram pictogram = PictoFactory.getPictogram(getApplicationContext(), id);
                pictogram.setId(id);

                MediaFrame frame = new MediaFrame();
                frame.setChoicePictogram(pictogram);
                frame.setPictogramId(id);
                frame.addContent(pictogram);

                daySequences.get(weekdaySelected).addFrame(frame);

            }
            markedFrames.set(weekdaySelected, new boolean[daySequences.get(weekdaySelected).getMediaFrames().size()]);
            adapterList.get(weekdaySelected).notifyDataSetChanged();
        }
    }

    private void OnEditPictogramResult(Intent data) {
        if (pictogramEditPos < 0)
            return;

        int[] checkoutIds = data.getExtras().getIntArray(
                PICTO_INTENT_CHECKOUT_ID);

        if (checkoutIds.length == 0)
            return;

        if (choiceMode) {

            MediaFrame frame = choice.getMediaFrames().get(pictogramEditPos);

            frame.setPictogramId(checkoutIds[0]);

            choiceAdapter.notifyDataSetChanged();

        } else {
            MediaFrame frame = daySequences.get(weekdaySelected).getMediaFrames().get(pictogramEditPos);

            frame.setPictogramId(checkoutIds[0]);

            adapterList.get(weekdaySelected).notifyDataSetChanged();
        }
    }
    // this is just a variable for a workaround
    //  public static LinearLayout weekdayLayout;
    public boolean saveSchedule(View v) {
        Sequence scheduleSeq = schedule;

        if (scheduleSeq.getTitlePictoId() == 0) {
            GuiHelper.ShowToast(this, "Skema er ikke gemt!, vælg et titel-pictogram");
            return false;
        }
        EditText scheduleName = (EditText) findViewById(R.id.sequenceName);
        String strTitle = scheduleName.getText().toString();
        if (strTitle.equals("") || strTitle.equals(getString(R.string.unnamed_sequence))) {
            // if no title, set a default one
            scheduleSeq.setTitle(getString(R.string.unnamed_sequence));
        } else {
            scheduleSeq.setTitle(strTitle);
        }


        if(DBController.getInstance().existScheduleSequence(scheduleSeq, getApplicationContext())){
            try {
                File file = new File(getApplicationContext().getFilesDir(), "progress" + String.valueOf(scheduleSeq.getId()) + ".bin");
                boolean deleted = file.delete();
            }
            catch(NullPointerException e){
                GuiHelper.ShowToast(this, "New progress to delete");
            }
            DBController.getInstance().deleteSequence(scheduleSeq, getApplicationContext());
            scheduleSeq.deleteAllMediaFrames();
            scheduleSeq.setId(0);
        }
        //Check whether the title has already been used
        List<Sequence> seqs = DBController.getInstance().getAllSequences(getApplicationContext());
        for (Sequence s : seqs) {
            if (scheduleSeq.getTitle().equals(s.getTitle())) {
                GuiHelper.ShowToast(this, "Navnet er allerede blevet brugt! Vælg et andet.");
                return false;
            }
        }


        boolean s1 = true;
        //Loops through the day's sequences and saves them to the database
        for (Sequence daySeq : daySequences) {
            daySeq.setTitle("");
            daySeq.setTitlePictoId(scheduleSeq.getTitlePictoId());
            daySeq.setId(0);
            s1 = s1 && DBController.getInstance().saveSequence(daySeq,
                    dk.aau.cs.giraf.dblib.models.Sequence.SequenceType.SCHEDULEDDAY,
                    childId, getApplicationContext());
            MediaFrame mf = new MediaFrame();
            mf.setNestedSequenceID(daySeq.getId());
            scheduleSeq.getMediaFrames().add(mf);
        }

        //Saves the week sequence with reference to the days
        boolean s2 = DBController.getInstance().saveSequence(scheduleSeq,
                dk.aau.cs.giraf.dblib.models.Sequence.SequenceType.SCHEDULE,
                childId, getApplicationContext());

        if (s1 && s2) {
            GuiHelper.ShowToast(this, "Skema gemt");
            return true;
        } else {
            GuiHelper.ShowToast(this, "Skema er ikke gemt!"); 
            return false;
        }
    }

    /*public void addPictograms(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "multi");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.this.startActivityForResult(i, 4);
    }*/

    /*public void chooseChoicePictogram(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.this.startActivityForResult(i, 5);
    }*/

    public void removeChoiceIcon(View v){
        daySequences.get(weekdaySelected).getMediaFrames().get(lastPosition).setChoicePictogram(null);
        //renderChoiceIcon(lastPosition, this);
    }

    public class DrawView extends View {
        Paint paint = new Paint();

        public DrawView(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            canvas.drawRect(30, 30, 80, 80, paint);
            paint.setStrokeWidth(0);
            paint.setColor(Color.CYAN);
            canvas.drawRect(33, 60, 77, 77, paint);
            paint.setColor(Color.YELLOW);
            canvas.drawRect(33, 33, 77, 60, paint);

        }

    }

    @Override
    public boolean isFrameMarked(MediaFrame frame) {
        for(int i = 0; i < daySequences.size(); i++){
            if(daySequences.get(i).getMediaFrames().contains(frame)){
                return markedFrames.get(i)[daySequences.get(i).getMediaFrames().indexOf(frame)];
            }
        }
        return false;
    }

    private void finishActivity(){
        assumeMinimize = false;
        finish();
    }
    private void callPictoAdmin(View v, int modeId) {
        assumeMinimize = false;
        Intent i = new Intent();
        i.setComponent(new ComponentName(PICTO_ADMIN_PACKAGE, PICTO_ADMIN_CLASS));
        i.putExtra("currentChildID", childId);
        i.putExtra("currentGuardianID", guardianId);



        if (modeId == PICTO_NEW_PICTOGRAM_CALL)
            i.putExtra("purpose", "multi");
        else
            i.putExtra("purpose", "single");

        ScheduleEditActivity.this.startActivityForResult(i, modeId);
    }

    private void checkFrameMode(MediaFrame frame, View v, final SequenceAdapter adapter) {

        if (frame.getNestedSequenceID() != 0) {
            createAndShowNestedDialog(v);

        } else if (frame.getContent().size() > 0) {
            createAndShowChoiceDialog(v, adapter);
        } else {
            callPictoAdmin(v, PICTO_EDIT_PICTOGRAM_CALL);
        }
    }


    private class AddDialog extends GDialog {

        private AddDialog(Context context, final SequenceAdapter adapter) {
            super(context);

            this.SetView(LayoutInflater.from(this.getContext()).inflate(R.layout.add_frame_dialog,null));

            GButton getSequence = (GButton) findViewById(R.id.get_sequence);
            GButton getPictogram = (GButton) findViewById(R.id.get_pictogram);
            GButton getChoice = (GButton) findViewById(R.id.get_choice);

            getSequence.setOnClickListener(new GButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    createAndShowNestedDialog(v);
                    dismiss();
                }
            });
            getPictogram.setOnClickListener(new GButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    callPictoAdmin(v, PICTO_NEW_PICTOGRAM_CALL);
                    dismiss();
                }
            });
            getChoice.setOnClickListener(new GButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    choiceMode = true;
                    dismiss();
                    createAndShowChoiceDialog(v, adapter);
                }
            });
        }
    }
    private class BackDialog extends GDialog {

        public BackDialog(Context context) {

            super(context);

            this.SetView(LayoutInflater.from(this.getContext()).inflate(R.layout.exit_sequence_dialog,null));

            GButton saveChanges = (GButton) findViewById(R.id.save_changes);
            GButton discardChanges = (GButton) findViewById(R.id.discard_changes);
            GButton cancel = (GButton) findViewById(R.id.return_to_editting);

            saveChanges.setOnClickListener(new GButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean sequenceOk;
                    sequenceOk = saveSchedule(v);
                    dismiss();
                    if (sequenceOk) {
                        finishActivity();
                    }
                }
            });

            discardChanges.setOnClickListener(new GButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    finishActivity();
                }
            });

            cancel.setOnClickListener(new GButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }
    private class ChoiceDialog extends GDialog {

        private boolean firstTimeOpening = false;

        private ChoiceDialog(Context context, final SequenceAdapter adapter) {
            super(context);

            final SequenceAdapter a = adapter;
            choice.getMediaFrames().clear();
            if ( pictogramEditPos != -1 ) {
                for (int i = 0; i < adapter.getItem(pictogramEditPos).getContent().size(); i++)
                {
                    MediaFrame frame = new MediaFrame();
                    int id = adapter.getItem(pictogramEditPos).getContent().get(i).getPictogramID();
                    frame.setPictogramId(id);
                    Pictogram pictogram = PictoFactory.getPictogram(getApplicationContext(), id);
                    pictogram.setId(id);
                    frame.addContent(pictogram);
                    choice.addFrame(frame);
                }
            }

            this.SetView(LayoutInflater.from(this.getContext()).inflate(R.layout.choice_dialog,null));

            GButton saveChoice = (GButton) findViewById(R.id.save_choice);
            GButton discardChoice = (GButton) findViewById(R.id.discard_choice);

            //Adapter to display a list of pictograms in the choice dialog
            choiceAdapter = setupChoiceAdapter();

            saveChoice.setOnClickListener(new GButton.OnClickListener(){

                @Override
                public void onClick(View v) {
                    tempFrameList = daySequences.get(weekdaySelected).getMediaFrames();
                    MediaFrame frame = new MediaFrame();
                    ArrayList<Pictogram> tempPictoList = new ArrayList<Pictogram>();

                    for(MediaFrame f : choice.getMediaFrames()){
                        for(Pictogram p : f.getContent()) {
                            tempPictoList.add(p);
                        }
                    }

                    if( tempPictoList.size() == 0) {
                        //TODO: Display message that user can not save empty choice.
                        return;
                    }
                    else{
                        frame.setContent(tempPictoList);
                        frame.setChoicePictogram(tempPictoList.get(0));
                        frame.setPictogramId(tempPictoList.get(0).getPictogramID());
                    }

                    if (pictogramEditPos == -1){
                        daySequences.get(weekdaySelected).addFrame(frame);
                        pictogramEditPos = tempFrameList.size()-1;
                    } else {
                        daySequences.get(weekdaySelected).getMediaFrames().get(pictogramEditPos).setContent(tempPictoList);
                        //daySequences.get(weekdaySelected).getMediaFrames().get(pictogramEditPos).setPictogramId(frame.getPictogramId());
                    }

                    adapter.notifyDataSetChanged();
                    choiceMode = false;
                    pictogramEditPos = -1;
                    dismiss();
                }
            });
            discardChoice.setOnClickListener(new GButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    choiceMode = false;
                    dismiss();
                }
            });
            setupChoiceGroup(choiceAdapter);
        }
        private HorizontalSequenceViewGroup setupChoiceGroup(
                final SequenceAdapter adapter) {
            final HorizontalSequenceViewGroup choiceGroup = (HorizontalSequenceViewGroup) findViewById(R.id.choice_view_group);
            choiceGroup.setEditModeEnabled(isInEditMode);
            choiceGroup.setAdapter(adapter);

            // Handle rearrange
            choiceGroup
                    .setOnRearrangeListener(new HorizontalSequenceViewGroup.OnRearrangeListener() {
                        @Override
                        public void onRearrange(int indexFrom, int indexTo) {
                            adapter.notifyDataSetChanged();
                        }
                    });



            // Handle new view
            choiceGroup
                    .setOnNewButtonClickedListener(new HorizontalSequenceViewGroup.OnNewButtonClickedListener() {
                        @Override
                        public void onNewButtonClicked(View v) {
                            final HorizontalSequenceViewGroup sequenceGroup = (HorizontalSequenceViewGroup) findViewById(R.id.choice_view_group);
                            sequenceGroup.liftUpAddNewButton();
                            choiceMode = true;
                            callPictoAdmin(v, PICTO_NEW_PICTOGRAM_CALL);
                        }
                    });

            choiceGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view,
                                        int position, long id) {
                    pictogramEditPos = position;
                    choiceMode = true;
                    callPictoAdmin(view, PICTO_EDIT_PICTOGRAM_CALL);
                }
            });


            return choiceGroup;
        }
    }

    private void createAndShowSaveDialog(View v) {
        //Creates a dialog for saving Sequence. If Sequence is saved succesfully, exit Activity
        GDialogMessage saveDialog = new GDialogMessage(v.getContext(), R.drawable.save,
                "Gem skema",
                "Du er ved at gemme skemaet",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean sequenceOk;
                        sequenceOk = saveSchedule(v);
                        if (sequenceOk) {
                            finishActivity();
                        }
                    }
                });
        saveDialog.show();
    }

    @Override
    public void onBackPressed() {
        //Create instance of BackDialog class and display it
        if(!doingDelete){
            BackDialog backDialog = new BackDialog(this);
            backDialog.show();
        }
        else{

            if(deletingSomething) {
                deletingSomething = false;
                markedFrames.clear();
                for(int j = 0; j < markedFrames.size(); j++){
                    markedFrames.set(j, new boolean[daySequences.get(j).getMediaFrames().size()]);
                    adapterList.get(j).notifyDataSetChanged();
                }
            }
            doingDelete = false;
            setNewMode(true);
            saveButton.setVisibility(View.VISIBLE);
            scheduleImage.setVisibility(View.VISIBLE);
            scheduleName.setText(tempNameHolder);
            scheduleName.setEnabled(true);
        }
    }

    private void createAndShowAddDialog(View v, final SequenceAdapter adapter) {
        //Create instance of AddDialog and display it
        AddDialog addFrame = new AddDialog(v.getContext(), adapter);
        addFrame.show();
    }

    private void createAndShowChoiceDialog(View v, final SequenceAdapter adapter) {
        //Create instance of ChoiceDialog and display it
        ChoiceDialog choiceDialog = new ChoiceDialog(v.getContext(), adapter);
        choiceDialog.show();
    }

    private void createAndShowNestedDialog(View v) {
        //Creates a Dialog for information. Clicking OK starts MainActivity in nestedMode
        GDialogMessage nestedDialog = new GDialogMessage(v.getContext(),
                //TODO: Find a better icon than the ic_launcher icon
                R.drawable.ic_launcher,
                "Åbner sekvensvalg",
                "Et nyt vindue åbnes, hvor du kan vælge en anden sekvens at indsætte",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        assumeMinimize = false;

                        //Put required Intents to set up Nested Mode
                        Intent intent = new Intent(getApplication(), MainActivity.class);
                        intent.putExtra("insertSequence", true);
                        intent.putExtra("currentGuardianID", guardian.getId());
                        intent.putExtra("currentChildID", childId);
                        startActivityForResult(intent, NESTED_SEQUENCE_CALL);
                    }
                });

        nestedDialog.show();
    }

    private void createAndShowErrorDialog(View v) {
        //Creates alertDialog to display error. Clicking Ok dismisses the Dialog
        GDialogAlert alertDialog = new GDialogAlert(v.getContext(), R.drawable.delete,
                "Fejl",
                "Du kan ikke gemme en tom Sekvens",
                new View.OnClickListener(){
                    @Override public void onClick(View v) {
                    }
                });
        alertDialog.show();
    }

    public void showExitDialog(View v){
        exitDialog.show();
    }

    public void exitDialogCancel(View v){
        exitDialog.dismiss();
    }
}