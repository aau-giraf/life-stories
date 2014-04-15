package dk.aau.cs.giraf.tortoise;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.controller.SerializableSequence;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

/**
 *
 */
public class SequenceListAdapter extends BaseAdapter {

	private Context context;
	private boolean isInEditMode = false;
	private boolean isInTemplateMode = false;
	private List<SerializableSequence> items;
	private OnAdapterGetViewListener onAdapterGetViewListener;
	
	public SequenceListAdapter(Context context) {
		this.items = new ArrayList<SerializableSequence>();
		setItems();
		this.context = context;
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PictogramView v = (PictogramView) convertView;
		
		if (v == null) {
			v = new PictogramView(context, 16f);
		}
		
		SerializableSequence s = items.get(position);
        
        v.setTitle(s.getTitle());
        v.setEditModeEnabled(isInEditMode);
        
        Pictogram p = PictoFactory.getPictogram(context, s.getTitlePictoId());
        Bitmap bm = p.getImageData();
        v.setImage(LayoutTools.getSquareBitmap(bm));
        
        if (onAdapterGetViewListener != null)
			onAdapterGetViewListener.onAdapterGetView(position, v);

        return v;
    }

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public SerializableSequence getItem(int position) {
        return items.get(position);
    }
	
	@Override
	public long getItemId(int position) {
        return position;
    }
	
	public void setEditModeEnabled(boolean editEnabled) {
		if (isInEditMode != editEnabled) {
			isInEditMode = editEnabled;
		}
	}
	
	public void setTemplateModeEnabled(boolean templateEnabled) {
		if(isInTemplateMode != templateEnabled) {
			isInTemplateMode = templateEnabled;
		}
		setItems();
	}
	
	public void setItems() {
		items.clear();
		if(isInTemplateMode) {
			items.addAll(LifeStory.getInstance().getTemplates());
		}
		else {
			items.addAll(LifeStory.getInstance().getStories());
		}
	}
	
	public void setOnAdapterGetViewListener(OnAdapterGetViewListener onCreateViewListener) {
		this.onAdapterGetViewListener = onCreateViewListener;
	}
	
	public OnAdapterGetViewListener getOnAdapterGetViewListener() {
		return this.onAdapterGetViewListener;
	}
	
	public interface OnAdapterGetViewListener {
		public void onAdapterGetView(int position, View view);
	}
}
