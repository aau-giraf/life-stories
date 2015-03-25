package dk.aau.cs.giraf.tortoise.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.WeekSchedulePageFragment;

/**
 * Taken from http://developer.android.com/training/animation/screen-slide.html
 */
public class WeekSchedulePagerActivity extends FragmentActivity {
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

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.horizontal_view_pager);
        mPagerAdapter = new WeekDayPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    /**
     * A simple pager adapter that represents 7 WeekSchedulePageFragment objects, in
     * sequence.
     */
    private class WeekDayPagerAdapter extends FragmentStatePagerAdapter {
        public WeekDayPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new WeekSchedulePageFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}