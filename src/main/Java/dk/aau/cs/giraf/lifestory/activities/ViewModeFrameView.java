package dk.aau.cs.giraf.lifestory.activities;

import android.content.Context;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.lifestory.AbstractFrameView;
import dk.aau.cs.giraf.lifestory.Frame;
import dk.aau.cs.giraf.lifestory.controller.MediaFrame;

public class ViewModeFrameView extends AbstractFrameView implements OnDragListener, OnClickListener {
	
	ViewModeActivity viewModeActivity;
	
	public ViewModeFrameView(ViewModeActivity viewModeActivity, Context context, RelativeLayout parentLayout,
			MediaFrame mediaFrame, Frame frame, int width, int height) {
		super(context, parentLayout, mediaFrame, frame, width, height);
		this.viewModeActivity = viewModeActivity;
		init();
	}
	
	private void init() {
		this.setOnDragListener(this);
		this.setOnClickListener(this);
	}
	
	public void detachPictograms() {
		for(Pictogram p : this.getMediaFrame().getContent()) {
			if (p.getParent() instanceof LinearLayout)
				((LinearLayout)p.getParent()).removeView(p);
			else if(p.getParent() instanceof ViewModeFrameView)
				((ViewModeFrameView)p.getParent()).removeView(p);
		}
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		switch (event.getAction()) {
		case DragEvent.ACTION_DROP:
			Pictogram p =(Pictogram)event.getLocalState();
			viewModeActivity.movePictogram(p, p.getParent(), this);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		viewModeActivity.currentViewModeFrame = this;
		viewModeActivity.renderPictos();
	}
	
	
}
