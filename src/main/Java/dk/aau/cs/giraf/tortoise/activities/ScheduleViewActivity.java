package dk.aau.cs.giraf.tortoise.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.tortoise.ProgressTracker;
import dk.aau.cs.giraf.tortoise.R;

import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

import android.widget.Spinner;
import android.widget.TextView;

public class ScheduleViewActivity extends ScheduleActivity
{

    private final String DELETE_SEQUENCES_TAG = "DELETE_SEQUENCES_TAG";
    private final int PICTO_EDIT_PICTOGRAM_CALL = 4;
    private final String PICTO_ADMIN_PACKAGE = "dk.aau.cs.giraf.pictosearch";
    private final String PICTO_ADMIN_CLASS = PICTO_ADMIN_PACKAGE + "." + "PictoAdminMain";
    int weekDaySelected;
    private int childId;
    private int guardianId;
    int amountOfPictograms;
    private GirafButton scheduleImage;
    private GirafButton resetProgress;
    private GirafButton portraitButton;
    Helper helper;
    int template;
    private Sequence seq;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
            return;
        }

        // the view activity uses a modified version of the edit activity layout
        setContentView(R.layout.schedule_view_activity);

        helper = new Helper(this);

        initializeButtons();

        childId = intent.getIntExtra("currentChildID", -1);

        guardianId = intent.getIntExtra("currentGuardianID", -1);

        template = intent.getIntExtra("story", -1);

        displaySequences();


        /*if(loadProgress(new File(getApplicationContext().getFilesDir(), "progress"+String.valueOf(seq.getId())+".bin")) != null) {
            resumeSession(loadProgress(new File(getApplicationContext().getFilesDir(), "progress"+String.valueOf(seq.getId())+".bin")));
        }*/


        // disable non-programmatic scrolling
        //disableScrolling();

        // display the sequences in the week schedule

    }

    @Override
    public void onResume() {
        super.onResume();

        ProgressTracker temp = loadProgress(new File(getApplicationContext().getFilesDir(),
                "progress"+String.valueOf(seq.getId())+".bin"));

        if(temp != null) {
            resumeSession(temp);
        }
    }

    private void resumeSession(ProgressTracker temp){
        progress = temp;

        if(progress.getChangedDays(getApplicationContext()) != null && progress.getChangedDays(getApplicationContext()) != weekdaySequences){
            weekdaySequences = progress.getChangedDays(getApplicationContext());
            renderSchedule(false);
        }

        if(progress.getProgress() != null && progress.getProgress() != progressActivity) {
            progressActivity = progress.getProgress();
            resumeProgress(progressActivity[0]);
        }
        else {
            progressActivity = new int[2];
        }

        if(progress.getMarkedActivities() != null && progress.getMarkedActivities() != markedActivities) {
            markedActivities = progress.getMarkedActivities();
            setMarks();
        }
    }

    private ProgressTracker loadProgress(File f) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            return (ProgressTracker) ois.readObject();
        } catch (Exception e) {
        }
        return null;
    }

    private void initializeButtons() {
        scheduleImage = (GirafButton) findViewById(R.id.schedule_image);
        scheduleImage.setEnabled(false);
        resetProgress = new GirafButton(this, getResources().getDrawable(R.drawable.icon_rotate));
        resetProgress.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ProgressResetting();
            }
        });
        portraitButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_change_land_to_port));
        portraitButton.setOnClickListener(new View.OnClickListener() {
                                              //Open Child Selector when pressing the Child Select Button
                                              @Override
                                              public void onClick(View v) {
                                                  startPortraitMode(v);
                                              }
                                          });
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinnercontent,
                R.layout.giraf_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        amountOfPictograms = 2;
                        break;
                    case 1:
                        amountOfPictograms = 0;
                        break;
                    case 2:
                        amountOfPictograms = 1;
                        break;
                    case 3:
                        amountOfPictograms = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                amountOfPictograms = 2;
            }
        });

        addGirafButtonToActionBar(resetProgress, RIGHT);
        addGirafButtonToActionBar(portraitButton, RIGHT);
    }

    public void ProgressResetting(){

        progress = new ProgressTracker();
        progressActivity = new int[2];
        markedActivities = null;
        currentActivity = new int[] {-1,-1,-1,-1,-1,-1,-1};
        try {
            File file = new File(getApplicationContext().getFilesDir(), "progress" + String.valueOf(seq.getId()) + ".bin");
            boolean deleted = file.delete();
        }
        catch(NullPointerException e){
            GuiHelper.ShowToast(this, "New progress to delete");
        }
        displaySequences();

    }

    public void jaClick(View v) {
        // Button to accept delete of sequences
        replaceDialog.dismiss();
        callPictoAdmin(v, PICTO_EDIT_PICTOGRAM_CALL);

    }

    public void nejClick(View v) {
        // Button to cancel delete of sequences
        replaceDialog.dismiss();
    }

    private void callPictoAdmin(View v, int modeId) {
        Intent i = new Intent();
        i.setComponent(new ComponentName(PICTO_ADMIN_PACKAGE, PICTO_ADMIN_CLASS));
        i.putExtra("currentChildID", childId);
        i.putExtra("currentGuardianID", guardianId);
        i.putExtra("purpose", "single");

        ScheduleViewActivity.this.startActivityForResult(i, modeId);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 4) {

            OnEditPictogramResult(data);
        }
    }

    private void OnEditPictogramResult(Intent data) {
        if (pictogramEditPos < 0)
            return;

        int[] checkoutIds = data.getExtras().getIntArray(
                PICTO_INTENT_CHECKOUT_ID);

        if (checkoutIds.length == 0)
            return;

        MediaFrame frame = weekdaySequences.get(weekdaySelected).getMediaFrames().get(pictogramEditPos);

        frame.setPictogramId(checkoutIds[0]);

        markedActivities.get(weekdaySelected)[pictogramEditPos] = true;
        progress.setMarkedActivities(markedActivities);
    }

    public void weekdaySelected(View view) {
        TextView dayText;
        resetTextView();

        //Finds what day was selected and puts it in the weekDaySelected variable
        switch(view.getId()) {
            case R.id.monday: //If the day selected is Monday
                weekDaySelected = 0;
                dayText = (TextView) findViewById(R.id.mondayStoryName);
                dayText.setAllCaps(true);
                break;
            case R.id.tuesday: //If the day selected is Tuesday
                weekDaySelected = 1;
                dayText = (TextView) findViewById(R.id.tuesdayStoryName);
                dayText.setAllCaps(true);
                break;
            case R.id.wednesday: //If the day selected is Wednesday
                weekDaySelected = 2;
                dayText = (TextView) findViewById(R.id.wednesdayStoryName);
                dayText.setAllCaps(true);
                break;
            case R.id.thursday: //If the day selected is Thursday
                weekDaySelected = 3;
                dayText = (TextView) findViewById(R.id.thursdayStoryName);
                dayText.setAllCaps(true);
                break;
            case R.id.friday: //If the day selected is Friday
                weekDaySelected = 4;
                dayText = (TextView) findViewById(R.id.fridayStoryName);
                dayText.setAllCaps(true);
                break;
            case R.id.saturday: //If the day selected is Saturday
                weekDaySelected = 5;
                dayText = (TextView) findViewById(R.id.saturdayStoryName);
                dayText.setAllCaps(true);
                break;
            case R.id.sunday: //If the day selected is Sunday
                weekDaySelected = 6;
                dayText = (TextView) findViewById(R.id.sundayStoryName);
                dayText.setAllCaps(true);
                break;
            default:
                weekDaySelected = 0; //If for some reason there is no day selected it defaults to Monday
        }
    }

    protected void resetTextView() {
        TextView text;
        for (int i = 0; i < 7; i++) {
            switch (i) {
                case 0:
                    text = (TextView) findViewById(R.id.mondayStoryName);
                    text.setAllCaps(false);
                case 1:
                    text = (TextView) findViewById(R.id.tuesdayStoryName);
                    text.setAllCaps(false);
                case 2:
                    text = (TextView) findViewById(R.id.wednesdayStoryName);
                    text.setAllCaps(false);
                case 3:
                    text = (TextView) findViewById(R.id.thursdayStoryName);
                    text.setAllCaps(false);
                case 4:
                    text = (TextView) findViewById(R.id.fridayStoryName);
                    text.setAllCaps(false);
                case 5:
                    text = (TextView) findViewById(R.id.saturdayStoryName);
                    text.setAllCaps(false);
                case 6:
                    text = (TextView) findViewById(R.id.sundayStoryName);
                    text.setAllCaps(false);

            }
        }
    }

    public void startPortraitMode (View view) {
        Intent i = new Intent(getApplicationContext(), ScheduleViewPortraitActivity.class);
        EditText scheduleName = (EditText) findViewById(R.id.editText);

        //Puts the weekDaySelected variable in the intent, to pass it to the next Activity
        i.putExtra("weekDaySelected", weekDaySelected);
        i.putExtra("amountOfPictograms", amountOfPictograms);
        i.putExtra("scheduleName", scheduleName.getText().toString());

        //Starts the Portrait mode activity
        startActivity(i);
    }

    private void displaySequences()
    {
        // load sequences associated with citizen
        DBController.getInstance().loadCurrentProfileSequences(childId, dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, this);

        // get sequences from database
        List<Sequence> storyList = DBController.getInstance().loadCurrentProfileSequencesAndFrames(childId, dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, getApplicationContext());

        int storyIndex = template;

         // -1 indicates an error
        if(storyIndex != -1)
        {
            // get parent sequence and set image of week schedule in layout accordingly
            seq = storyList.get(storyIndex);

            Drawable scheduleImageDrawable = new BitmapDrawable(getResources(), seq.getTitleImage());
            scheduleImage.setIcon(scheduleImageDrawable);
            EditText scheduleName = (EditText) findViewById(R.id.editText);
            scheduleName.setText(seq.getTitle());
            scheduleName.setEnabled(false);

            // show sequences
            weekdaySequences = new ArrayList<Sequence>();

            // add empty sequences for each week day
            for (int n = 0; n < 7; n++) {
                weekdaySequences.add(n, DBController.getInstance().getSequenceFromID(seq.getMediaFrames().get(n).getNestedSequenceID(), this));
            }

            renderSchedule(false);
        }
        else
        {
            GuiHelper.ShowToast(this, "Kunne ikke indlæse sekvenser.");
        }

        markCurrentWeekday();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            File f = new File(getApplicationContext().getFilesDir() ,"progress"+String.valueOf(seq.getId())+".bin");
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(progress);
            oos.flush();
            oos.close();
        } catch (Exception e){
        }
    }

    public void showExitDialog(View v){
        finish();
    }

}
