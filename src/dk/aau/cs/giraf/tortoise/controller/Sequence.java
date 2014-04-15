package dk.aau.cs.giraf.tortoise.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.tortoise.LayoutTools;

public class Sequence extends AbstractSequence {
	
	private List<MediaFrame> mediaFrames;
	private Bitmap titleImage;
	private OnNumChoicesEventListener mListener;
	
	public Sequence(){
		super();
		setMediaFrames(new ArrayList<MediaFrame>());
		titleImage = null;
	}
	
	public Sequence(Context context, SerializableSequence s) {
		setMediaFrames(new ArrayList<MediaFrame>());
		for(SerializableMediaFrame m : s.getMediaFrames()) {
			this.getMediaFrames().add(m.getMediaFrame(context));
		}
		this.numChoices = s.numChoices;
		this.title = s.title;
		this.titlePictoId = s.titlePictoId;
		Bitmap bitmap = PictoFactory.getPictogram(context, this.getTitlePictoId()).getImageData();
		bitmap = LayoutTools.getSquareBitmap(bitmap);
		bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, context, 20);
		this.titleImage = bitmap;
	}
	
	public interface OnNumChoicesEventListener {
		
		public void onNumChoicesChanged(int numChoices);
	}
	
	public void setOnNumChoicesEventListener(OnNumChoicesEventListener listener) {
		mListener = listener;
	}

	public List<MediaFrame> getMediaFrames() {
		return mediaFrames;
	}

	public void setMediaFrames(List<MediaFrame> mediaFrames) {
		this.mediaFrames = mediaFrames;
	}
	
	public void decrementNumChoices() {
		if (mListener != null)
			mListener.onNumChoicesChanged(this.getNumChoices());
		this.numChoices--;
	}
	
	public void incrementNumChoices() {
		if (mListener != null)
			mListener.onNumChoicesChanged(this.getNumChoices());
		this.numChoices++;
	}
	
	public Bitmap getTitleImage() {
		return titleImage;
	}
	
	public void setTitleImage(Bitmap titleImage) {
		this.titleImage = titleImage;
	}
	
	public SerializableSequence getSerializableSequence() {
		return new SerializableSequence(this);
	}
}
