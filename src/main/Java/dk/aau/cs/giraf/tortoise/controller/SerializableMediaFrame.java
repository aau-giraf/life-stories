package dk.aau.cs.giraf.tortoise.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import dk.aau.cs.giraf.pictogram.Pictogram;

public class SerializableMediaFrame extends AbstractMediaFrame implements Serializable {
	
	private List<Long> content;

	public SerializableMediaFrame() {
		super();
		setContent(new ArrayList<Long>());
	}
	
	public SerializableMediaFrame(MediaFrame m) {
		setContent(new ArrayList<Long>());
		for (Pictogram p : m.getContent()) {
			this.addContent(p.getPictogramID());
		}
		this.choiceNumber = m.choiceNumber;
		this.frames = m.frames;
	}

	public List<Long> getContent() {
		return content;
	}

	public void setContent(List<Long> content) {
		this.content = content;
	}
	
	public void addContent(Long content) {
		this.content.add(content);
	}
	
	public MediaFrame getMediaFrame(Context context) {
		return new MediaFrame(context, this);
	}
}
