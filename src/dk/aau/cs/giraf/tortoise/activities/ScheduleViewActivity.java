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
import android.view.ViewGroup.LayoutParams;

public class ScheduleViewActivity extends ScheduleActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_edit_activity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setUpViewMode();

        
        // display the sequences in the week schedule
        displaySequences();

        int[] borderIds = getBorderIds();

        for(int i = 0; i < 7; i++)
        {
            // do not show arrows unless there are too many elements in the view
            if(shouldShowArrow(borderIds[i]))
            {
                addUpArrow(borderIds[i]);
                addDownArrow(borderIds[i]);
            }
        }

        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
            return;
        }

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

    public void addUpArrow(int layoutId)
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
                try
                {
                LinearLayout arrowViewParent;
                    arrowViewParent = (LinearLayout) view.getParent();

                RelativeLayout scrollViewParent = (RelativeLayout) arrowViewParent.getParent();

                ScrollView arrowScrollView = (ScrollView) (scrollViewParent != null ? scrollViewParent.getChildAt(1) : null); // TODO: fix this hardcoding

                if (arrowScrollView != null)
                {
                    arrowScrollView.smoothScrollBy(0, -30);
                }
                }
                catch (Exception ex)
                {
                    GuiHelper.ShowToast(getApplicationContext(), ex.toString());
                }
            }
        });
    }

    public void addDownArrow(int layoutId)
    {
        LinearLayout l = (LinearLayout) findViewById(layoutId);

        LinearLayout arrow = new LinearLayout(this);
        LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        linLayoutParam.height = 60;
        linLayoutParam.setMargins(0, 440, 0, 0);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);


        linLayoutParam.width = LayoutParams.MATCH_PARENT;
        arrow.setLayoutParams(linLayoutParam);

        arrow.setBackgroundResource(R.drawable.scroll_down);
        l.addView(arrow);

        arrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try{
                    LinearLayout arrowViewParent;
                    arrowViewParent = (LinearLayout) view.getParent();

                    RelativeLayout scrollViewParent = null;
                    if (arrowViewParent != null) {
                        scrollViewParent = (RelativeLayout) arrowViewParent.getParent();
                    }

                    ScrollView arrowScrollView = (ScrollView) (scrollViewParent != null ? scrollViewParent.getChildAt(1) : null); // TODO: fix this hardcoding

                    if (arrowScrollView != null) {
                        arrowScrollView.smoothScrollBy(0, 30);
                    }
                } catch (Exception ex)
                {
                    GuiHelper.ShowToast(getApplicationContext(), ex.toString());
                }
            }
        });
    }

    public Boolean shouldShowArrow(int borderID)
    {
        // method to determine whether arrows should be shown
        LinearLayout borderLayout = (LinearLayout) findViewById(borderID);
        try
        {
            RelativeLayout parentLayout = (RelativeLayout) borderLayout.getParent();

            if (parentLayout != null) {
                ScrollView sv = (ScrollView) parentLayout.getChildAt(1);

                if (sv != null)
                {
                    // this is the view in the scroll view which contains pictograms in each week day
                    LinearLayout scrollViewChild = (LinearLayout) sv.getChildAt(0); // TODO: fix hard coding of 0

                    // if more than 4 pictograms in scroll view, show arrows
                    if(scrollViewChild.getChildCount() >= 4)
                    {
                        return true;
                    }else{
                        return false;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            GuiHelper.ShowToast(this, ex.toString());
        }

        return false;
    }

    // TODO: not currently used. Not sure if working. Not tested!
    int getScrollViewPosition(ScrollView scrollView)
    {
        // 1 for top, 0 for between top and bottom and -1 for bottom
        LinearLayout linearLayout = (LinearLayout) scrollView.getChildAt(0);
        if (linearLayout != null)
        {
            if(linearLayout.getMeasuredHeight() <= (scrollView.getScrollY() +
                    scrollView.getHeight()))
            {
                return -1;
            }
            else {
                return 0;
            }
        }
>>>>>>> refs/remotes/origin/dev
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
