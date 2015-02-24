package dk.aau.cs.giraf.tortoise;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
	
	private static final int DEFAULT_CORNER_RADIUS = 10;
	
	private float cornerRadius;
 
    public RoundedImageView(Context context, float radius) {
        super(context);
        
        cornerRadius = radius;
        ensureClipPathSupport();
    }
    
    public RoundedImageView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	
		cornerRadius = getCornerRadius(context, attrs);
		ensureClipPathSupport();
    }
 
    public RoundedImageView(Context context, AttributeSet attrs, float radius) {
        this(context, attrs);
        
        cornerRadius = getCornerRadius(context, attrs);
        ensureClipPathSupport();
    }
 
    public RoundedImageView(Context context, AttributeSet attrs, int defStyle, float radius) {
        super(context, attrs, defStyle);

        cornerRadius = radius;
        ensureClipPathSupport();
    }
    
	private int getCornerRadius(Context context, AttributeSet attrs) {
		int cornerRadius = DEFAULT_CORNER_RADIUS;
		
		TypedArray a = context.obtainStyledAttributes(attrs,
				new int[] {0x7f010003});
		try {
			cornerRadius = a.getDimensionPixelSize(
					0, DEFAULT_CORNER_RADIUS);
		} finally {
			a.recycle();
		}
		
		return cornerRadius;
	}
    
    private void ensureClipPathSupport()
    {
    	// Disable hardware acceleration to support clipPath in newer versions of android
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
    	   this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
 
    @Override
    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        
    	canvas.drawColor(0x28FFFFFF);
        
        super.onDraw(canvas);
        
    }    
}