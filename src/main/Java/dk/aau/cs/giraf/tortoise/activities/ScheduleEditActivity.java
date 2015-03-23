package dk.aau.cs.giraf.tortoise.activities;

import android.app.Activity;
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
import android.text.Editable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Frame;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
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

public class ScheduleEditActivity extends ScheduleActivity {

    private Profile guardian;
    private Profile selectedChild;
    private boolean isInEditMode;
    private boolean isNew;
    private boolean assumeMinimize = true;
    public static boolean choiceMode = false;
    private int guardianId;
    private int childId;
    private int sequenceId;
    private int pictogramEditPos = -1;
    public static dk.aau.cs.giraf.tortoise.controller.Sequence schedule;
    public static dk.aau.cs.giraf.tortoise.controller.Sequence choice = new dk.aau.cs.giraf.tortoise.controller.Sequence();
    public static SequenceAdapter adapter;
    public static SequenceAdapter choiceAdapter;
    private List<MediaFrame> tempFrameList;
    private List<Pictogram> tempPictogramList = new ArrayList<Pictogram>();
    private GButton backButton;
    private GButton sequenceImageButton;
    private EditText sequenceTitleView;
    private final String PICTO_ADMIN_PACKAGE = "dk.aau.cs.giraf.pictosearch";
    private final String PICTO_ADMIN_CLASS = PICTO_ADMIN_PACKAGE + "." + "PictoAdminMain";
    private final String PICTO_INTENT_CHECKOUT_ID = "checkoutIds";
    private final int PICTO_SEQUENCE_IMAGE_CALL = 345;
    private final int PICTO_EDIT_PICTOGRAM_CALL = 456;
    private final int PICTO_NEW_PICTOGRAM_CALL = 567;
    private final int SEQUENCE_VIEWER_CALL = 1337;
    private final int NESTED_SEQUENCE_CALL = 40;
    public static Activity activityToKill;
    private Helper helper;
    private GDialog printAlignmentDialog;
    private File[] file;

    public List<List<ImageView>> weekdayLists;
    GDialog exitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null) {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }


        setContentView(R.layout.schedule_edit_activity);
        loadIntents();
        loadProfiles();
        initSequences(intent);
        setupFramesGrid();
        setupButtons();
        exitDialog = new GDialog(this, LayoutInflater.from(this).inflate(R.layout.dialog_schedule_exit, null));

        //TODO:This was never implemented.
        // check whether tablet is in portrait or landscape mode and set the layout accordingly
        // landscape mode shows mode days than portrait mode
       /* int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            isInLandscape = true;
            setContentView(R.layout.schedule_edit_activity);
        } else {
            isInLandscape = false;
            setContentView(R.layout.schedule_edit_activity_portrait);
        }*/

        //Create empty sequences if no existing sequence is received. Otherwise load this sequence.

    }

    private void loadIntents() {
        Bundle extras = getIntent().getExtras();
        childId = extras.getInt("childId");
        sequenceId = extras.getInt("sequenceId");
        guardianId = extras.getInt("guardianId");
        isInEditMode = extras.getBoolean("EditMode");
    }

    private void loadProfiles() {
        //Create helper to load Child from Database
        helper = new Helper(this);
        selectedChild = helper.profilesHelper.getProfileById(childId);
        guardian = helper.profilesHelper.getProfileById(guardianId);
    }

    private void initSequences(Intent intent) {

        // Create new array of sequences.
        weekdaySequences = new ArrayList<Sequence>();

        helper = new Helper(this);

        // Test if we get a template. Create new (empty) sequences if not.
        int template = intent.getIntExtra("template", -1);

        if(template == -1)
        {
            LifeStory.getInstance().setCurrentStory(new Sequence());

            // add empty sequences for each week day
            for (int i = 0; i < 7; i++)
            {
                weekdaySequences.add(i, new Sequence());
            }
            schedule = new Sequence();
            //showAddButtons();
        }
        else
        {
            schedule = LifeStory.getInstance().getStories().get(template);
            LifeStory.getInstance().setCurrentStory(schedule);

            for (int i = 0; i < 7; i++)
            {
                //Get id for the day corresponding to each sequence
                int dayID = LifeStory.getInstance().getStories().get(template).getMediaFrames().get(i).getNestedSequenceID();
                Sequence daySeq = DBController.getInstance().getSequenceFromID(dayID, this);
                Collections.sort(daySeq.getMediaFrames(), new Comparator<MediaFrame>() {
                    public int compare(MediaFrame x, MediaFrame y) {
                        return Integer.valueOf(x.getPosY()).compareTo(y.getPosY());
                    }
                });
                weekdaySequences.add(i, daySeq);
            }

            //Set title image.

            //Set title text
            EditText title = (EditText) findViewById(R.id.scheduleName);
            title.setText(schedule.getTitle());
            title.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            });
            title.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_ENTER) {
                        v.clearFocus();
                    }
                    return false;
                }
            });

            //After loading the sequences, render the schedule and show add buttons.
            renderSchedule(true);
        }


    }

    private void setupFramesGrid() {
        // Create Adapter for the SequenceViewGroup (The Grid displaying the Sequence)
        adapter = setupAdapter();
        setupSequenceViewGroup(adapter);
    }
    private void setupButtons() {
        //Creates all buttons in Activity and their listeners
        GButton saveButton = (GButton) findViewById(R.id.save_button);
        backButton = (GButton) findViewById(R.id.back_button);
        //If sticking with this way of doing xml, implement find title pictogram
        sequenceImageButton = (GButton) findViewById(R.id.sequence_image);

        saveButton.setOnClickListener(new ImageButton.OnClickListener() {
            //Show Dialog to save Sequence when clicking the Save Button
            @Override
            public void onClick(View v) {
                createAndShowSaveDialog(v);
            }
        });

        backButton.setOnClickListener(new ImageButton.OnClickListener() {
            //Show Back Dialog when clicking the Cancel Button
            @Override
            public void onClick(View v) {
                createAndShowBackDialog(v);
            }
        });

        sequenceImageButton.setOnClickListener(new ImageView.OnClickListener() {
            //If Sequence Image Button is clicked, call PictoAdmin to select an Image for the Sequence
            @Override
            public void onClick(View v) {
                if (isInEditMode) {
                    callPictoAdmin(PICTO_SEQUENCE_IMAGE_CALL);
                }
            }
        });

        //If no Image has been selected or the Sequence, display the Add Sequence Picture. Otherwise load the image for the Button
        if (schedule.getTitlePictoId() == 0) {
            Drawable d = getResources().getDrawable(R.drawable.add_sequence_picture);
            sequenceImageButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d);
        } else {
            helper = new Helper(this);
            Drawable d = new BitmapDrawable(getResources(), helper.pictogramHelper.getPictogramById(schedule.getTitlePictoId()).getImage());
            sequenceImageButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d);
        }
    }

    private SequenceViewGroup setupSequenceViewGroup(final SequenceAdapter adapter) {
        //The SequenceViewGroup class takes care of most of the required functionality, including size properties, dragging and rearranging

        //Set up adapter to display the Sequence
        final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup6);
        sequenceGroup.setEditModeEnabled(isInEditMode);
        sequenceGroup.setAdapter(adapter);

        //When clicking the big "+", lift up the view and show the Add Dialog
        sequenceGroup.setOnNewButtonClickedListener(new SequenceViewGroup.OnNewButtonClickedListener() {
            @Override
            public void onNewButtonClicked() {
                final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup6);
                sequenceGroup.liftUpAddNewButton();
                createAndShowAddDialog(sequenceGroup);
            }
        });

        //If clicking an item, save the position, save the Frame, and find out what kind of Frame it is. Then perform relevant action
        sequenceGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

                //Save Frame and Position
                pictogramEditPos = position;
                MediaFrame frame = schedule.getMediaFrames().get(position);

                //Perform action depending on the type of pictogram clicked.
                checkFrameMode(frame, view);
            }
        });

        //Handle Rearrange
        sequenceGroup.setOnRearrangeListener(new SequenceViewGroup.OnRearrangeListener() {
            @Override
            public void onRearrange(int indexFrom, int indexTo) {
                adapter.notifyDataSetChanged();
            }
        });

        return sequenceGroup;
    }

    private SequenceAdapter setupAdapter() {
        //Sets up the adapter for the Sequence to display
        final SequenceAdapter adapter = new SequenceAdapter(this, schedule);

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
                            schedule.getMediaFrames().remove(position);
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
        final SequenceAdapter adapter = new SequenceAdapter(this, choice);

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


    @Override
    public void onResume() {
        // this method is also called after oncreate()
        // makes sure that current weekday is also marked after resume of the app
        super.onResume();

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
            // i think this is triggered when a profile image is chosen from pictosearch
            try {

                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");

                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                } else {
                    try {
                        //LifeStory.getInstance().setCurrentStory(new Sequence());
                        LifeStory.getInstance().getCurrentStory().setTitlePictoId(checkoutIds[0]);
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
                        bitmap = LayoutTools.getSquareBitmap(bitmap);
                        bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 20);
                        LifeStory.getInstance().getCurrentStory().setTitleImage(bitmap);
                        // TODO: fix this to not just write on top of image
                        GButton storyImage = (GButton) findViewById(R.id.schedule_image_button);
                        storyImage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, new BitmapDrawable(getResources(), bitmap));
                    }
                    //We expect a null pointer exception if the pictogram is without image
                    catch (NullPointerException e) {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (Exception e) {
                GuiHelper.ShowToast(this, e.toString() + " - rcode 2.");
            }
        }
        //Add content to NEW frame.
        else if (resultCode == RESULT_OK && requestCode == 3) {
            try {
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");

                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                } else // when pictograms are received
                {
                    try {

                        final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup);
                        sequenceGroup.placeDownAddNewButton();

                        OnNewPictogramResult(data, getApplicationContext());

                        MediaFrame mf = new MediaFrame();

                        addContentToMediaFrame(mf, checkoutIds);

                        weekdaySequences.get(weekdaySelected).getMediaFrames().add(mf);

                        if (weekdayLayout.getChildCount() > 0) {
                            weekdayLayout.removeViewAt(weekdayLayout.getChildCount() - 1); // remove add button
                        }
                        // add item to scroll view
                        addItems(mf, weekdayLayout);
                        weekdayLayout.addView(addButton()); // add the add button again


                    } catch (NullPointerException e) {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (NullPointerException e) {
                GuiHelper.ShowToast(this, "Fejl");
            }
        }
        //Edit content of frame.
        else if (resultCode == RESULT_OK && requestCode == 4) {
            try {
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");

                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt.");
                } else {
                    MediaFrame mf = weekdaySequences.get(weekdaySelected).getMediaFrame(lastPosition);

                    addContentToMediaFrame(mf, checkoutIds);
                    /*for (int id : checkoutIds) {
                        Pictogram pictogram = PictoFactory.getPictogram(this, id);
                        mediaFrame.addContent(pictogram);
                    }*/

                    renderSchedule(true);
                    updateMultiChoiceDialog(lastPosition);
                }
            } catch (Exception e) {
                GuiHelper.ShowToast(this, e.toString());
            }
        }
        //Change choice icon
        else if (resultCode == RESULT_OK && requestCode == 5){
            try{
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    Toast t = Toast.makeText(this, "Ingen pictogrammer valgt.", Toast.LENGTH_LONG);
                    t.show();
                }
                else
                {
                    try{
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        weekdaySequences.get(weekdaySelected).getMediaFrames().get(lastPosition).setChoicePictogram(picto);
                        renderChoiceIcon(lastPosition, this);
                    }
                    //We expect a null pointer exception if the pictogram is without image
                    catch (NullPointerException e){
                        Toast t = Toast.makeText(this, "Der skete en uventet fejl.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            } catch (Exception e){
                GuiHelper.ShowToast(this, e.toString());
            }
        }
    }

    private void OnNewPictogramResult(Intent data, Context con) {
        int[] checkoutIds = data.getExtras().getIntArray(
                PICTO_INTENT_CHECKOUT_ID);

        //If no pictures are returned, assume user canceled and nothing is supposed to change.
        if (checkoutIds.length == 0 || checkoutIds == null) {
            return;
        }
        if (choiceMode) {

            for (int id : checkoutIds) {
                Pictogram pictogram = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                pictogram.setId(id);
                tempPictogramList.add(pictogram);


                MediaFrame frame = new MediaFrame();
                frame.setPictogramId(id);
                choice.addFrame(frame);

                if (choice.getId() == 0) {
                    choice.setId(checkoutIds[0]);
                }
            }

            choiceAdapter.notifyDataSetChanged();
        } else {

            for (int id : checkoutIds) {
                MediaFrame frame = new MediaFrame();
                frame.setPictogramId(id);
                schedule.addFrame(frame);
            }

            if (schedule.getId() == 0 && checkoutIds.length > 0) {
                schedule.setId(checkoutIds[0]);
                helper = new Helper(this);
                Drawable d = new BitmapDrawable(getResources(), helper.pictogramHelper.getPictogramById(schedule.getId()).getImage());
                sequenceImageButton.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
                sequenceImageButton.setVisibility(View.GONE);
                sequenceImageButton.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
    }
    // this is just a variable for a workaround
    //  public static LinearLayout weekdayLayout;
    public boolean saveSchedule(View v) {
        Sequence scheduleSeq = LifeStory.getInstance().getCurrentStory();

        if (scheduleSeq.getTitlePictoId() == 0) {
            GuiHelper.ShowToast(this, "Skema er ikke gemt!, vælg et titel-pictogram");
            return false;
        }
        Editable title = ((EditText) findViewById(R.id.scheduleName)).getText();
        String strTitle = title.toString();
        if (strTitle.equals("")) {
            // if no title, set a default one
            scheduleSeq.setTitle(getString(R.string.unnamed_sequence));
        } else {
            scheduleSeq.setTitle(strTitle);
        }


        if(DBController.getInstance().existScheduleSequence(scheduleSeq, getApplicationContext())){
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
        for (Sequence daySeq : super.weekdaySequences) {
            daySeq.setTitle("");
            daySeq.setTitlePictoId(scheduleSeq.getTitlePictoId());
            daySeq.setId(0);
            s1 = s1 && DBController.getInstance().saveSequence(daySeq,
                    dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULEDDAY,
                    LifeStory.getInstance().getChild().getId(),
                    getApplicationContext());
            MediaFrame mf = new MediaFrame();
            mf.setNestedSequenceID(daySeq.getId());
            scheduleSeq.getMediaFrames().add(mf);
        }

        //Saves the week sequence with reference to the days
        boolean s2 = DBController.getInstance().saveSequence(scheduleSeq,
                dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE,
                LifeStory.getInstance().getChild().getId(),
                getApplicationContext());

        if (s1 && s2) {
            return true;
        } else {
            return false;
        }
    }

    private boolean overrideSchedule(View v, Sequence scheduleSeq) {
        Sequence seq = DBController.getInstance().getSequenceFromID(scheduleSeq.getId(), getApplicationContext());

        boolean hasChanged = false;
        //Checks whether the title of the new schedule is different from the one in the database
        if(!scheduleSeq.getTitle().equals(seq.getTitle())) {
            seq.setTitle(scheduleSeq.getTitle());
            hasChanged = true;
        }
        //Checks whether the title pictogram of the new schedule is different from the one in the database
        if(!(scheduleSeq.getTitlePictoId() == seq.getTitlePictoId())) {
            seq.setTitlePictoId(scheduleSeq.getTitlePictoId());
            hasChanged = true;
        }
        List<MediaFrame> newFrames = scheduleSeq.getMediaFrames();
        List<MediaFrame> oldFrames = seq.getMediaFrames();

        //Loops through all the days and checks whether there are any changes


        if (hasChanged) {
            GuiHelper.ShowToast(this, "Ændringer er gemt");
            return true;
        }
        else {
            GuiHelper.ShowToast(this, "Der var ikke nogle ændringer");
            return false;
        }
    }

    public void addPictograms(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "multi");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.this.startActivityForResult(i, 4);
    }

    public void chooseChoicePictogram(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.this.startActivityForResult(i, 5);
    }

    public void removeChoiceIcon(View v){
        weekdaySequences.get(weekdaySelected).getMediaFrames().get(lastPosition).setChoicePictogram(null);
        renderChoiceIcon(lastPosition, this);
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
    private void finishActivity(){
        assumeMinimize = false;
        finish();
    }
    private void callPictoAdmin(int modeId) {
        assumeMinimize = false;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PICTO_ADMIN_PACKAGE, PICTO_ADMIN_CLASS));
        intent.putExtra("currentChildID", selectedChild.getId());
        intent.putExtra("currentGuardianID", guardian.getId());

        if (modeId == PICTO_NEW_PICTOGRAM_CALL)
            intent.putExtra("purpose", "multi");
        else
            intent.putExtra("purpose", "single");

        startActivityForResult(intent, modeId);
    }

    private void checkFrameMode(MediaFrame frame, View v) {

        if (frame.getNestedSequenceID() != 0) {
            createAndShowNestedDialog(v);

        } else if (frame.getContent().size() > 0) {
            createAndShowChoiceDialog(v);
        } else {
            callPictoAdmin(PICTO_EDIT_PICTOGRAM_CALL);
        }
    }


        private class AddDialog extends GDialog {

        private AddDialog(Context context) {
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
                    callPictoAdmin(PICTO_NEW_PICTOGRAM_CALL);
                    dismiss();
                }
            });
            getChoice.setOnClickListener(new GButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    choiceMode = true;
                    dismiss();
                    createAndShowChoiceDialog(v);
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

        private ChoiceDialog(Context context) {
            super(context);

            choice.getMediaFrames().clear();
            if ( pictogramEditPos != -1 ) {
                for (int i = 0; i < adapter.getItem(pictogramEditPos).getContent().size(); i++)
                {
                    MediaFrame frame = new MediaFrame();
                    frame.setPictogramId(adapter.getItem(pictogramEditPos).getContent().get(i).getId());
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
                    tempFrameList = schedule.getMediaFrames();
                    MediaFrame frame = new MediaFrame();
                    if(tempPictogramList == null) {
                        //TODO: Display message that user can not save empty choice.
                        return;
                    }
                    frame.setContent(tempPictogramList);
                    frame.setPictogramId(tempPictogramList.get(0).getId());


                    if (pictogramEditPos == -1){
                        schedule.addFrame(frame);
                        pictogramEditPos = tempFrameList.size()-1;
                    } else {
                        schedule.getMediaFrames().get(pictogramEditPos).setContent(tempPictogramList);
                    }
                    adapter.notifyDataSetChanged();
                    choiceMode = false;
                    pictogramEditPos = -1;
                    dismiss();
                }
            });
            discardChoice.setOnClickListener(new GButton.OnClickListener(){
                @Override
                public void onClick(View v) {
                    choiceMode = false;
                    dismiss();
                }
            });
            setupChoiceGroup(choiceAdapter);
        }

        private SequenceViewGroup setupChoiceGroup(
                final SequenceAdapter adapter) {
            final SequenceViewGroup choiceGroup = (SequenceViewGroup) findViewById(R.id.choice_view_group);
            choiceGroup.setEditModeEnabled(isInEditMode);
            choiceGroup.setAdapter(adapter);

            // Handle rearrange
            choiceGroup
                    .setOnRearrangeListener(new SequenceViewGroup.OnRearrangeListener() {
                        @Override
                        public void onRearrange(int indexFrom, int indexTo) {
                            adapter.notifyDataSetChanged();
                        }
                    });



            // Handle new view
            choiceGroup
                    .setOnNewButtonClickedListener(new SequenceViewGroup.OnNewButtonClickedListener() {
                        @Override
                        public void onNewButtonClicked() {
                            final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(R.id.choice_view_group);
                            sequenceGroup.liftUpAddNewButton();

                            callPictoAdmin(PICTO_NEW_PICTOGRAM_CALL);
                        }
                    });

            choiceGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view,
                                        int position, long id) {
                    pictogramEditPos = position;
                    callPictoAdmin(PICTO_EDIT_PICTOGRAM_CALL);
                }
            });


            return choiceGroup;
        }
    }

    private void createAndShowSaveDialog(View v) {
        //Creates a dialog for saving Sequence. If Sequence is saved succesfully, exit Activity
        GDialogMessage saveDialog = new GDialogMessage(v.getContext(), R.drawable.save,
                "Gem Sekvens",
                "Du er ved at gemme sekvensen",
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

    private void createAndShowBackDialog(View v) {
        //Create instance of BackDialog class and display it
        BackDialog backDialog = new BackDialog(v.getContext());
        backDialog.show();
    }

    private void createAndShowAddDialog(View v) {
        //Create instance of AddDialog and display it
        AddDialog addFrame = new AddDialog(v.getContext());
        addFrame.show();
    }

    private void createAndShowChoiceDialog(View v) {
        //Create instance of ChoiceDialog and display it
        ChoiceDialog choiceDialog = new ChoiceDialog(v.getContext());
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