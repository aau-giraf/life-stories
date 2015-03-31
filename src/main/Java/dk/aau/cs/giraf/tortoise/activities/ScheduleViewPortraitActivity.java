package dk.aau.cs.giraf.tortoise.activities;

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

import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.fragments.*;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

/**
 * Taken from http://developer.android.com/training/animation/screen-slide.html
 */
public class ScheduleViewPortraitActivity extends FragmentActivity {
    /**
     * The number of pages in the ViewPager.
     */
    private static final int NUM_PAGES = 7;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

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

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.horizontal_view_pager);
        mPagerAdapter = new WeekDayPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        setCurrentDay(weekDaySelected);
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
                    return MondayFragment.newInstance();

                case 1: //This will show Tuesday
                    return TuesdayFragment.newInstance();

                case 2: //This will show Wednesday
                    return WednesdayFragment.newInstance();

                case 3: //This will show Thursday
                    return ThursdayFragment.newInstance();

                case 4: //This will show Friday
                    return FridayFragment.newInstance();

                case 5: //This will show Saturday
                    return SaturdayFragment.newInstance();

                case 6: //This will show Sunday
                    return SundayFragment.newInstance();

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