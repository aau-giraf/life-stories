package dk.aau.cs.giraf.tortoise.fragments;

import android.content.Context;
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
import android.widget.RelativeLayout;
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
 * Creates a fragment of a friday from the layout file friday.xml
 */
public class FridayFragment extends Fragment {

    private static int amountOfPictograms;
    private static int currentActivity;

    private void addPictograms(final ViewGroup view) {


        LinearLayout scrollContent = (LinearLayout) view.findViewById(R.id.layoutFriday);
        ScrollView scrollView = (ScrollView) scrollContent.getParent();
        Sequence weekday = ScheduleViewActivity.weekdaySequences.get(4);
        List<Pictogram> pictograms = new ArrayList<Pictogram>();
        resizeScrollView(scrollView);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(view.getMeasuredWidth(),view.getMeasuredWidth());


        for (MediaFrame mf : weekday.getMediaFrames()) {
            pictograms.addAll(mf.getContent());
        }

        for (int i = 0; i < pictograms.size(); i++) {

            ImageView iw = new ImageView(getActivity().getApplicationContext());
            iw.setImageBitmap(resizeBitmap(pictograms.get(i).getImageData()));
            iw.setMaxWidth(iw.getHeight());

            iw.setScaleY(0.8f);
            iw.setScaleX(0.8f);
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
            });
            iw.setPadding(0, 20, 0, 20);
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
                scrollView.setY(420);
                break;
            case 1:
                scrollView.getLayoutParams().height = 395;
                scrollView.setY(198);
                break;
            default:
                scrollView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    private Bitmap resizeBitmap (Bitmap originalBitmap) {
          return Bitmap.createScaledBitmap(originalBitmap, 188, 188, false);
    }

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