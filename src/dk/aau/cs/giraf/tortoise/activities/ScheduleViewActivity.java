package dk.aau.cs.giraf.tortoise.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.R;

import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

public class ScheduleViewActivity extends ScheduleActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_edit_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setUpViewMode();
        // Get intent, action and MIME type

        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
            return;
        }

        // display the sequences in the week schedule
        displaySequences();

        //Set schedule name
        TextView title = (TextView) findViewById(R.id.scheduleName);
        title.setText(LifeStory.getInstance().getCurrentStory().getTitle());
    }

    /**
     * Removes the part of the layout that should not be visible in view mode.
     */
    private void setUpViewMode() {
        View saveButton = findViewById(R.id.save);
        saveButton.setVisibility(View.INVISIBLE);

        GButton scheduleImageButton = (GButton)findViewById(R.id.schedule_image_button);
        scheduleImageButton.setClickable(false);
    }

    private void displaySequences()
    {
        // load sequences associated with citizen
        DBController.getInstance().loadCurrentCitizenSequences(LifeStory.getInstance().getChild().getId(), dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, this);

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
}
