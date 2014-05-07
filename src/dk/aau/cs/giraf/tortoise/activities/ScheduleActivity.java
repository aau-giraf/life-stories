package dk.aau.cs.giraf.tortoise.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleActivity extends TortoiseActivity
{
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

    public void startPictosearchForScheduler(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.weekdayLayout = (LinearLayout) v.getParent();
        DetermineWeekSection(v);

        this.startActivityForResult(i, 3);
    }

    public void DetermineWeekSection(View v)
    {
        int weekdayId = v.getId();
        LinearLayout layout;

        switch (weekdayId)
        {
            case R.id.sectionMonday:
                layout = (LinearLayout) findViewById(R.id.layoutMonday);
                ScheduleEditActivity.weekdayLayout = layout;
                break;
            case R.id.sectionTuesday:
                layout = (LinearLayout) findViewById(R.id.layoutTuesday);
                ScheduleEditActivity.weekdayLayout = layout;
                break;
            case R.id.sectionWednesday:
                layout = (LinearLayout) findViewById(R.id.layoutWednesday);
                ScheduleEditActivity.weekdayLayout = layout;
                break;
            case R.id.sectionThursday:
                layout = (LinearLayout) findViewById(R.id.layoutThursday);
                ScheduleEditActivity.weekdayLayout = layout;
                break;
            case R.id.sectionFriday:
                layout = (LinearLayout) findViewById(R.id.layoutFriday);
                ScheduleEditActivity.weekdayLayout = layout;
                break;
            case R.id.sectionSaturday:
                layout = (LinearLayout) findViewById(R.id.layoutSaturday);
                ScheduleEditActivity.weekdayLayout = layout;
                break;
            case R.id.sectionSunday:
                layout = (LinearLayout) findViewById(R.id.layoutSunday);
                ScheduleEditActivity.weekdayLayout = layout;
                break;
            default:
                break;
        }
    }

    public void markCurrentWeekday()
    {
        unmarkWeekdays();

        String weekday = getWeekday();

        try
        {
            if(weekday.equals(getResources().getString(R.string.monday)))
            {
                GToggleButton btn = (GToggleButton) findViewById(R.id.monday);
                btn.setToggled(false);

                // this variable is for knowing which linear layout in the scroll view in ScheduleEditActivity
                // to put pictogram in
                // TODO: ugly workaround.. should be fixed
                ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutMonday);
                LinearLayout rl = (LinearLayout) findViewById(R.id.border_monday);
                rl.setBackgroundResource(R.layout.weekday_selected);
            }else if(weekday.equals(getResources().getString(R.string.tuesday)))
            {
                GToggleButton btn = (GToggleButton) findViewById(R.id.tuesday);
                btn.setToggled(false);

                // TODO: ugly workaround.. should be fixed
                ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutTuesday);
                LinearLayout rl = (LinearLayout) findViewById(R.id.border_tuesday);
                rl.setBackgroundResource(R.layout.weekday_selected);
            }else if(weekday.equals(getResources().getString(R.string.wednesday)))
            {
                GToggleButton btn = (GToggleButton) findViewById(R.id.wednesday);
                btn.setToggled(false);

                // TODO: ugly workaround.. should be fixed
                ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutWednesday);

                // highlights current day with a border
                // TODO: put this in method
                LinearLayout rl = (LinearLayout) findViewById(R.id.border_wednesday);
                rl.setBackgroundResource(R.layout.weekday_selected);
            }else if(weekday.equals(getResources().getString(R.string.thursday)))
            {
                GToggleButton btn = (GToggleButton) findViewById(R.id.thursday);
                btn.setToggled(false);

                // TODO: ugly workaround.. should be fixed
                ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutThursday);
                LinearLayout rl = (LinearLayout) findViewById(R.id.border_thursday);
                rl.setBackgroundResource(R.layout.weekday_selected);
            }else if(weekday.equals(getResources().getString(R.string.friday)))
            {
                GToggleButton btn = (GToggleButton) findViewById(R.id.friday);
                btn.setToggled(false);

                // TODO: ugly workaround.. should be fixed
                ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutFriday);
                LinearLayout rl = (LinearLayout) findViewById(R.id.border_friday);
                rl.setBackgroundResource(R.layout.weekday_selected);
            }else if(weekday.equals(getResources().getString(R.string.saturday)))
            {
                GToggleButton btn = (GToggleButton) findViewById(R.id.saturday);
                btn.setToggled(false);

                // TODO: ugly workaround.. should be fixed
                ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutSaturday);
                LinearLayout rl = (LinearLayout) findViewById(R.id.border_saturday);
                rl.setBackgroundResource(R.layout.weekday_selected);
            }else if(weekday.equals(getResources().getString(R.string.sunday)))
            {
                GToggleButton btn = (GToggleButton) findViewById(R.id.sunday);
                btn.setToggled(false);

                // TODO: ugly workaround.. should be fixed
                ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutSunday);
                LinearLayout rl = (LinearLayout) findViewById(R.id.border_sunday);
                rl.setBackgroundResource(R.layout.weekday_selected);
            }
        } catch (Exception ex)
        {
            // TODO: handling of exception should be made by making dynamic week days?
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
