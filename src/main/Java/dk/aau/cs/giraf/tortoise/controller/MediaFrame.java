package dk.aau.cs.giraf.tortoise.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.EqualsUtil;
import dk.aau.cs.giraf.dblib.models.OasisObserver;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.dblib.models.Pictogram;

public class MediaFrame extends AbstractMediaFrame{

    private Pictogram choicePictogram;
	private List<Pictogram> content;
	private OnContentChangedEventListener mListener;
    private int posY;
    private long nestedSequenceID;
    private long pictogramId;
    private boolean marked = false;
	
	public MediaFrame(){
		super();
		setContent(new ArrayList<Pictogram>());
	}
	
	public MediaFrame(Context context, SerializableMediaFrame m) {
		setContent(new ArrayList<Pictogram>());
        Helper helper = new Helper(context);
		for(Long p : m.getContent()) {
			this.getContent().add(helper.pictogramHelper.getById(p));
		}
		this.choiceNumber = m.choiceNumber;
		this.frames = m.frames;
	}


    public long getNestedSequenceID() {
        return nestedSequenceID;
    }

    public void setNestedSequenceID(long nestedSequenceID) {
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
/*		if(mListener != null && getContent().size() == 2)
			mListener.OnIsChoiceListener(this, true);
		else if (mListener != null)
			mListener.OnContentSizeChanged(this);*/
	}
    public Pictogram getChoicePictogram(){
        return choicePictogram;
    }
    public void setChoicePictogram(Pictogram picto){
        choicePictogram = picto;
    }

    public int getPosY(){return this.posY;}
    public void setPosY(int value){ this.posY = value;}

    public long getPictogramId(){
        return this.pictogramId;
    }
    public void setPictogramId(long value){
        this.pictogramId = value;
    }

	
	public void removeContent(Pictogram content){
		this.content.remove(content);
		if(mListener != null && getContent().size() == 1)
			mListener.OnIsChoiceListener(this, false);
		else if (mListener != null)
			mListener.OnContentSizeChanged(this);
	}

    public boolean getMarked(){
        return marked;
    }

    public void setMarked(boolean value){
        marked = value;
    }
	
	public SerializableMediaFrame getSerializableMediaFrame() {
		return new SerializableMediaFrame(this);
	}

    @Override
    public String toString() {
        String localOutput =  getPosY() + ","  + getPictogramId() + ","  + getNestedSequenceID() ;
        return localOutput;
    }

    @Override
    public boolean equals(Object aCategory) {
        if ( this == aCategory ) return true;

        if ( !(aCategory instanceof MediaFrame) ) return false;

        MediaFrame profileCategory = (MediaFrame)aCategory;

        return EqualsUtil.areEqual(this.getPosY(), profileCategory.getPosY()) &&
                EqualsUtil.areEqual(this.getPictogramId(), profileCategory.getPictogramId())&&
                EqualsUtil.areEqual(this.getNestedSequenceID(), profileCategory.getNestedSequenceID())&&
                EqualsUtil.areEqual(this.getContent(), profileCategory.getContent());
    }
}
