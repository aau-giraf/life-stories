package dk.aau.cs.giraf.tortoise.controller;


public abstract class AbstractSequence {

    private long id;
	protected long titlePictoId;
	protected String title;
	protected int numChoices;
	
	public AbstractSequence(){
		numChoices = 0;
		titlePictoId = 0;
		title = "";
	}

    public AbstractSequence(long id, long titlePictoId, String title){
        this.id = id;
        this.titlePictoId = titlePictoId;
        this.title = title;
        numChoices = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
