package dk.aau.cs.giraf.tortoise.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class SerializableSequence extends AbstractSequence implements Serializable{
	
	private List<SerializableMediaFrame> mediaFrames;
	
	public SerializableSequence() {
		super();
		setMediaFrames(new ArrayList<SerializableMediaFrame>());
	}
	
	public SerializableSequence(Sequence s) {
		setMediaFrames(new ArrayList<SerializableMediaFrame>());
		for(MediaFrame m : s.getMediaFrames()) {
			this.getMediaFrames().add(m.getSerializableMediaFrame());
		}
		this.numChoices = s.numChoices;
		this.title = s.title;
		this.titlePictoId = s.titlePictoId;
	}

	public List<SerializableMediaFrame> getMediaFrames() {
		return mediaFrames;
	}

	public void setMediaFrames(List<SerializableMediaFrame> mediaFrames) {
		this.mediaFrames = mediaFrames;
	}
	
	public Sequence getSequence(Context context) {
		return new Sequence(context, this);
	}
}
