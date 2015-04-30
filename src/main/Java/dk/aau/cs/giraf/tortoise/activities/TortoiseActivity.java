package dk.aau.cs.giraf.tortoise.activities;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;

public class TortoiseActivity extends GirafActivity
{
    @Override
    protected void onResume()
    {

        super.onResume();
        //hideNavigationBar();
    }

    private void hideNavigationBar()
    {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    // method for exiting the current activity
    public void doExit(View v)
    {
        finish();
    }

    // TODO: should be moved
    public Drawable resizeDrawable(int srcDrawable, int width, int height)
    {
        Drawable tempDrawable = getResources().getDrawable(srcDrawable);
        Bitmap b = ((BitmapDrawable) tempDrawable).getBitmap();
        Drawable finalDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(b, width, height, false));

        return finalDrawable;
    }

    // TODO: should be moved
    public Drawable resizeDrawable(Drawable srcDrawable, int width, int height)
    {
        Bitmap b = ((BitmapDrawable) srcDrawable).getBitmap();
        Drawable finalDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(b, width, height, false));

        return finalDrawable;
    }

    // TODO: should be moved
    public Bitmap resizeBitmap(Bitmap srcBitmap, int width, int height)
    {
        Bitmap originalBitmap = srcBitmap;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

        return resizedBitmap;
    }

    public void addContentToMediaFrame(MediaFrame mf, long[] checkoutIds) {

        List<Long> pictoIDList = new ArrayList<Long>();

        // get the pictograms that are currently being shown
        List<Pictogram> pictoList = mf.getContent();

        // put all their IDs in a list
        for(Pictogram p : pictoList)
        {
            pictoIDList.add(p.getId());
        }

        for (int i = 0; i < checkoutIds.length; i++)
        {
            Pictogram picto = new Helper(getApplicationContext()).pictogramHelper.getById( checkoutIds[i]);


            boolean shouldAddToList = true;

            // if pictogram already exists, don't add it. We don't want duplicates
            for (Long element : pictoIDList)
            {
                if(element == picto.getId())
                {
                    shouldAddToList = false;
                }
            }

            if(shouldAddToList)
            {
                // add pictogram
                pictoIDList.add(picto.getId());
                mf.addContent(picto);
            }
        }

    }


}
