package dk.aau.cs.giraf.tortoise.helpers;

import android.content.Context;
import android.widget.Toast;

public class GuiHelper
{
    public static void ShowToast(Context context, CharSequence msg)
    {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }
}
