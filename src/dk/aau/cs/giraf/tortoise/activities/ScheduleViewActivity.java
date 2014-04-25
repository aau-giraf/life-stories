package dk.aau.cs.giraf.tortoise.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.R;

import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleViewActivity extends TortoiseActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_view_activity);

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

    private void markCurrentWeekday()
    {
        unmarkWeekdays();

        String weekday = getWeekday();

        if(weekday.equals(getResources().getString(R.string.monday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.monday);
            btn.setToggled(false);
        }else if(weekday.equals(getResources().getString(R.string.tuesday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.tuesday);
            btn.setToggled(false);
        }else if(weekday.equals(getResources().getString(R.string.wednesday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.wednesday);
            btn.setToggled(false);
        }else if(weekday.equals(getResources().getString(R.string.thursday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.thursday);
            btn.setToggled(false);
        }else if(weekday.equals(getResources().getString(R.string.friday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.friday);
            btn.setToggled(false);
        }else if(weekday.equals(getResources().getString(R.string.saturday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.saturday);
            btn.setToggled(false);
        }else if(weekday.equals(getResources().getString(R.string.sunday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.sunday);
            btn.setToggled(false);
        }
    }

    private void unmarkWeekdays()
    {
        GToggleButton btn;

        btn = (GToggleButton) findViewById(R.id.monday);
        btn.setToggled(true);
        btn = (GToggleButton) findViewById(R.id.tuesday);
        btn.setToggled(true);
        btn = (GToggleButton) findViewById(R.id.wednesday);
        btn.setToggled(true);
        btn = (GToggleButton) findViewById(R.id.thursday);
        btn.setToggled(true);
        btn = (GToggleButton) findViewById(R.id.friday);
        btn.setToggled(true);
        btn = (GToggleButton) findViewById(R.id.saturday);
        btn.setToggled(true);
        btn = (GToggleButton) findViewById(R.id.sunday);
        btn.setToggled(true);
    }

    public void weekdaySelected(View v)
    {

        int btnId = v.getId();

        GToggleButton btn = (GToggleButton) findViewById(v.getId());

        // "push" week day button immediately to "disable" toggle feature.
        // The week day buttons should not act as normal buttons
        btn.setToggled(true);

         switch(btnId)
        {
            case R.id.monday:
                if(getWeekday().equals("Mandag"))
                {
                    btn.setToggled(false);
                }
                break;
            case R.id.tuesday:
                if(getWeekday().equals("Tirsdag"))
                {
                    btn.setToggled(false);
                }
                break;
            case R.id.wednesday:
                if(getWeekday().equals("Onsdag"))
                {
                    btn.setToggled(false);
                }
                break;
            case R.id.thursday:
                if(getWeekday().equals("Torsdag"))
                {
                    btn.setToggled(false);
                };
                break;
            case R.id.friday:
                if(getWeekday().equals("Fredag"))
                {
                    btn.setToggled(false);
                }
                break;
            case R.id.saturday:
                if(getWeekday().equals("Lørdag"))
                {
                    btn.setToggled(false);
                }
                break;
            case R.id.sunday:
                if(getWeekday().equals("Søndag"))
                {
                    btn.setToggled(false);
                }
                break;
        }
    }

    private String getWeekday()
    {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEE");
        String weekday = dateFormatter.format(date);

        // return week day with first letter as uppercase - e.g Mandag
        return weekday.substring(0, 1).toUpperCase() + weekday.substring(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");

            if (checkoutIds.length == 0)
            {
                GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
            }
        }
        else if (resultCode == RESULT_OK && requestCode == 2) {
            try{
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");
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

    public void startPictosearch(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());

        this.startActivityForResult(i, 2);
    }
}
