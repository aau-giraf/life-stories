package dk.aau.cs.giraf.tortoise;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GProfileSelector;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafInflatableDialog;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
//import dk.aau.cs.giraf.oasis.lib.models.Sequence;
import dk.aau.cs.giraf.tortoise.activities.ScheduleEditActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.activities.TortoiseActivity;
import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;
import dk.aau.cs.giraf.tortoise.PictogramView.OnDeleteClickListener;
import dk.aau.cs.giraf.tortoise.SequenceListAdapter.OnAdapterGetViewListener;
import dk.aau.cs.giraf.tortoise.activities.EditModeActivity;

public class MainActivity extends TortoiseActivity implements SequenceListAdapter.SelectedSequenceAware {

    private final int DIALOG_DELETE = 1;
    private final String DELETE_SEQUENCES_TAG = "DELETE_SEQUENCES_TAG";

    private boolean isInEditMode = false;
    private boolean isInScheduleMode = false;
    private boolean canFinish;
    private boolean markingMode = false;
    private boolean isChildSet = false;
    private SequenceListAdapter sequenceAdapter;
    GridView sequenceGrid;
    private List<Sequence> schedules;
    private Set<Sequence> markedSequences = new HashSet<Sequence>();
    private View currentMainWindow;

    private Profile guardian;
    private Profile selectedChild;

    private boolean nestedMode;
    private boolean assumeMinimize = true;
    private boolean childIsSet = false;

    public static Activity activityToKill;

    private int childId;

    private Helper helper;
    GirafInflatableDialog acceptDeleteDialog;
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

        helper = new Helper(this);
        // Set guardian- and child profiles
        //LifeStory.getInstance().setGuardian(
        //        helper.profilesHelper.getProfileById(i.getIntExtra("currentGuardianID", -1)));

        setupSequenceGridView();

        initializeButtons();

        setupModeFromIntents();

        //overrideViews();
        /*if (i.getIntExtra("currentChildID", -1) == -1) {
            profileSelector.performClick();
        }else {
            LifeStory.getInstance().setChild(
                    helper.profilesHelper.getProfileById(i.getIntExtra("currentChildID", -1)));
            isChildSet = true;
            // Initialize name of profile
        }*/
    }

    private void setupSequenceGridView() {

        sequenceGrid = (GridView) findViewById(R.id.sequence_grid);

        //sequenceAdapter = new SequenceListAdapter(MainActivity.this, sequences, MainActivity.this);
        //sequenceGrid.setAdapter(sequenceAdapter);
    }

    private void initializeButtons() {
        profileSelector = new GirafButton(this, getResources().getDrawable(R.drawable.icon_change_user));
        addButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_add));
        deleteButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_delete));
        editButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_edit));

        profileSelector.setOnClickListener(new View.OnClickListener() {
            //Open Child Selector when pressing the Child Select Button
            @Override
            public void onClick(View v) {
                final GProfileSelector childSelector = new GProfileSelector(v.getContext(),
                        guardian,
                        null,
                        false);
                childSelector.show();

                childSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        //When child is selected, save Child locally and update application accordingly (Title name and Sequences)
                        selectedChild = helper.profilesHelper.getProfileById((int) id);
                        childId = (int) id;
                        schedules = DBController.getInstance().loadCurrentProfileSequencesAndFrames(
                                childId, dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, getApplicationContext());
                        //profileName.setText(LifeStory.getInstance().getChild().getName());

                        loadSeqGrid();
                        isChildSet = true;
                        childSelector.dismiss();
                    }
                });
                try{childSelector.backgroundCancelsDialog(false);}
                catch (Exception ignored){}
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sequence sequence = new Sequence();
                addSchedule(sequence, true, v);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            // Opens a dialog to remove the selected sequences
            @Override
            public void onClick(View v) {
                acceptDeleteDialog = GirafInflatableDialog.newInstance(
                        getApplicationContext().getString(R.string.delete_schedules),
                        getApplicationContext().getString(R.string.delete_this) + " "
                                + getApplicationContext().getString(R.string.marked_schedules),
                        R.layout.dialog_delete);
                acceptDeleteDialog.show(getSupportFragmentManager(), DELETE_SEQUENCES_TAG);
            }
        });
        deleteButton.setVisibility(View.GONE);

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
                        ((PictogramView) view).setEditModeEnabledForMain(isInEditMode);
                    }
                }
            }
        });

        addGirafButtonToActionBar(profileSelector, LEFT);
        addGirafButtonToActionBar(addButton, RIGHT);
        addGirafButtonToActionBar(editButton, RIGHT);
        addGirafButtonToActionBar(deleteButton, RIGHT);
    }

    /*private void overrideViews() {

    }*/

    private void setupModeFromIntents() {
        //Create helper to fetch data from database and fetches intents (from Launcher or AddEditSequencesActivity)

        Bundle extras = getIntent().getExtras();
        int guardianId;

        //Get GuardianId and ChildId from extras
        guardianId = extras.getInt("currentGuardianID");
        childId = extras.getInt("currentChildID");

        //Save guardian locally (Fetch from Database by Id)
        guardian = helper.profilesHelper.getProfileById(guardianId);

        //Make user pick a child and set up GuardianMode if ChildId is -1 (= Logged in as Guardian)
         if(childId == -1){
            pickAndSetChild();
            schedules = DBController.getInstance().loadCurrentProfileSequencesAndFrames(
                     childId, dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, getApplicationContext());
            //loadSeqGrid();
        }
        //Else setup application for a Child
        else {
            schedules = DBController.getInstance().loadCurrentProfileSequencesAndFrames(
                     childId, dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, getApplicationContext());
            setupChildMode();
            setChild();
            isChildSet = true;
        }
    }

    private void pickAndSetChild() {
        //Create ProfileSelector to make Guardian select Child
        final GProfileSelector childSelector = new GProfileSelector(this, guardian, null, false);

        //When child is selected, save Child locally and update application accordingly (Title name and Sequences)
        childSelector.setOnListItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedChild = helper.profilesHelper.getProfileById((int) id);
                childId = (int) id;
                schedules = DBController.getInstance().loadCurrentProfileSequencesAndFrames(
                        childId, dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, getApplicationContext());
                //profileName.setText(LifeStory.getInstance().getChild().getName());

                setChild();
                isChildSet = true;
                childSelector.dismiss();
                loadSeqGrid();
            }
        });
        childSelector.show();
    }

    // Sets up child mode - only possible to view sequences
    private void setupChildMode() {
        //When clicking a Sequence, lift up the view, create Intent for SequenceViewer and launch it
        sequenceGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                ((PictogramView) arg1).liftUp();

                Intent i = new Intent(getApplicationContext(), ScheduleViewActivity.class);
                final Sequence sequence = sequenceAdapter.getItem(position);
                i.putExtra("currentChildID", selectedChild.getId());
                i.putExtra("currentGuardianID", guardian.getId());
                i.putExtra("story", position);
                i.putExtra("sequenceId", sequence.getId());
                startActivity(i);
                //Create Intent with relevant Extras
            }
        });

        addButton.setVisibility(View.INVISIBLE);
        deleteButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.INVISIBLE);
        profileSelector.setVisibility(View.INVISIBLE);
    }

    private void loadSeqGrid(){
        // Clear existing life stories
        /*LifeStory.getInstance().getStories().clear();
        if (isInScheduleMode)
            DBController.getInstance().loadCurrentProfileSequences(
                    profile.getId(), Sequence.SequenceType.SCHEDULE, getApplicationContext());
        else
            DBController.getInstance().loadCurrentProfileSequences(
                    profile.getId(), Sequence.SequenceType.STORY, getApplicationContext());

        // Initialize grid view
        sequenceGrid = (GridView) findViewById(R.id.sequence_grid);
        sequenceAdapter = initAdapter();
        sequenceGrid.setAdapter(sequenceAdapter);*/


        sequenceGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                markingMode = true;
                Sequence sequence = sequenceAdapter.getItem(position);
                markSequence(sequence, view);
                deleteButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.INVISIBLE);
                addButton.setVisibility(View.GONE);
                return true;
            }
        });

        // Load Sequence
        sequenceGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                canFinish = false;

                Intent i;
                final Sequence sequence = sequenceAdapter.getItem(arg2);
                selectedChild = helper.profilesHelper.getProfileById(childId);

                if(!markingMode){
                    if (isInScheduleMode){
                        if (isInEditMode) {
                            i = new Intent(getApplicationContext(), ScheduleEditActivity.class);
                            i.putExtra("currentChildID", selectedChild.getId());
                            i.putExtra("currentGuardianID", guardian.getId());
                            i.putExtra("isNew", false);
                            i.putExtra("template", arg2);
                            i.putExtra("EditMode", true);
                            i.putExtra("sequenceId", sequence.getId());
                            startActivity(i);
                        } else {
                            i = new Intent(getApplicationContext(), ScheduleViewActivity.class);
                            i.putExtra("currentChildID", selectedChild.getId());
                            i.putExtra("currentGuardianID", guardian.getId());
                            i.putExtra("story", arg2);
                            i.putExtra("sequenceId", sequence.getId());
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
                            i.putExtra("sequenceId", sequence.getId());
                            Log.e("Tag", Integer.toString(sequence.getMediaFrames().get(0).getContent().get(0).getPictogramID()));
                            i.putExtra("landscapeMode", true);
                            i.putExtra("visiblePictogramCount", 4);
                            i.putExtra("callerType", "Tortoise");
                            startActivityForResult(i, 0);
                        }
                    }
                }
                else
                {
                    if (markedSequences.contains(sequence))
                    {
                        unMarkSequence(sequence, arg1);
                    } else {
                        markSequence(sequence, arg1);
                    }
                }
            }
        });
    }

    private void markSequence(Sequence sequence, View view) {
        markedSequences.add(sequence);
        view.setBackgroundColor(getResources().getColor(R.color.giraf_page_indicator_active));
    }

    private void unMarkSequence(Sequence c, View view) {
        markedSequences.remove(c);
        view.setBackgroundDrawable(null);
    }

    public void deleteClick(View v) {
        // Button to accept delete of sequences
        acceptDeleteDialog.dismiss();
        // Delete all selected items
        for (Sequence seq : markedSequences) {
            DBController.getInstance().deleteSequence(seq, getApplicationContext());
            schedules.remove(seq);//Check to whether cascading delete
            markedSequences.remove(seq);
        }
        AsyncFetchDatabase fetchDatabaseSetChild = new AsyncFetchDatabase();
        fetchDatabaseSetChild.execute();
        sequenceAdapter.notifyDataSetChanged(); // Needs fixing
        markingMode = false;
        addButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
    }

    public void cancelDeleteClick(View v) {
        // Button to cancel delete of sequences
        acceptDeleteDialog.dismiss();
        markingMode = false;
    }

    public SequenceListAdapter initAdapter() {

        final SequenceListAdapter adapter = new SequenceListAdapter(MainActivity.this, schedules, MainActivity.this);

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

    /*@Override
    protected void onResume()
    {
        super.onResume();


    }*/

    public void renderDialog(int dialogId, final int position) {
        final Sequence seq;
        final LifeStory lifeStory = LifeStory.getInstance();
        final DBController dbc = DBController.getInstance();
        final MainActivity parentObj = this;

        // If isInTemplateMode is true then the guardian profile is active. If not, the child profile is active.

        seq = schedules.get(position);

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

                            sequenceAdapter.setItems(schedules);
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
        i.putExtra("currentChildID", selectedChild.getId());
        i.putExtra("currentGuardianID", guardian.getId());
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

    private synchronized void setChild() {
        //Save Child locally and update relevant information for application
        selectedChild = helper.profilesHelper.getProfileById(childId);
        this.setActionBarTitle(getResources().getString(R.string.app_name_week_schedule) + " - " + selectedChild.getName()); // selectedChild.getName() "Child's name code"

        /*sequenceAdapter = initAdapter();
        sequenceGrid.setAdapter(sequenceAdapter);
        loadSeqGrid();*/
        // AsyncTask thread
        AsyncFetchDatabase fetchDatabaseSetChild = new AsyncFetchDatabase();
        fetchDatabaseSetChild.execute();
    }

    @Override
    public boolean isSequenceMarked(Sequence sequence) {
        return markedSequences.contains(sequence);
    }

    // AsyncTask. Used to fetch data from the database in another thread which is NOT the GUI thread
    public class AsyncFetchDatabase extends AsyncTask<Void, Void, List<Sequence>> {

        @Override
        protected List<Sequence> doInBackground(Void... params) {
            return DBController.getInstance().loadCurrentProfileSequencesAndFrames(selectedChild.getId(), dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, getApplicationContext());
        }

        @Override
        protected void onPostExecute(final List<Sequence> result) {
            sequenceAdapter = new SequenceListAdapter(MainActivity.this, result, MainActivity.this);
            sequenceGrid.setAdapter(sequenceAdapter);
        }
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        // Create the AsyncTask thread used to fetch database content
        AsyncFetchDatabase fetchDatabase = new AsyncFetchDatabase();

        // Removes highlighting from Sequences that might have been lifted up when selected before entering the sequence
        for (int i = 0; i < sequenceGrid.getChildCount(); i++) {
            View view = sequenceGrid.getChildAt(i);

            ((PictogramView) view).placeDown();
        }
        //If a Child is selected at this point, update Sequences for the Child
        //Profile child = LifeStory.getInstance().getChild();
        if(isChildSet) {
            /*sequenceAdapter = initAdapter();
            sequenceGrid.setAdapter(sequenceAdapter);
            */
            fetchDatabase.execute();
            //loadSeqGrid();
        }

        isInEditMode = false;
        editButton.setPressed(false);

        /*if(sequenceAdapter != null) {
            sequenceAdapter.setEditModeEnabled(isInEditMode);
            sequenceAdapter.notifyDataSetChanged();
        }*/
    }

    @Override
    public void onBackPressed() {
        if (markingMode) {
            markedSequences.clear();
            sequenceAdapter.notifyDataSetChanged();

            deleteButton.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            markingMode = false;
        } else {
            super.onBackPressed();
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
