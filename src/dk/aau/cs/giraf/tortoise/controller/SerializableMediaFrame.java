package dk.aau.cs.giraf.tortoise.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import dk.aau.cs.giraf.pictogram.Pictogram;

public class SerializableMediaFrame extends AbstractMediaFrame {
	
	private List<Integer> content;

	public SerializableMediaFrame() {
		super();
		setContent(new ArrayList<Integer>());
	}
	
	public SerializableMediaFrame(MediaFrame m) {
		setContent(new ArrayList<Integer>());
		for (Pictogram p : m.getContent()) {
			this.addContent(p.getPictogramID());
		}
		this.choiceNumber = m.choiceNumber;
		this.frames = m.frames;
	}

	public List<Integer> getContent() {
		return content;
	}

	public void setContent(List<Integer> content) {
		this.content = content;
	}
	
	public void addContent(Integer content) {
		this.content.add(content);
	}
	
	public MediaFrame getMediaFrame(Context context) {
		return new MediaFrame(context, this);
	}
}
