package dk.aau.cs.giraf.tortoise.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
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
        // pushing a toggle button has no effect
        GToggleButton btn = (GToggleButton) findViewById(v.getId());
        btn.setToggled(true);
    }

    public void addItems()
    {
        try
        {
            LifeStory.getInstance().setCurrentStory(new Sequence());


            int ss = R.id.layoutTest;
            LinearLayout sv = (LinearLayout) findViewById(R.id.layoutTest);

            int i;

            for(i = 0; i < 50; i++)
            {
                RadioButton rb = new RadioButton(this);
                sv.addView(rb);
            }
        } catch (Exception ex)
        {
            GuiHelper.ShowToast(this, ex.toString());
        }
    }

    // this method handles pictograms sent back via an intent from pictosearch
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 2) {
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
    }
}