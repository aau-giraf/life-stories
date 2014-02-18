	package dk.aau.cs.giraf.tortoise;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import dk.aau.cs.giraf.pictogram.Pictogram;

public abstract class AbstractFrameView extends FrameLayout {
	
	public int width;
	public int height;
	protected float scale;
	protected Frame frame;
	protected MediaFrame mediaFrame;
	@SuppressWarnings("unused")
	protected RelativeLayout parentLayout;
	protected TextView textView;
	protected Pictogram pictogram;
	protected RelativeLayout.LayoutParams outerLayoutParams;
	
	public AbstractFrameView(Context context, RelativeLayout parentLayout, MediaFrame mediaFrame, Frame frame, int width , int height) {
		super(context);
		this.parentLayout = parentLayout;
		this.mediaFrame = mediaFrame;
		this.frame = frame;
		this.scale = context.getResources().getDisplayMetrics().density;
		this.width = (int)(width * scale);
		this.height = (int)(height * scale);
		this.setForegroundGravity(Gravity.CENTER);
		this.setBackgroundResource(R.layout.border);
		setOuterLayoutParams(new RelativeLayout.LayoutParams(
				this.width, this.height));
		this.setPadding((int)(15*scale), (int) (15*scale), (int) (15*scale), (int) (15*scale));
		this.setLayoutParams(getOuterLayoutParams());
	}
	
	public void addText(String text) {
        if(textView == null) {
            textView = new TextView(this.getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(25);
            textView.setPadding(10, 10, 10, 10);
        }
        this.removeAllViews();
        textView.setText(text);
        this.addView(textView);
    }
	
	public void removeText() {
		if(textView != null)
			this.removeView(textView);
	}

	public MediaFrame getMediaFrame() {
		return mediaFrame;
	}

	public void setMediaFrame(MediaFrame mediaFrame) {
		this.mediaFrame = mediaFrame;
	}

	public Frame getFrame() {
		return frame;
	}

	public void setFrame(Frame frame) {
		this.frame = frame;
	}

	public Pictogram getPictogram() {
		return pictogram;
	}

	public void setPictogram(Pictogram pictogram) {
		this.pictogram = pictogram;
		FrameLayout.LayoutParams params =
				new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		pictogram.setLayoutParams(params);
		this.addView(pictogram);
	}
	
	public void removePictogram() {
		if(this.pictogram != null) {
			this.removeView(pictogram);
		}
	}

	public RelativeLayout.LayoutParams getOuterLayoutParams() {
		return outerLayoutParams;
	}

	public void setOuterLayoutParams(RelativeLayout.LayoutParams outerLayoutParams) {
		this.outerLayoutParams = outerLayoutParams;
	}

}
