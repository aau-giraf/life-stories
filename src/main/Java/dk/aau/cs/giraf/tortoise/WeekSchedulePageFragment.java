package dk.aau.cs.giraf.tortoise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Taken from http://developer.android.com/training/animation/screen-slide.html
 */
public class WeekSchedulePageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.schedule_edit_activity_portrait, container, false);

        return rootView;
    }
}
