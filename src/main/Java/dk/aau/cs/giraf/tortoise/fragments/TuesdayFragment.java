package dk.aau.cs.giraf.tortoise.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.activities.ScheduleActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewPortraitActivity;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

/**
 * Creates a fragment of a tuesday from the layout file tuesday.xml
 */
public class TuesdayFragment extends Fragment {

    protected static int bitmapSize;
    boolean pictogramInFocus = false;

    private void addPictograms(ViewGroup view) {
        LinearLayout scrollContent = (LinearLayout) view.findViewById(R.id.layoutTuesday);
        Sequence weekday = ScheduleViewActivity.weekdaySequences.get(1);
        List<Pictogram> pictograms = new ArrayList<Pictogram>();

        for (MediaFrame mf : weekday.getMediaFrames()) {
            pictograms.addAll(mf.getContent());
        }

        for (int i = 0; i < pictograms.size(); i++) {
            ImageView iw = new ImageView(getActivity().getApplicationContext());
            iw.setImageBitmap(resizeBitmap(pictograms.get(i).getImageData()));
            iw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        ScheduleViewPortraitActivity.clearAllPictogramBorders();
                        Drawable backgroundDrawable = getResources().getDrawable(R.drawable.week_schedule_bg_tile);
                        v.setBackgroundDrawable(backgroundDrawable);
                }
            });
            iw.setPadding(0, 10, 0, 10);
            scrollContent.addView(iw);
        }
    }

    private Bitmap resizeBitmap (Bitmap originalBitmap) {
        switch (bitmapSize) {
            case 1:
                return Bitmap.createScaledBitmap(originalBitmap, 838, 838, false);
            case 2:
                return Bitmap.createScaledBitmap(originalBitmap, 552, 552, false);
            default:
                return Bitmap.createScaledBitmap(originalBitmap, 188, 188, false);
        }
    }

    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the TuesdayFragment
     */
    public static TuesdayFragment newInstance(int pictogramSize) {
        bitmapSize = pictogramSize;
        return new TuesdayFragment();
    }

    //Creates a view of a tuesday, using the layout file tuesday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.tuesday, container, false);

        //Finds the button on top of the day name and disables it, it is not needed in portrait mode
        view.findViewById(R.id.tuesday).setEnabled(false);

        addPictograms(view);

        return view;
    }
}