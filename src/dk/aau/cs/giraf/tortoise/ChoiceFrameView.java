package dk.aau.cs.giraf.tortoise;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import dk.aau.cs.giraf.pictogram.Pictogram;

public class ChoiceFrameView extends FrameLayout implements OnDragListener{
	
	float scale;
	private MediaFrame mediaFrame;
	private Pictogram pictogram;
	ViewModeActivity viewModeActivity;
	
	public ChoiceFrameView(ViewModeActivity viewModeActivity, MediaFrame mediaFrame, Pictogram pictogram, LinearLayout.LayoutParams params) {
		super(viewModeActivity.getApplicationContext());
		this.viewModeActivity = viewModeActivity;
		this.setMediaFrame(mediaFrame);
		this.setPictogram(pictogram);
		this.scale = viewModeActivity.getApplicationContext().getResources().getDisplayMetrics().density;
		this.setBackgroundResource(R.layout.border);
		this.setPadding((int)(15*scale), (int) (15*scale), (int) (15*scale), (int) (15*scale));
		this.addView(pictogram);
		this.setLayoutParams(params);
	}

	public MediaFrame getMediaFrame() {
		return mediaFrame;
	}

	public void setMediaFrame(MediaFrame mediaFrame) {
		this.mediaFrame = mediaFrame;
	}

	public Pictogram getPictogram() {
		return pictogram;
	}

	public void setPictogram(Pictogram pictogram) {
		this.pictogram = pictogram;
	}

    /**
     * In case the DragEvent action is a drop, the Pictogram associated with the DragEvent
     * is moved from its parent to the View provided as the v parameter.
     * Always returns false, so that the onDragEvent() method is invoked. Dunno why.
     * @param v
     * @param event
     * @return
     */
	@Override
	public boolean onDrag(View v, DragEvent event) {
		switch (event.getAction()) {
		case DragEvent.ACTION_DROP:
			Pictogram p = (Pictogram)event.getLocalState();
			viewModeActivity.movePictogram(p, p.getParent(), v);
			break;
		default:
			break;
		}
		return false;
	}

}
