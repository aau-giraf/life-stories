package dk.aau.cs.giraf.tortoise.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.EditChoiceFrameView;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.activities.ScheduleViewActivity;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;

/**
 * Created by Bruger on 21-04-2015.
 */
public class AbstractFragment extends Fragment  {

    protected static int amountOfPictograms;
    protected int currentActivity;
    protected Sequence weekday;
    int mStackLevel = 0;
    protected int currentWeekday;

    protected void addPictograms(final ViewGroup view) {

        LinearLayout scrollContent = (LinearLayout) view.findViewById(currentWeekday);
        ScrollView scrollView = (ScrollView) scrollContent.getParent();
        List<Pictogram> pictograms = new ArrayList<Pictogram>();
        resizeScrollView(scrollView);

        for (MediaFrame mf : weekday.getMediaFrames()) {
            if (mf.getContent().size() == 1) {
                pictograms.addAll(mf.getContent());
            } else {
                pictograms.add(mf.getContent().get(0));
            }
        }

        for (int i = 0; i < pictograms.size(); i++) {

            if (weekday.getMediaFrames().get(i).getContent().size() == 1) {
                scrollContent.addView(SetSinglePictogram(pictograms.get(i)));
            } else if (weekday.getMediaFrames().get(i).getContent().size() > 1) {
                scrollContent.addView(SetChoicePictogram(pictograms.get(i), view));
            }


        }
    }

    private ImageView SetSinglePictogram(Pictogram pictogram){
        GuiHelper helper = new GuiHelper();

        ImageView iw = new ImageView(getActivity().getApplicationContext());
        iw.setImageBitmap(resizeBitmap(helper.ConstructWhiteBackground(pictogram.getImageData(), getResources())));
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
        iw.setPadding(0, 20, 0, 20);
        return iw;
    }

    private ImageView SetChoicePictogram(Pictogram pictogram, final ViewGroup view){
        GuiHelper helper = new GuiHelper();

        ImageView iw = new ImageView(getActivity().getApplicationContext());
        iw.setImageBitmap(resizeBitmap(helper.ConstructWhiteBackground(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_choose), getResources())));
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
        iw.setPadding(0, 20, 0, 20);
        return iw;
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

