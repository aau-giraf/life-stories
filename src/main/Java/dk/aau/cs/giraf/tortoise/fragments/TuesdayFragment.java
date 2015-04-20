package dk.aau.cs.giraf.tortoise.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
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
import dk.aau.cs.giraf.tortoise.activities.ScheduleActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewPortraitActivity;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

/**
 * Creates a fragment of a tuesday from the layout file tuesday.xml
 */
public class TuesdayFragment extends Fragment {

    private static int amountOfPictograms;
    private static int currentActivity;

    private void addPictograms(ViewGroup view) {
        LinearLayout scrollContent = (LinearLayout) view.findViewById(R.id.layoutTuesday);
        ScrollView scrollView = (ScrollView) scrollContent.getParent();
        Sequence weekday = ScheduleViewActivity.weekdaySequences.get(1);
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


                    // index of pictogram being clicked
                    int index = getViewIndex(v);
                    LinearLayout dayLayout = (LinearLayout) v.getParent();

                    int pictoCount = dayLayout.getChildCount();
                    ImageView iv = (ImageView) dayLayout.getChildAt(index);

                    if(index == 0 || (index-1) == currentActivity || (index+1) == currentActivity)
                    {
                        currentActivity = index;
                        setPictogramSizes((View) v.getParent());

                        /*Re-size*/
                        v.setScaleX(1.2f);
                        v.setScaleY(1.2f);
                        iv.setColorFilter(null);

                    } else if((index+1) == pictoCount)
                    {

                        if(iv.getScaleX() == 1.2f) {
                        /*Re-size*/
                            iv.setScaleY(0.4f);
                            iv.setScaleX(0.4f);
                        /*Adding grey scale*/
                            ColorMatrix matrix = new ColorMatrix();
                            matrix.setSaturation(0);
                            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                            iv.setColorFilter(filter);
                        }
                    }


                }
            });
            iw.setPadding(0, 10, 0, 10);
            scrollContent.addView(iw);
        }
    }

    private void setPictogramSizes(View v) {
        LinearLayout dayLayout = (LinearLayout) v;
        int pictoCount = dayLayout.getChildCount();
        for (int i = 0; i < pictoCount; i++) {
            ImageView iv = (ImageView) dayLayout.getChildAt(i);

            if(i < currentActivity){
                /*Re-Size*/
                iv.setScaleY(0.4f);
                iv.setScaleX(0.4f);

                /*Greyscale*/
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                iv.setColorFilter(filter);



            }else if(i > currentActivity)
            {
                iv.setScaleX(0.8f);
                iv.setScaleY(0.8f);
                iv.setColorFilter(null);

            }

        }
    }

    private void resizeScrollView(ScrollView scrollView) {
        switch(amountOfPictograms) {
            case 0:
                scrollView.getLayoutParams().height = 198;
                break;
            case 1:
                scrollView.getLayoutParams().height = 420;
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
     * @return an instance of the TuesdayFragment
     */
    public static TuesdayFragment newInstance(int pictogramNum) {
        amountOfPictograms = pictogramNum;
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
    public int getViewIndex(View v)
    {
        int index;

        if(((LinearLayout) v.getParent()).getChildCount() > 0)
        {
            index = ((LinearLayout) v.getParent()).indexOfChild(v);
        }
        else
        {
            index = -1;
        }

        return index;
    }
}