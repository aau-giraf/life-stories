package dk.aau.cs.giraf.tortoise.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.tortoise.R;

import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class ScheduleViewActivity extends ScheduleActivity
{

    int weekDaySelected;

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
        setContentView(R.layout.schedule_edit_activity);

        // disable non-programmatic scrolling
        //disableScrolling();

        // display the sequences in the week schedule
        displaySequences();

        // add arrows to week days with more than four pictograms
        // addArrows();

        // Set title, remove buttons that should not be there. Set orientation to landscape
        setUpViewMode();
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

        //Puts the weekDaySelected variable in the intent, to pass it to the next Activity
        i.putExtra("weekDaySelected", weekDaySelected);
        i.putExtra("pictogramSize", 1);

        //Starts the Portrait mode activity
        startActivity(i);
    }

    /**
     * Adds title name and removes the part of the layout that should not be visible in view mode.
     */
    private void setUpViewMode() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        View saveButton = findViewById(R.id.save);
        saveButton.setVisibility(View.INVISIBLE);

        GButton scheduleImageButton = (GButton)findViewById(R.id.schedule_image_button);
        scheduleImageButton.setClickable(false);

        EditText title = (EditText) findViewById(R.id.scheduleName);
        title.setText(LifeStory.getInstance().getCurrentStory().getTitle());
        title.setEnabled(false);
    }

    public int[] getBorderIds()
    {
        int ids[] =
        {
            R.id.border_monday,
            R.id.border_tuesday,
            R.id.border_wednesday,
            R.id.border_thursday,
            R.id.border_friday,
            R.id.border_saturday,
            R.id.border_sunday,
        };

        return ids;
    }

    private void displaySequences()
    {
        // load sequences associated with citizen
        DBController.getInstance().loadCurrentProfileSequences(LifeStory.getInstance().getChild().getId(), dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, this);

        // get sequences from database
        List<Sequence> storyList = LifeStory.getInstance().getStories();
        GButton scheduleImage = (GButton) findViewById(R.id.schedule_image_button);


        Intent i = getIntent();
        int storyIndex = i.getIntExtra("story", -1);
         // -1 indicates an error
        if(storyIndex != -1)
        {
            // get parent sequence and set image of week schedule in layout accordingly
            Sequence seq = storyList.get(storyIndex);

            Drawable scheduleImageDrawable = new BitmapDrawable(getResources(), seq.getTitleImage());
            scheduleImage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, scheduleImageDrawable);
            LifeStory.getInstance().setCurrentStory(seq);

            // show sequences
            weekdaySequences = new ArrayList<Sequence>();

            // add empty sequences for each week day
            for (int n = 0; n < 7; n++) {
                weekdaySequences.add(n, DBController.getInstance().getSequenceFromID(seq.getMediaFrames().get(n).getNestedSequenceID(), this));
            }

            renderSchedule(false);


           /* int layoutArray[] = new int[7];

            layoutArray[0] = R.id.layoutMonday;
            layoutArray[1] = R.id.layoutTuesday;
            layoutArray[2] = R.id.layoutWednesday;
            layoutArray[3] = R.id.layoutThursday;
            layoutArray[4] = R.id.layoutFriday;
            layoutArray[5] = R.id.layoutSaturday;
            layoutArray[6] = R.id.layoutSunday;


            int ii = 0;
            for(MediaFrame mf : seq.getMediaFrames())
            {
                for(MediaFrame activityFrame : DBController.getInstance().getSequenceFromID(mf.getNestedSequenceID(), getApplicationContext()).getMediaFrames())
                {
                    LinearLayout l = (LinearLayout) findViewById(layoutArray[ii]);
                    addItems(activityFrame, l);
                }

                ii++;
            }*/
        }
        else
        {
            GuiHelper.ShowToast(this, "Kunne ikke indlæse sekvenser.");
        }

        markCurrentWeekday();
    }

    @Override
    public void dismissAddContentDialog(View v)
    {
        multichoiceDialog.dismiss();
        renderSchedule(false);
    }
    public void showExitDialog(View v){
        finish();
    }

}
