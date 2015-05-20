package dk.aau.cs.giraf.lifestory.activities;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.lifestory.AbstractFrameView;
import dk.aau.cs.giraf.lifestory.Frame;
import dk.aau.cs.giraf.lifestory.LayoutTools;
import dk.aau.cs.giraf.lifestory.controller.MediaFrame;
import dk.aau.cs.giraf.lifestory.interfaces.OnCurrentFrameEventListener;
import dk.aau.cs.giraf.lifestory.interfaces.OnMainLayoutEventListener;
import dk.aau.cs.giraf.lifestory.interfaces.OnMediaFrameEventListener;
import dk.aau.cs.giraf.lifestory.R;

public class EditModeFrameView extends AbstractFrameView implements OnDragListener,
        OnCurrentFrameEventListener,
        OnMediaFrameEventListener,
        OnMainLayoutEventListener,
																	OnTouchListener {

	EditModeActivity mainActivity;
	int touchPointX;
	int touchPointY;
	
	public EditModeFrameView(EditModeActivity mainActivity, Context context, RelativeLayout parentLayout,
			MediaFrame mediaFrame, Frame frame, int width, int height) {
		super(context, parentLayout, mediaFrame, frame, width, height);
		this.mainActivity = mainActivity;
		init();
	}
	
	private void init() {
		this.setOnTouchListener(this);
		this.setOnDragListener(this);
		mainActivity.addOnCurrentFrameEventListener(this);
		mainActivity.addOnMediaFrameChangedListener(this);
		mainActivity.addOnMainLayoutEventListener(this);
	}
	
	public void detachPictograms() {
		for(Pictogram p : this.getMediaFrame().getContent()) {
			if (p.getParent() instanceof FrameLayout)
				((FrameLayout)p.getParent()).removeView(p);
			else if(p.getParent() instanceof EditModeFrameView)
				((EditModeFrameView)p.getParent()).removeView(p);
		}

        //this.removePictogram();
	}
	
	public void highLight() {
		this.setScaleX(1.05f);
		this.setScaleY(1.05f);
	}
	
	public void lowLight() {
		this.setScaleX(1f);
		this.setScaleY(1f);
	}
	
	@Override
	public void setMediaFrame(MediaFrame mediaFrame) {
		this.mediaFrame = mediaFrame;
		if(this.mediaFrame == null) {
			mainActivity.removeOnCurrentFrameEventListener(this);
		}
		for(OnMediaFrameEventListener e : mainActivity.mediaFrameListeners) {
			e.OnMediaFrameChanged(this.getMediaFrame());
		}
	}
	
	@Override
	public boolean onDrag(View v, DragEvent event) {
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			if (event.getLocalState() == this) {
				setVisibility(View.GONE);
				return true;
			}
			else {
				return false;
			}
		case DragEvent.ACTION_DROP:
			EditModeFrameView view = (EditModeFrameView) event.getLocalState();
			LayoutTools.placeFrame(parentLayout, view,
                    (int) ((event.getX() + this.getLeft()) - (view.width / 2)),
                    (int) (event.getY() + this.getTop()) - (view.height / 2));
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void OnCurrentFrameChanged(EditModeFrameView editModeFrameView, int ChoiceNumber) {
		Log.i("mainLayout", "" + ChoiceNumber);
		if (this.getMediaFrame().getChoiceNumber() == 0 
				&& this == editModeFrameView){
			this.highLight();
			this.setBackgroundResource(R.layout.border_selected);
		}
		else if(this.getMediaFrame().getChoiceNumber() > 0 
				&& this.getMediaFrame().getChoiceNumber() == ChoiceNumber) {
			this.highLight();
			this.setBackgroundResource(R.layout.border_selected);
		}
		else {
			this.lowLight();
			this.setBackgroundResource(R.layout.border);
		}
	}

	@Override
	public void OnMediaFrameChanged(MediaFrame mediaFrame) {
		if(mediaFrame == this.getMediaFrame()) {
			this.highLight();
			this.setBackgroundResource(R.layout.border_selected);
		}
	}

	@Override
	public void OnMainLayoutTouchListener() {
		this.lowLight();
		this.setBackgroundResource(R.layout.border);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.touchPointX = (int) event.getX();
			this.touchPointY = (int) event.getY();
			this.bringToFront();
			break;
		case MotionEvent.ACTION_MOVE:
			if(canMove(event)) {
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
				v.startDrag(data, shadowBuilder, v, 0);
				return false;
			}
			else
				return true;
		case MotionEvent.ACTION_UP:
			if(!canMove(event)) {
				this.touchPointX = (int) event.getX();
				this.touchPointY = (int) event.getY();
				this.bringToFront();
				mainActivity.currentEditModeFrame = this;
				for(OnCurrentFrameEventListener e : mainActivity.currentFrameListeners) {
					e.OnCurrentFrameChanged(this, this.getMediaFrame().getChoiceNumber());
				}
			}
			break;
		default:
			break;
		}
		return true;
	}
	
	private boolean canMove(MotionEvent event) {
		int normDiffX = (int) ((touchPointX - event.getX()) < 0 
				? -(touchPointX - event.getX()) : (touchPointX - event.getX()));
		int normDiffY = (int) ((touchPointY - event.getY()) < 0 
				? -(touchPointY - event.getY()) : (touchPointY - event.getY()));
		return normDiffX > 8 || normDiffY > 8 ? true : false;
	}

}
