package dk.aau.cs.giraf.tortoise;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.tortoise.activities.ViewModeActivity;

public class ChoiceFrameView extends FrameLayout implements OnDragListener{
	
	float scale;
	private PictogramView mediaFrame;
	private Pictogram pictogram;
	ViewModeActivity viewModeActivity;
	
	public ChoiceFrameView(ViewModeActivity viewModeActivity, PictogramView mediaFrame, Pictogram pictogram, LinearLayout.LayoutParams params) {
		super(viewModeActivity.getApplicationContext());
		this.viewModeActivity = viewModeActivity;
		this.setMediaFrame(mediaFrame);
		this.setPictogram(pictogram);
		this.scale = viewModeActivity.getApplicationContext().getResources().getDisplayMetrics().density;
		this.setBackgroundResource(R.layout.border);
		this.setPadding((int)(15*scale), (int) (15*scale), (int) (15*scale), (int) (15*scale));
		this.addView(mediaFrame);
		this.setLayoutParams(params);
	}

	public PictogramView getMediaFrame() {
		return mediaFrame;
	}

	public void setMediaFrame(PictogramView mediaFrame) {
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
     * Always returns false, so that the onDragEvent() method is invoked (or else the event is consumed).
     * see the documentation for onDrag
     * @param v
     * @param event
     * @return
     */
	@Override
	public boolean onDrag(View v, DragEvent event) {
		switch (event.getAction()) {
		case DragEvent.ACTION_DROP:
			PictogramView p = (PictogramView)v;
			viewModeActivity.movePictogram(p, p.getParent(), v);
			break;
		default:
			break;
		}
		return false;
	}

}
