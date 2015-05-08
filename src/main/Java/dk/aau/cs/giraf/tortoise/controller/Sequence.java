package dk.aau.cs.giraf.tortoise.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.PictogramView;

public class Sequence extends AbstractSequence {
	
	private List<PictogramView> mediaFrames;
	private Bitmap titleImage;
	private OnNumChoicesEventListener mListener;
    private boolean frameListChanged;

	public Sequence(){
		super();
		setMediaFrames(new ArrayList<PictogramView>());
		titleImage = null;
	}

    public Sequence(long id, long titlePictoId, String title, List<PictogramView> mediaFrames, Context con){
        super(id, titlePictoId, title);
        this.mediaFrames = mediaFrames;
        setBitmapFromTitlePictoID(con);
    }
	
	public Sequence(Context context, SerializableSequence s) {
		setMediaFrames(new ArrayList<PictogramView>());
		for(SerializableMediaFrame m : s.getMediaFrames()) {
			this.getMediaFrames().add(m.getMediaFrame(context));
		}
		this.numChoices = s.numChoices;
		this.title = s.title;
		this.titlePictoId = s.titlePictoId;
        setBitmapFromTitlePictoID(context);
	}


    private void setBitmapFromTitlePictoID(Context con){
        if(this.getTitlePictoId() == 0){
            this.titleImage = null;
        }
        else {
            Helper helper = new Helper(con);
            Bitmap bitmap = helper.pictogramHelper.getById(this.getTitlePictoId()).getImage();
            bitmap = LayoutTools.getSquareBitmap(bitmap);
            bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, con, 20);
            this.titleImage = bitmap;
        }
    }

    public void addFrame(PictogramView frame) {
        mediaFrames.add(frame);
        this.frameListChanged = true;
    }

    public void rearrange(int oldIndex, int newIndex) {
        if (oldIndex < 0 || oldIndex >= mediaFrames.size()) throw new IllegalArgumentException("oldIndex out of range");
        if (newIndex < 0 || newIndex >= mediaFrames.size()) throw new IllegalArgumentException("newIndex out of range");

        PictogramView temp = mediaFrames.remove(oldIndex);
        mediaFrames.add(newIndex, temp);
    }


	public interface OnNumChoicesEventListener {
		
		public void onNumChoicesChanged(int numChoices);
	}
	
	public void setOnNumChoicesEventListener(OnNumChoicesEventListener listener) {
		mListener = listener;
	}

	public List<PictogramView> getMediaFrames() {
		return mediaFrames;
	}

    public PictogramView getMediaFrame(int position){
        return mediaFrames.get(position);
    }

	public void setMediaFrames(List<PictogramView> mediaFrames) {
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

    public boolean getFrameListChanged() {
        return frameListChanged;
    }

    public void setFrameListChanged(boolean frameListChanged) {
        this.frameListChanged = frameListChanged;
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

    public void deleteMediaFrame(int position) {
        mediaFrames.remove(position);
    }
    public void deleteAllMediaFrames(){ mediaFrames.clear();}
}
