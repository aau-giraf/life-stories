package dk.aau.cs.giraf.tortoise.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.EditChoiceFrameView;
import dk.aau.cs.giraf.tortoise.HorizontalSequenceViewGroup;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.SequenceAdapter;
import dk.aau.cs.giraf.tortoise.activities.ScheduleActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleEditActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewPortraitActivity;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;

/**
 * Creates a fragment of a friday from the layout file friday.xml
 */
public class FridayFragment extends AbstractFragment {
    /**
     * Returns an instance of this fragment, to use in a ViewPager
     * @return an instance of the FridayFragment
     */
    public static FridayFragment newInstance(int pictogramNum) {
        amountOfPictograms = pictogramNum;
        return new FridayFragment();
    }

    //Creates the view of a friday from the layout file friday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.friday, container, false);

        //Finds the button on top of the day name and disables it, it is not needed in portrait mode
        view.findViewById(R.id.friday).setEnabled(false);

        weekday = ScheduleViewActivity.weekdaySequences.get(4);
        currentWeekday = R.id.layoutFriday;
        addPictograms(view);

        return view;
    }
}