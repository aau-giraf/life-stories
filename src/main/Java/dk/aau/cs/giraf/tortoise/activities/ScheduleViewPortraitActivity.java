package dk.aau.cs.giraf.tortoise.activities;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import java.util.List;

import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.WeekSchedulePageFragment;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

public class ScheduleViewPortraitActivity extends ScheduleViewActivity {

    int weekDaySelected;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();

        weekDaySelected = i.getIntExtra("weekDaySelected", 1);

        setContentView(R.layout.schedule_edit_activity_portrait);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
    }

    protected void addFragments() {

    }
}
