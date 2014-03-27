package dk.aau.cs.giraf.tortoise;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;

public class MediaFrame extends AbstractMediaFrame{
	
	private List<Pictogram> content;
	private OnContentChangedEventListener mListener;
	
	public MediaFrame(){
		super();
		setContent(new ArrayList<Pictogram>());
	}
	
	public MediaFrame(Context context, SerializableMediaFrame m) {
		setContent(new ArrayList<Pictogram>());
		for(Integer p : m.getContent()) {
			this.getContent().add(PictoFactory.getPictogram(context, p));
		}
		this.choiceNumber = m.choiceNumber;
		this.frames = m.frames;
	}
	
	public interface OnContentChangedEventListener {
		
		public void OnIsChoiceListener(MediaFrame mediaFrame, boolean isChoice);
		public void OnContentSizeChanged(MediaFrame mediaFrame);
	}
	
	public void setOnContentChangedListener(OnContentChangedEventListener listener) {
		mListener = listener;
	}
	
	public List<Pictogram> getContent(){
		return content;
	}

	private void setContent(List<Pictogram> content){
		this.content = content;
	}
	
	public void addContent(Pictogram content){
		this.content.add(content);
		if(mListener != null && getContent().size() == 2)
			mListener.OnIsChoiceListener(this, true);
		else if (mListener != null)
			mListener.OnContentSizeChanged(this);
	}
	
	public void removeContent(Pictogram content){
		this.content.remove(content);
		if(mListener != null && getContent().size() == 1)
			mListener.OnIsChoiceListener(this, false);
		else if (mListener != null)
			mListener.OnContentSizeChanged(this);
	}
	
	public SerializableMediaFrame getSerializableMediaFrame() {
		return new SerializableMediaFrame(this);
	}
}
