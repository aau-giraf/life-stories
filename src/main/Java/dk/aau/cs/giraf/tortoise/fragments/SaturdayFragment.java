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
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

/**
 * Creates a fragment of a saturday from the layout file saturday.xml
 */
public class SaturdayFragment extends Fragment {

    protected static int bitmapSize;
    boolean pictogramInFocus = false;

    private void addPictograms(ViewGroup view) {
        LinearLayout scrollContent = (LinearLayout) view.findViewById(R.id.layoutSaturday);
        Sequence weekday = ScheduleViewActivity.weekdaySequences.get(5);
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
                    if (!pictogramInFocus) {
                        v.setBackgroundResource(R.layout.weekday_selected);
                        pictogramInFocus = true;
                    }
                    else if (pictogramInFocus) {
                        v.setBackgroundResource(0);
                        pictogramInFocus = false;
                    }
                }
            });
            scrollContent.addView(iw);
        }
    }

    private Bitmap resizeBitmap (Bitmap originalBitmap) {
        switch (bitmapSize) {
            case 1:
                return Bitmap.createScaledBitmap(originalBitmap, 276, 276, false);
            case 2:
                return Bitmap.createScaledBitmap(originalBitmap, 184, 184, false);
            default:
                return Bitmap.createScaledBitmap(originalBitmap, 92, 92, false);
        }
    }

    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the SaturdayFragment
     */
    public static SaturdayFragment newInstance(int pictogramSize) {
        bitmapSize = pictogramSize;
        return new SaturdayFragment();
    }

    //Creates a view of a saturday, using the layout file saturday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.saturday, container, false);

        //Finds the button on top of the day name and disables it, it is not needed in portrait mode
        view.findViewById(R.id.saturday).setEnabled(false);

        addPictograms(view);

        return view;
    }
}