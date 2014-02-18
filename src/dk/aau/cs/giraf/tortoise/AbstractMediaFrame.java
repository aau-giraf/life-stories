package dk.aau.cs.giraf.tortoise;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMediaFrame {
	
	protected List<Frame> frames;
	protected int choiceNumber;
	
	public AbstractMediaFrame(){
		setFrames(new ArrayList<Frame>());
		setChoiceNumber(0);
	}
	
	public void addFrame(Frame frame) {
		this.frames.add(frame);
	}
	
	public boolean removeFrame(Frame frame) {
		return this.frames.remove(frame);
	}

	public List<Frame> getFrames() {
		return frames;
	}

	public void setFrames(List<Frame> frames) {
		this.frames = frames;
	}

	public int getChoiceNumber() {
		return choiceNumber;
	}

	public void setChoiceNumber(int choiceNumber) {
		this.choiceNumber = choiceNumber;
	}
}
