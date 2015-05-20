package dk.aau.cs.giraf.lifestory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.RelativeLayout;

import dk.aau.cs.giraf.lifestory.activities.EditModeActivity;
import dk.aau.cs.giraf.lifestory.activities.EditModeFrameView;
import dk.aau.cs.giraf.lifestory.controller.MediaFrame;
import dk.aau.cs.giraf.lifestory.helpers.LifeStory;

public class LayoutTools {
    
	static int offsetX = 0;
    static int offsetY = 0;
    static int roundX = 0;
    static int roundY = 0;
    static int oldLeftMargin;
    static int oldTopMargin;
	
    public static void removeEditModeFrameView(EditModeFrameView editModeFrameView) {
        RelativeLayout mainLayout = (RelativeLayout) editModeFrameView.getParent();
        int tempChoiceNumber = editModeFrameView.getMediaFrame().getChoiceNumber();
        if(editModeFrameView != null) {
            if (editModeFrameView.getMediaFrame().getFrames().size() == 1) {
                if (editModeFrameView.getMediaFrame().getChoiceNumber() > 0) {
                    LifeStory.getInstance().getCurrentStory().decrementNumChoices();
                    for(MediaFrame m : LifeStory.getInstance().getCurrentStory().getMediaFrames()) {
                        if(m.getChoiceNumber() > tempChoiceNumber) {
                            m.setChoiceNumber(m.getChoiceNumber() - 1);
                        }
                    }
                    for(int i = 0; i < mainLayout.getChildCount(); i++) {
                        if(((EditModeFrameView)mainLayout.getChildAt(i))
                                .getMediaFrame().getChoiceNumber() >= tempChoiceNumber) {
                            ((EditModeFrameView)mainLayout.getChildAt(i)).removeText();
                            ((EditModeFrameView)mainLayout.getChildAt(i)).addText(
                                    "Valg " + ((EditModeFrameView)mainLayout.getChildAt(i))
                                    .getMediaFrame().getChoiceNumber());
                        }   
                    }
                }
                LifeStory.getInstance().getCurrentStory().getMediaFrames().remove(editModeFrameView.getMediaFrame());
            }
            else {
                editModeFrameView.getMediaFrame().removeFrame(editModeFrameView.getFrame());
                editModeFrameView.setMediaFrame(null);
            }
            mainLayout.removeView(editModeFrameView);
        }
    }
    
    public static EditModeFrameView getEditModeFrameView(EditModeActivity mainActivity, RelativeLayout mainLayout, int width, int height) {
    	MediaFrame mediaFrame = new MediaFrame();
    	Frame frame = new Frame(width, height, new Point(0, 0));
		mediaFrame.addFrame(frame);
    	EditModeFrameView editModeFrameView = new EditModeFrameView(mainActivity, mainActivity.getApplicationContext(),  mainLayout, mediaFrame, frame, height, width);
    	return editModeFrameView;
    }
    
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, Context context, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        
        bitmap.recycle();
        return output;
    }
	
	public static Bitmap getSquareBitmap(Bitmap bm) {
		
		if (bm.getWidth() >= bm.getHeight()){

		bm = Bitmap.createBitmap(
		     bm, 
		     bm.getWidth()/2 - bm.getHeight()/2,
		     0,
		     bm.getHeight(), 
		     bm.getHeight()
		     );
		}else{
		bm = Bitmap.createBitmap(
				bm,
		     0, 
		     bm.getHeight()/2 - bm.getWidth()/2,
		     bm.getWidth(),
		     bm.getWidth() 
		     );
		}
		
		return bm;
	}
	
	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);
	    
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
	
	public static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	    return inSampleSize;
	}
    
	public static boolean placeFrame(RelativeLayout mainLayout, EditModeFrameView v, int x, int y) {
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
		int gridInterval = (int) v.scale * 19;
		oldLeftMargin = params.leftMargin;
		oldTopMargin = params.topMargin;
		
		params.leftMargin = x;
		params.topMargin = y;
		
		if(params.leftMargin + params.width > mainLayout.getWidth())
			params.leftMargin = mainLayout.getWidth() - params.width;
		else if(params.leftMargin < 0)
			params.leftMargin = 0;
		
		if(params.topMargin + params.height > mainLayout.getHeight())
			params.topMargin = mainLayout.getHeight() - params.height;
		else if(params.topMargin < 0)
			params.topMargin = 0;
		
		offsetX = params.leftMargin % gridInterval;
		if (offsetX > gridInterval / 2)
			roundX = 1;
		else
			roundX = 0;
		offsetY = params.topMargin % gridInterval;
		if (offsetY > gridInterval / 2)
			roundY = 1;
		else
			roundY = 0;
		params.leftMargin = params.leftMargin - offsetX + (roundX * gridInterval);
		params.topMargin = params.topMargin - offsetY + (roundY * gridInterval);
		Log.i("mainLayout", "New X: " + params.leftMargin + ", New Y: " + params.topMargin + ", Old X: " + oldLeftMargin + ", Old Y: " + oldTopMargin + ", Grid: " + gridInterval);
		if(params.leftMargin + params.width > mainLayout.getWidth() 
				|| params.leftMargin < 0
				|| params.topMargin + params.height > mainLayout.getHeight()
				|| params.topMargin < 0) {
			params.leftMargin = oldLeftMargin;
			params.topMargin = oldTopMargin;
		}
		
		v.getFrame().getPosition().x = params.leftMargin;
		v.getFrame().getPosition().y = params.topMargin;
		v.setLayoutParams(params);
		if (v.getParent() != mainLayout && !(params.leftMargin == 0 && params.topMargin == 0)) {
			LifeStory.getInstance().getCurrentStory().getMediaFrames().add(v.getMediaFrame());
			mainLayout.addView(v);
		}
		v.bringToFront();
		return true;
	}
}
