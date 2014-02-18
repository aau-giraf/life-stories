package dk.aau.cs.giraf.tortoise;


public abstract class AbstractSequence {

	protected long titlePictoId;
	protected String title;
	protected int numChoices;
	
	public AbstractSequence(){
		numChoices = 0;
		titlePictoId = 0;
		title = "";
	}
	
	public long getTitlePictoId() {
		return titlePictoId;
	}

	public void setTitlePictoId(long titlePictoId) {
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
