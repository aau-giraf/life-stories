package dk.aau.cs.giraf.tortoise.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleActivity extends TortoiseActivity{
    //TODO move common methods here

    public void startPictosearch(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());

        this.startActivityForResult(i, 2);
    }

    public void markCurrentWeekday()
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

    public String getWeekday()
    {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEE");
        String weekday = dateFormatter.format(date);

        // return week day with first letter as uppercase - e.g Mandag
        return weekday.substring(0, 1).toUpperCase() + weekday.substring(1);
    }

    private void unmarkWeekdays()
    {
        GToggleButton btn;

        try
        {
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
        }catch (NullPointerException ex)
        {
            // the exception is ignored because it is thrown when using portrait mode
            // the exception is a work-around
        }
    }
}
