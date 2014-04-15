package dk.aau.cs.giraf.tortoise.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.controller.SerializableSequence;

public class LifeStory {
	
	private Sequence currentStory;
	private List<SerializableSequence> stories, templates;
	private static LifeStory instance;
	private int currentIndex;
	private Profile guardian;
	private Profile child;

    // Singleton pattern
	public static LifeStory getInstance() {
		if(instance == null)
			instance = new LifeStory(0);
		return instance;
	}
	
	private LifeStory(int index) {
		this.currentIndex = index;
		this.stories = new ArrayList<SerializableSequence>();
		this.templates = new ArrayList<SerializableSequence>();
	}
	
	public void addStory() {
		stories.add(currentStory.getSerializableSequence());
	}
	
	public void addTemplate() {
		templates.add(currentStory.getSerializableSequence());
	}
	
	public void removeTemplate(int index) {
		templates.remove(index);
	}
	
	public void setCurrentStory(Context context, int index) {
		this.currentIndex = index;
		currentStory = stories.get(index).getSequence(context);
	}
	
	public void setCurrentStory(Sequence s) {
		this.currentStory = s;
	}
	
	public void setCurrentTemplate(Context context, int index) {
		currentStory = templates.get(index).getSequence(context);
	}
	
	public void setNextStory(Context context) {
		currentIndex++;
		currentIndex = currentIndex % stories.size();
		setCurrentStory(context, currentIndex);
	}
	
	public void setPreviousStory(Context context) {
		currentIndex--;
		currentIndex = currentIndex < 0 ? stories.size() - 1 : currentIndex;
		setCurrentStory(context, currentIndex);
	}
	
	public List<SerializableSequence> getStories() {
		return this.stories;
	}
	
	public List<SerializableSequence> getTemplates() {
		return this.templates;
	}
	
	public Sequence getCurrentStory() {
		return this.currentStory;
	}
	
	public void setTemplates(List<SerializableSequence> templates) {
		this.templates.clear();
		this.templates.addAll(templates);
	}
	
	public void setStories(List<SerializableSequence> stories) {
		this.stories.clear();
		this.stories.addAll(stories);
	}

	public Profile getGuardian() {
		return guardian;
	}

	public void setGuardian(Profile guardian) {
		this.guardian = guardian;
	}

	public Profile getChild() {
		return child;
	}

	public void setChild(Profile child) {
		this.child = child;
	}

}
