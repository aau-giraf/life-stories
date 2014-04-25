package dk.aau.cs.giraf.tortoise.activities;


import android.app.Activity;
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
}
