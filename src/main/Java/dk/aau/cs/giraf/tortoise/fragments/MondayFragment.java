package dk.aau.cs.giraf.tortoise.fragments;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

/**
 * Creates a fragment of a monday from the layout file monday.xml
 */
public class MondayFragment extends AbstractFragment {
    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the MondayFragment
     */
    public static MondayFragment newInstance(int pictogramNum) {
        amountOfPictograms = pictogramNum;
        return new MondayFragment();
    }


    //Creates the view of a monday, using the layout file monday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.monday, container, false);

        //Finds the button on top of the day name and disables it, it is not needed in portrait mode
        view.findViewById(R.id.monday).setEnabled(false);

        weekday = ScheduleViewActivity.weekdaySequences.get(0);
        addPictograms(view);

        return view;
    }
}