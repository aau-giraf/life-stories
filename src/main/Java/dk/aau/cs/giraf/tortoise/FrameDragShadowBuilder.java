package dk.aau.cs.giraf.tortoise;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.View.DragShadowBuilder;

import dk.aau.cs.giraf.tortoise.activities.EditModeFrameView;

public class FrameDragShadowBuilder extends DragShadowBuilder{
	private EditModeFrameView frame;
	private RectF rectangle;
	private Paint paint;
	 
    public FrameDragShadowBuilder(EditModeFrameView view) { 
        super(view); 
        this.frame = view;
        rectangle = new RectF(0, 0, this.frame.width, this.frame.height);
        paint = new Paint();
        
    } 
     
    @Override 
    public void onProvideShadowMetrics(Point shadowSize, Point touchPoint) { 
        // Fill in the size 
        shadowSize.x = frame.width;
        shadowSize.y = frame.height;
        // Fill in the location of the shadow relative to the touch. 
        // Here we center the shadow under the finger. 
        touchPoint.x = shadowSize.x / 2; 
        touchPoint.y = shadowSize.y / 2;
    } 
 
    @Override 
    public void onDrawShadow(Canvas canvas) { 
        //Draw the shadow view onto the provided canvas
    	paint.setColor(Color.WHITE);
        paint.setStyle(Style.FILL);
        canvas.drawRoundRect(rectangle, 20 / frame.scale, 20 / frame.scale, paint);
    	paint.setColor(Color.BLACK);
    	paint.setStyle(Style.STROKE);
    	paint.setStrokeWidth(4 / frame.scale);
        canvas.drawRoundRect(rectangle, 20 / frame.scale, 20 / frame.scale, paint);
    } 
}
