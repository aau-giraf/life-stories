package dk.aau.cs.giraf.tortoise.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.impl.cookie.NetscapeDraftSpec;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import dk.aau.cs.giraf.gui.GTextView;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.LayoutTools;
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
        setContentView(R.layout.schedule_view_activity);

        // display the sequences in the week schedule
        displaySequences();

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }

        //Set schedule name
        TextView title = (TextView) findViewById(R.id.scheduleName);
        title.setText(LifeStory.getInstance().getCurrentStory().getTitle());
    }

    private void displaySequences()
    {
        // load sequences associated with citizen
        DBController.getInstance().loadCurrentCitizenSequences(LifeStory.getInstance().getChild().getId(), dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE, this);

        // get sequences from database
        List<Sequence> storyList = LifeStory.getInstance().getStories();
        ImageView scheduleImage = (ImageView) findViewById(R.id.schedule_image_view);


        Intent i = getIntent();
        int storyIndex = i.getIntExtra("story", -1);
         // -1 indicates an error
        if(storyIndex != -1)
        {
            // get parent sequence and set image of week schedule in layout accordingly
            Sequence seq = storyList.get(storyIndex);
            scheduleImage.setImageBitmap(seq.getTitleImage());
            LifeStory.getInstance().setCurrentStory(seq);
            // show sequences
            int layoutArray[] = new int[7];

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
            }

        }
        else
        {
            GuiHelper.ShowToast(this, "Kunne ikke indlæse sekvenser.");
        }

        markCurrentWeekday();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);

        if (resultCode == RESULT_OK && requestCode == 1) {
            int[] checkoutIds = i.getExtras().getIntArray("checkoutIds");

            if (checkoutIds.length == 0)
            {
                GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
            }
        }
        else if (resultCode == RESULT_OK && requestCode == 2) {
            try{
                int[] checkoutIds = i.getExtras().getIntArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                }
                else
                {
                    try
                    {
                        // TODO: should be handled: LifeStory.getInstance().getCurrentStory().setTitlePictoId(checkoutIds[0]);
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        Bitmap bitmap = picto.getImageData();
                        bitmap = LayoutTools.getSquareBitmap(bitmap);
                        bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 20);
                        // TODO: should be handled: LifeStory.getInstance().getCurrentStory().setTitleImage(bitmap);
                        ImageButton scheduleImage = (ImageButton) findViewById(R.id.schedule_image_button);
                        scheduleImage.setImageBitmap(bitmap);
                    }
                    //We expect a null pointer exception if the pictogram is without image
                    //TODO: Investigate if this still happens with the new DB.
                    // It still does
                    catch (NullPointerException e)
                    {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (Exception e)
            {
                GuiHelper.ShowToast(this, e.toString());
            }
        }
    }
}
