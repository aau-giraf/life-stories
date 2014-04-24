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
    //View layout;

    protected void onCreate(Bundle savedInstanceState)
    {
        // mark the current weekday in the scheduler
        //markCurrentWeekday();

        super.onCreate(savedInstanceState);
        //layout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.schedule_activity, null);
        setContentView(R.layout.schedule_activity);

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {

            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }
    }

    private void markCurrentWeekday()
    {
        String weekday = getWeekday();

        if(weekday.equals("torsdag") || weekday.equals("thursday"))
        {
            GToggleButton btn = (GToggleButton) layout.findViewById(R.id.thursday);
            btn.setToggled(false);
            GuiHelper.ShowToast(this, "toggled");
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
                    GuiHelper.ShowToast(this, "Påskeferie!!");
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

        return weekday;
    }
}
