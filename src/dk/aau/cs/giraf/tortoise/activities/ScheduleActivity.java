package dk.aau.cs.giraf.tortoise.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;

public class ScheduleActivity extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        GuiHelper.ShowToast(this,"God påske!");
        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data i intent");
            finish();
        }else
        {

        }
        finish();
    }
}
