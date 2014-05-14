package dk.aau.cs.giraf.tortoise.activities;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

import dk.aau.cs.giraf.tortoise.R;

import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import android.view.ViewGroup.LayoutParams;

public class ScheduleViewActivity extends ScheduleActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_view_activity);

        int[] borderIds = getBorderIds();

        for(int i = 0; i < 7; i++)
        {
            addTopArrow(borderIds[i]);
        }

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

    public int[] getBorderIds()
    {
        int ids[] =
        {
            R.id.borderMonday,
            R.id.borderTuesday,
            R.id.borderWednesday,
            R.id.borderThursday,
            R.id.borderFriday,
            R.id.borderSaturday,
            R.id.borderSunday,
        };

        return ids;
    }

    public void addTopArrow(int layoutId)
    {
        LinearLayout l = (LinearLayout) findViewById(layoutId);

        LinearLayout arrow = new LinearLayout(this);
        LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        linLayoutParam.height = 60;
        linLayoutParam.width = LayoutParams.MATCH_PARENT;
        arrow.setLayoutParams(linLayoutParam);

        arrow.setBackgroundResource(R.drawable.scroll_up);
        l.addView(arrow);

        arrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try{
                LinearLayout arrowViewParent;
                    arrowViewParent = (LinearLayout) view.getParent();

                RelativeLayout scrollViewParent = (RelativeLayout) arrowViewParent.getParent();

                ScrollView arrowScrollView = (ScrollView) (scrollViewParent != null ? scrollViewParent.getChildAt(1) : null); // TODO: fix this hardcoding

                if (arrowScrollView != null) {
                    arrowScrollView.smoothScrollBy(0, -30);
                }
                } catch (Exception ex)
                {
                    GuiHelper.ShowToast(getApplicationContext(), ex.toString());
                }
            }
        });
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
}
