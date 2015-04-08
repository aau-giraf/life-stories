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

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GirafButton;
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
    private GirafButton scheduleImage;
    private GirafButton portraitButton;

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

        initializeButtons();


        // disable non-programmatic scrolling
        //disableScrolling();

        // display the sequences in the week schedule
        displaySequences();
    }

    private void initializeButtons() {
        scheduleImage = new GirafButton(this, getResources().getDrawable(R.drawable.no_image_big));
        scheduleImage.setEnabled(false);
        portraitButton = new GirafButton(this, getResources().getDrawable(R.drawable.placeholder));
        portraitButton.setOnClickListener(new View.OnClickListener() {
                                              //Open Child Selector when pressing the Child Select Button
                                              @Override
                                              public void onClick(View v) {
                                                  startPortraitMode(v);
                                              }
                                          });

        addGirafButtonToActionBar(scheduleImage, LEFT);
        addGirafButtonToActionBar(portraitButton, RIGHT);
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
        i.putExtra("amountOfPictograms", 0);
        i.putExtra("scheduleName", scheduleName.getText().toString());

        //Starts the Portrait mode activity
        startActivity(i);
    }

    private void displaySequences()
    {
        // load sequences associated with citizen
        DBController.getInstance().loadCurrentProfileSequences(LifeStory.getInstance().getChild().getId(), dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, this);

        // get sequences from database
        List<Sequence> storyList = LifeStory.getInstance().getStories();


        Intent i = getIntent();
        int storyIndex = i.getIntExtra("story", -1);
         // -1 indicates an error
        if(storyIndex != -1)
        {
            // get parent sequence and set image of week schedule in layout accordingly
            Sequence seq = storyList.get(storyIndex);

            Drawable scheduleImageDrawable = new BitmapDrawable(getResources(), seq.getTitleImage());
            scheduleImage.setIcon(scheduleImageDrawable);
            LifeStory.getInstance().setCurrentStory(seq);
            EditText scheduleName = (EditText) findViewById(R.id.editText);
            scheduleName.setText(seq.getTitle());

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
    public void dismissAddContentDialog(View v)
    {
        multichoiceDialog.dismiss();
        renderSchedule(false);
    }
    public void showExitDialog(View v){
        finish();
    }

}
