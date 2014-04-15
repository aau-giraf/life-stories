package dk.aau.cs.giraf.tortoise.controller;


public abstract class AbstractSequence {

	protected int titlePictoId;
	protected String title;
	protected int numChoices;
	
	public AbstractSequence(){
		numChoices = 0;
		titlePictoId = 0;
		title = "";
	}
	
	public int getTitlePictoId() {
		return titlePictoId;
	}

	public void setTitlePictoId(int titlePictoId) {
		this.titlePictoId = titlePictoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String string) {
		this.title = string;
	}
	
	public int getNumChoices() {
		return this.numChoices;
	}
	
	public void setNumChoices(int numChoices) {
		this.numChoices = numChoices;
	}

}
