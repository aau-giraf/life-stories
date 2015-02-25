package dk.aau.cs.giraf.tortoise.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import dk.aau.cs.giraf.tortoise.R;

public class GuiHelper extends Activity
{
    public static void ShowToast(Context context, CharSequence msg)
    {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }
}
