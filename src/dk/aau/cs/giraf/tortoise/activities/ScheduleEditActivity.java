package dk.aau.cs.giraf.tortoise.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.EditChoiceFrameView;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleEditActivity extends ScheduleActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // check whether tablet is in portrait or landscape mode and set the layout accordingly
        // landscape mode shows mode days than portrait mode
        int screenOrientation = getResources().getConfiguration().orientation;
        if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            setContentView(R.layout.schedule_edit_activity);
        }
        else
        {
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
        // TODO: refactor for redundancy. Left some sample code in the for loop
        LinearLayout weekday = (LinearLayout) findViewById(R.id.layoutMonday);
        weekday.addView(addButton());
        weekday = (LinearLayout) findViewById(R.id.layoutTuesday);
        weekday.addView(addButton());
        weekday = (LinearLayout) findViewById(R.id.layoutWednesday);
        weekday.addView(addButton());
        weekday = (LinearLayout) findViewById(R.id.layoutThursday);
        weekday.addView(addButton());
        weekday = (LinearLayout) findViewById(R.id.layoutFriday);
        weekday.addView(addButton());
        weekday = (LinearLayout) findViewById(R.id.layoutSaturday);
        weekday.addView(addButton());
        weekday = (LinearLayout) findViewById(R.id.layoutSunday);
        weekday.addView(addButton());
        /*
        RelativeLayout level1 = (RelativeLayout) findViewById(R.id.completeWeekLayout);

        int childcount = level1.getChildCount();

        // find each of the individual week days
        for (int i = 0; i < childcount; i++)
        {
            View v = parentLayout.getChildAt(i);
            RelativeLayout childView = (RelativeLayout) v;
            childView.addView(addButton());

        }*/
    }

    public void addItems(Bitmap bm, LinearLayout layout)
    {
        try
        {
            LifeStory.getInstance().setCurrentStory(new Sequence());

            ImageView iw = new ImageView(this);
            iw.setBackgroundResource(R.drawable.week_schedule_bg_tile);
            iw.setImageBitmap(resizeBitmap(bm, 100, 100));

            // set padding of each imageview containing
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 20, 0, 20);
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
        lp.setMargins(0, 20, 0, 20);
        iv.setLayoutParams(lp);
        iv.setBackgroundResource(R.drawable.week_schedule_bg_tile);
        Drawable resizedDrawable = resizeDrawable(R.drawable.add, 100, 100);
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
                        addItems(bitmap, weekdayLayout);
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
}