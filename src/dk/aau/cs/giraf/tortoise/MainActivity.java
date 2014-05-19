package dk.aau.cs.giraf.tortoise;


import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import dk.aau.cs.giraf.gui.GButtonProfileSelect;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GProfileSelector;
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
                h.profilesHelper.getProfileById(i.getIntExtra("currentGuardianID", -1)));

        overrideViews();
        if (i.getIntExtra("currentChildID", -1) == -1) {
            findViewById(R.id.profileSelect).performClick();
        }else {
            LifeStory.getInstance().setChild(
                    h.profilesHelper.getProfileById(i.getIntExtra("currentChildID", -1)));
            // Initialize name of profile
            TextView profileName = (TextView) findViewById(R.id.child_name);
            profileName.setText(LifeStory.getInstance().getChild().getName());
            HideButtons();
        }
    }

    private void HideButtons() {
        findViewById(R.id.profileSelect).setVisibility(View.INVISIBLE);
        findViewById(R.id.add_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.edit_mode_toggle).setVisibility(View.INVISIBLE);

    }

    private void overrideViews() {
        GButtonProfileSelect gbps = (GButtonProfileSelect) findViewById(R.id.profileSelect);
        gbps.setOnClickListener(new View.OnClickListener() {
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

                        TextView profileName = (TextView) findViewById(R.id.child_name);
                        profileName.setText(LifeStory.getInstance().getChild().getName());

                        loadSeqGrid(LifeStory.getInstance().getChild());

                        childSelector.dismiss();
                    }
                });
                try{childSelector.backgroundCancelsDialog(false);}
                catch (Exception ignored){}
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
                        i.putExtra("template", arg2);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_tortoise_startup_screen, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        GToggleButton editMode = (GToggleButton) findViewById(R.id.edit_mode_toggle);
        TextView profileName = (TextView)findViewById(R.id.child_name);

        isInEditMode = false;
        editMode.setToggled(false);
        Profile child = LifeStory.getInstance().getChild();
        if(child != null) {
            profileName.setText(child.getName());
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
