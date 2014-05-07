package dk.aau.cs.giraf.tortoise.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.List;

import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleEditActivity extends ScheduleActivity
{
    public List<List<ImageView>> weekdayLists;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // check whether tablet is in portrait or landscape mode and set the layout accordingly
        // landscape mode shows mode days than portrait mode
        int screenOrientation = getResources().getConfiguration().orientation;
        if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            isInLandscape = true;
            setContentView(R.layout.schedule_edit_activity);
        }
        else
        {
            isInLandscape = false;
            setContentView(R.layout.schedule_edit_activity_portrait);
        }

        showAddButtons();

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }
    }

    @Override
    public void onResume()
    {
        // this method is also called after oncreate()
        // makes sure that current weekday is also marked after resume of the app
        super.onResume();

        // mark the current weekday in the scheduler
        markCurrentWeekday();
    }

    public void weekdaySelected(View v)
    {
        markCurrentWeekday();
    }

    public void showAddButtons()
    {
        LinearLayout level1 = (LinearLayout) findViewById(R.id.completeWeekLayout);

        int childcount = level1.getChildCount();

        // find each of the individual week days
        for (int i = 0; i < childcount; i++)
        {
            try
            {
                // TODO: fix hardcoding of 1
                RelativeLayout v = (RelativeLayout) level1.getChildAt(i); // the +1 is to choose the element at depth 2
                ScrollView level2 = (ScrollView) v.getChildAt(1);
                LinearLayout level3 = (LinearLayout) level2.getChildAt(0);
                level3.addView(addButton());

            }catch (Exception ex)
            {
                GuiHelper.ShowToast(this, "Der skete en fejl");
            }

        }
    }

    public boolean isInLandscape;

    public void addItems(Bitmap bm, LinearLayout layout)
    {
        try
        {
            LifeStory.getInstance().setCurrentStory(new Sequence());

            ImageView iw = new ImageView(this);
            iw.setBackgroundResource(R.drawable.week_schedule_bg_tile);

            int xy;

            // use wider buttons when in portrait mode
            if(isInLandscape)
            {
                // small buttons
                xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
            }else
            {
                // big buttons
                xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_portrait);
            }

            iw.setImageBitmap(resizeBitmap(bm, xy, xy)); // the same value is used for height and width because the pictogram should be square

            // set padding of each imageview containing
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 10, 0, 0); // pad pictogram at top to space them out
            iw.setLayoutParams(lp);

            final LinearLayout workaroundLayout = layout;

            // remove pictogram in the linear view contained in the scroll view
            iw.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    workaroundLayout.removeView(v);
                    return true;
                }
            });

            // add pictogram to week day and make sure the add button is always at the bottom of the week day
            layout.removeViewAt(layout.getChildCount() - 1); // remove add button
            layout.addView(iw); // add new pictogram
            layout.addView(addButton()); // add the add button again

        }
        catch (Exception ex)
        {
            GuiHelper.ShowToast(this, ex.toString());
        }

    }

    // this method returns an imageview containing the add pictogram button with a plus on it
    public ImageView addButton()
    {
        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 0); // only pad top of pictogram to create space between them
        iv.setLayoutParams(lp);
        iv.setBackgroundResource(R.layout.border_selected);

        // use wider buttons when in portrait mode
        int xy;

        if(isInLandscape)
        {
            // small buttons
            xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
        }else
        {
            // big buttons
            xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_portrait);
        }

        Drawable resizedDrawable = resizeDrawable(R.drawable.add, xy, xy);
        iv.setImageDrawable(resizedDrawable);

        // set listener on the add button so it starts pictosearch when clicked
        iv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startPictosearchForScheduler(view);
            }
        });

        // return the imageview with the plus image on it
        return iv;
    }

    // this is just a variable for a workaround
    public static LinearLayout weekdayLayout;

    // this method handles pictograms sent back via an intent from pictosearch
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 2)
        {
            // i think this is triggered when a profile image is chosen from pictosearch
            try{
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                }
                else
                {
                    try{
                        LifeStory.getInstance().setCurrentStory(new Sequence());
                        LifeStory.getInstance().getCurrentStory().setTitlePictoId(checkoutIds[0]);
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
                        bitmap = LayoutTools.getSquareBitmap(bitmap);
                        bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 20);
                        LifeStory.getInstance().getCurrentStory().setTitleImage(bitmap);
                        ImageView storyImage = (ImageView) findViewById(R.id.schedule_image_button);
                        storyImage.setImageBitmap(bitmap);
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
        else if (resultCode == RESULT_OK && requestCode == 3)
        {
            // this code is executed when the week scheduler requests an image from pictosearch
            try
            {
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
                if (checkoutIds.length == 0)
                {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                }
                else
                {
                    try
                    {
                        LifeStory.getInstance().setCurrentStory(new Sequence());
                        LifeStory.getInstance().getCurrentStory().setTitlePictoId(checkoutIds[0]);
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
                        bitmap = LayoutTools.getSquareBitmap(bitmap);
                        bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 20);

                        // add item to scroll view
                        addItems(bitmap, weekdayLayout);
                    }
                    catch (NullPointerException e)
                    {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (NullPointerException e)
            {
                GuiHelper.ShowToast(this, "Fejl");
            }
        }

    }
    public boolean saveSchedule(View v){
        Sequence story = LifeStory.getInstance().getCurrentStory();
        if (story == null){ return false; }
        story.getTitleImage();
        return true;
    }



}