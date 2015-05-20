package dk.aau.cs.giraf.lifestory.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.Toast;

public class GuiHelper extends Activity
{
    public static void ShowToast(Context context, CharSequence msg)
    {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }

    public static Bitmap ConstructWhiteBackground(Bitmap bitmap, Resources res){
        Bitmap imageWithBG = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());  // Create another image the same size

        imageWithBG.eraseColor(Color.WHITE);  // set its background to white, or whatever color you want

        Drawable[] dList = new Drawable[2];
        Drawable d = new BitmapDrawable(res, imageWithBG);
        Drawable d2 = new BitmapDrawable(res, bitmap);
        dList[0] = d;
        dList[1] = d2;
        LayerDrawable layers = new LayerDrawable(dList);

        int width = layers.getIntrinsicWidth();
        int height = layers.getIntrinsicHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        layers.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        layers.draw(canvas);

        return newBitmap;
    }
}
