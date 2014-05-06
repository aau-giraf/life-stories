package dk.aau.cs.giraf.tortoise.activities;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TortoiseActivity extends Activity
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
    public Bitmap resizeBitmap(Bitmap srcBitmap, int width, int height)
    {
        Bitmap originalBitmap = srcBitmap;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

        return resizedBitmap;
    }


}
