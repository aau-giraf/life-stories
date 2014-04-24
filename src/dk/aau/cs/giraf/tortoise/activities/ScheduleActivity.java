package dk.aau.cs.giraf.tortoise.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.tortoise.R;

import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;

public class ScheduleActivity extends TortoiseActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //layout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.schedule_activity, null);
        setContentView(R.layout.schedule_activity);

        // mark the current weekday in the scheduler
        markCurrentWeekday();

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }
    }

    @Override
    protected void onResume()
    {

    }

    private void markCurrentWeekday()
    {
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

    // because the intial state of week day buttons are toggled
    // their on-click action is triggered by oncreate()
    // this variable makes sure that nothing is done on the first pass
    int numOfPasses = 0;

    public void weekdaySelected(View v)
    {
        if(numOfPasses == 7)
        {
            switch (v.getId())
            {
                case R.id.monday:

                    break;
                case R.id.tuesday:

                    break;
                case R.id.wednesday:

                    break;
                case R.id.thursday:
                    break;
                case R.id.friday:

                    break;
                case R.id.sunday:

                    break;
            }
        }else
        {
            numOfPasses++;
        }
    }

    private String getWeekday()
    {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEE");
        String weekday = dateFormatter.format(date);

        return weekday.substring(0, 1).toUpperCase() + weekday.substring(1);
    }
}
