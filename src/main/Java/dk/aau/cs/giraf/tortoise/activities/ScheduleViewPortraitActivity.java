package dk.aau.cs.giraf.tortoise.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.fragments.*;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

/**
 * Taken from http://developer.android.com/training/animation/screen-slide.html
 */
public class ScheduleViewPortraitActivity extends TortoiseActivity {
    /**
     * The number of pages in the ViewPager.
     */
    private static final int NUM_PAGES = 7;

    private static int pictogramSize;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private static ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_edit_activity_portrait);

        Intent i = getIntent();

        int weekDaySelected = i.getIntExtra("weekDaySelected", 0);
        pictogramSize = i.getIntExtra("pictogramSize", 0);
        String scheduleName = i.getStringExtra("scheduleName");
        String citizenName = i.getStringExtra("childName");

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.horizontal_view_pager);
        mPagerAdapter = new WeekDayPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(NUM_PAGES);

        EditText scheduleTitle = (EditText) findViewById(R.id.editText);
        scheduleTitle.setText(scheduleName);
        scheduleTitle.setEnabled(false);

        setCurrentDay(weekDaySelected);
    }

    public static void clearAllPictogramBorders() {
        LinearLayout weekdayLayout;
        for (int i = 0; i < 7; i++) {
            switch(i) {
                case 0:
                    weekdayLayout = (LinearLayout) mPager.findViewById(R.id.layoutMonday);
                    clearPictogramBorders(weekdayLayout);
                    break;
                case 1:
                    weekdayLayout = (LinearLayout) mPager.findViewById(R.id.layoutTuesday);
                    clearPictogramBorders(weekdayLayout);
                    break;
                case 2:
                    weekdayLayout = (LinearLayout) mPager.findViewById(R.id.layoutWednesday);
                    clearPictogramBorders(weekdayLayout);
                    break;
                case 3:
                    weekdayLayout = (LinearLayout) mPager.findViewById(R.id.layoutThursday);
                    clearPictogramBorders(weekdayLayout);
                    break;
                case 4:
                    weekdayLayout = (LinearLayout) mPager.findViewById(R.id.layoutFriday);
                    clearPictogramBorders(weekdayLayout);
                    break;
                case 5:
                    weekdayLayout = (LinearLayout) mPager.findViewById(R.id.layoutSaturday);
                    clearPictogramBorders(weekdayLayout);
                    break;
                case 6:
                    weekdayLayout = (LinearLayout) mPager.findViewById(R.id.layoutSunday);
                    clearPictogramBorders(weekdayLayout);
                    break;
            }
        }
    }

    private static void clearPictogramBorders(View v) {
        LinearLayout dayLayout = (LinearLayout) v;
        int pictoCount = dayLayout.getChildCount();
        for (int i = 0; i < pictoCount; i++) {
            ImageView iv = (ImageView) dayLayout.getChildAt(i);
            iv.setBackgroundResource(0);
            iv.setPadding(0, 10, 0, 10);
        }
    }

    protected void setCurrentDay(int weekday) {
        mPager.setCurrentItem(weekday, true);
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    /**
     * A simple pager adapter that represents 7 Fragment objects, in
     * sequence.
     */
    private class WeekDayPagerAdapter extends FragmentStatePagerAdapter {
        public WeekDayPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: //This will show Monday
                    return MondayFragment.newInstance(pictogramSize);

                case 1: //This will show Tuesday
                    return TuesdayFragment.newInstance(pictogramSize);

                case 2: //This will show Wednesday
                    return WednesdayFragment.newInstance(pictogramSize);

                case 3: //This will show Thursday
                    return ThursdayFragment.newInstance(pictogramSize);

                case 4: //This will show Friday
                    return FridayFragment.newInstance(pictogramSize);

                case 5: //This will show Saturday
                    return SaturdayFragment.newInstance(pictogramSize);

                case 6: //This will show Sunday
                    return SundayFragment.newInstance(pictogramSize);

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}