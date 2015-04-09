package dk.aau.cs.giraf.tortoise.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
public class MondayFragment extends Fragment {

    private static int amountOfPictograms;

    private void addPictograms(ViewGroup view) {
        LinearLayout scrollContent = (LinearLayout) view.findViewById(R.id.layoutMonday);
        ScrollView scrollView = (ScrollView) scrollContent.getParent();
        Sequence weekday = ScheduleViewActivity.weekdaySequences.get(0);
        List<Pictogram> pictograms = new ArrayList<Pictogram>();
        resizeScrollView(scrollView);

        for (MediaFrame mf : weekday.getMediaFrames()) {
            pictograms.addAll(mf.getContent());
        }

        for (int i = 0; i < pictograms.size(); i++) {
            ImageView iw = new ImageView(getActivity().getApplicationContext());
            iw.setImageBitmap(resizeBitmap(pictograms.get(i).getImageData()));
            iw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Drawable backgroundDrawable = getResources().getDrawable(R.drawable.week_schedule_bg_tile);
                    v.setBackgroundDrawable(backgroundDrawable);

                }
            });
            iw.setPadding(0, 10, 0, 10);
            scrollContent.addView(iw);
        }
    }

    private void resizeScrollView(ScrollView scrollView) {
        RelativeLayout parent = (RelativeLayout) scrollView.getParent();
        switch(amountOfPictograms) {
            case 0:
                ScrollView.LayoutParams params1 = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, 198);
                params1.setMargins(0, (parent.getHeight() / 2) - 198, 0 , 0);
                scrollView.setLayoutParams(params1);
                break;
            case 1:
                ScrollView.LayoutParams params2 = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, 420);
                params2.setMargins(0, (parent.getHeight() / 2) - 420, 0 , 0);
                scrollView.setLayoutParams(params2);
                break;
            default:
                scrollView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    private Bitmap resizeBitmap (Bitmap originalBitmap) {
           return Bitmap.createScaledBitmap(originalBitmap, 188, 188, false);
    }

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

        addPictograms(view);

        return view;
    }
}
