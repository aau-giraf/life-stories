package dk.aau.cs.giraf.tortoise;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import dk.aau.cs.giraf.gui.GButtonProfileSelect;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GToast;
import dk.aau.cs.giraf.gui.GToggleButton;
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
import dk.aau.cs.giraf.tortoise.activities.ViewModeActivity;

public class MainActivity extends TortoiseActivity {

    private final int DIALOG_DELETE = 1;
    private boolean isInEditMode = false;
    private boolean isInScheduleMode = false;
    private boolean canFinish;
    private SequenceListAdapter sequenceAdapter;

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
            GuiHelper.ShowToast(this, "Tortoise skal startes fra GIRAF");

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
                h.profilesHelper.getProfileById(i.getIntExtra("currentGuardianID", -1))); //TODO -1 should be used
        LifeStory.getInstance().setChild(
                h.profilesHelper.getProfileById(11));//i.getIntExtra("currentChildID", 11)));

        overrideViews();

        // Initialize name of profile
        TextView profileName = (TextView) findViewById(R.id.child_name);
        profileName.setText(LifeStory.getInstance().getChild().getName());

    }

    private void overrideViews() {

        // Setup Profile Selector button
        GButtonProfileSelect gbps = (GButtonProfileSelect) findViewById(R.id.profileSelect);
        //Call the method setup with a Profile guardian, no currentProfile
        // (which means that the guardian is the current Profile) and the onCloseListener
        gbps.setup(LifeStory.getInstance().getGuardian(),
                null,
                new GButtonProfileSelect.onCloseListener() {
                    @Override
                    public void onClose(Profile guardianProfile, Profile currentProfile) {
                        //If the guardian is the selected profile create GToast displaying the name
                        if(currentProfile == null){
                            GToast w = new GToast(getApplicationContext(),
                                    "The Guardian " + guardianProfile.getName() + "is Selected", 2);
                            w.show();
                        }
                        //If another current Profile is the selected profile create GToast displaying the name
                        else{
                            GToast w = new GToast(getApplicationContext(),
                                    "The current profile " + currentProfile.getName() + "is Selected", 2);
                            w.show();
                        }
                    }
                });


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
                        i.putExtra("template", arg2);
                    } else {
                        i = new Intent(getApplicationContext(), ScheduleViewActivity.class);
                        i.putExtra("story", arg2);
                    }
                } else {
                    if (isInEditMode) {
                        i = new Intent(getApplicationContext(), EditModeActivity.class);
                        i.putExtra("template", arg2);
                    } else {
                        i = new Intent(getApplicationContext(), ViewModeActivity.class); //TODO should be common seq viewer
                        i.putExtra("story", arg2);
                    }
                }
                startActivity(i);
            }
        });

        // Edit mode switcher button
        GToggleButton button = (GToggleButton) findViewById(R.id.edit_mode_toggle);

        button.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                GToggleButton button = (GToggleButton) v;
                isInEditMode = button.isToggled();
                GridView sequenceGrid = (GridView) findViewById(R.id.sequence_grid);

                // Make sure that all views currently not visible will have the correct
                // editmode when they become visible
                sequenceAdapter.setEditModeEnabled(isInEditMode);

                //createButton.setVisibility(isInEditMode ? View.VISIBLE : View.GONE);

                // Update the editmode of all visible views in the grid
                for (int i = 0; i < sequenceGrid.getChildCount(); i++) {
                    View view = sequenceGrid.getChildAt(i);

                    if (view instanceof PictogramView) {
                        ((PictogramView) view).setEditModeEnabled(isInEditMode);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_tortoise_startup_screen, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // Clear existing life stories
        LifeStory.getInstance().getStories().clear();
        LifeStory.getInstance().getTemplates().clear();
        if (isInScheduleMode)
            DBController.getInstance().loadCurrentProfileSequences(
                    LifeStory.getInstance().getChild().getId(), Sequence.SequenceType.SCHEDULE, this);
        else
            DBController.getInstance().loadCurrentProfileSequences(
                    LifeStory.getInstance().getChild().getId(), Sequence.SequenceType.STORY, this);
        //DBController.getInstance().loadCurrentGuardianTemplates(LifeStory.getInstance().getGuardian().getId(), Sequence.SequenceType.SCHEDULE, this);
        //DBController.getInstance().loadCurrentGuardianTemplates(
        //        LifeStory.getInstance().getChild().getId(), Sequence.SequenceType.SCHEDULEDDAY, this);


        ToggleButton templateMode = (ToggleButton)findViewById(R.id.template_mode_toggle);
        GToggleButton editMode = (GToggleButton) findViewById(R.id.edit_mode_toggle);
        TextView profileName = (TextView)findViewById(R.id.child_name);

        isInEditMode = false;
        templateMode.setChecked(false);
        editMode.setToggled(false);
        Profile c = LifeStory.getInstance().getChild();
        profileName.setText(c.getName());
        sequenceAdapter.setEditModeEnabled(isInEditMode);
        sequenceAdapter.notifyDataSetChanged();
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

    public void addSchedule(View v)
    {
        canFinish = false;
        Intent i = new Intent(this, ScheduleEditActivity.class);
        i.putExtra("template", -1);

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
