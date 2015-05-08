package dk.aau.cs.giraf.tortoise.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;

public class MediaFrame extends AbstractMediaFrame {

    private Pictogram choicePictogram;
	private List<Pictogram> content;
	private OnContentChangedEventListener mListener;
    private int nestedSequenceID;
	
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

    public int getNestedSequenceID() {
        return nestedSequenceID;
    }

    public void setNestedSequenceID(int nestedSequenceID) {
        this.nestedSequenceID = nestedSequenceID;
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

	public void setContent(List<Pictogram> content){
		this.content = content;
	}
	
	public void addContent(Pictogram content){
		this.content.add(content);
	}
    public Pictogram getChoicePictogram(){
        return choicePictogram;
    }
    public void setChoicePictogram(Pictogram picto){
        choicePictogram = picto;
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
