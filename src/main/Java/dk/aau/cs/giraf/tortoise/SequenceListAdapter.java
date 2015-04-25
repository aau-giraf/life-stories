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
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.controller.SerializableSequence;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

/**
 *
 */
public class SequenceListAdapter extends BaseAdapter {

	private Context context;
	private boolean isInEditMode = false;
	private boolean isInTemplateMode = false;
	private List<Sequence> items;
	private OnAdapterGetViewListener onAdapterGetViewListener;
    private SelectedSequenceAware selectedSequenceAware;

    public interface SelectedSequenceAware {
        boolean isSequenceMarked(dk.aau.cs.giraf.tortoise.controller.Sequence sequence);
    }

	public SequenceListAdapter(Context context, List<Sequence> items, SelectedSequenceAware selectedSequenceAware) {
        this.selectedSequenceAware = selectedSequenceAware;
	    this.items = items;
        //setItems(items);
		this.context = context;
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PictogramView v = (PictogramView) convertView;
		
		if (v == null) {
			v = new PictogramView(context, 16f, true);
		}
		
		Sequence s = items.get(position);
        
        v.setTitle(s.getTitle());
        v.setEditModeEnabled(isInEditMode);
        
        Pictogram p = PictoFactory.getPictogram(context, s.getTitlePictoId());
        Bitmap bm = p.getImageData();
        v.setImage(LayoutTools.getSquareBitmap(bm));

        if (selectedSequenceAware != null) {
            final boolean isSequenceMarked = selectedSequenceAware.isSequenceMarked(s);

            // Check if the view is selected
            //sequence.getId() == selectedSequencePictogramViewPair.sequence.getId()
            if (isSequenceMarked) {
                // Set the background-color for the selected item
                v.setBackgroundColor(context.getResources().getColor(R.color.giraf_page_indicator_active));
            }
            else
            {
                v.setBackgroundDrawable(null);
            }
        }


        if (onAdapterGetViewListener != null)
			onAdapterGetViewListener.onAdapterGetView(position, v);

        return v;
    }

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Sequence getItem(int position) {
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
		setItems(null);
	}
	
	public void setItems(List<Sequence> itemList) {
		items.clear();
		if(isInTemplateMode) {
			items.addAll(LifeStory.getInstance().getTemplates());
		}
		else {

            if(itemList != null) {
                items.addAll(itemList);
            }
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
