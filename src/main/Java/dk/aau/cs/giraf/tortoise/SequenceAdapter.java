package dk.aau.cs.giraf.tortoise;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.activities.ScheduleEditActivity;
import dk.aau.cs.giraf.tortoise.activities.TortoiseActivity;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.List;

public class SequenceAdapter extends BaseAdapter {

    // Adapter for list of sequences in the context of the given child

	private Sequence sequence;
	private Context context;
    private boolean draggable = true;
    private boolean mode = false;
	
	private OnAdapterGetViewListener onAdapterGetViewListener;
    private SelectedFrameAware selectedFrameAware;

    public interface SelectedFrameAware {
        boolean isFrameMarked(MediaFrame frame);
    }

	public SequenceAdapter(Context context, Sequence sequence, SelectedFrameAware selectedFrameAware) {
        this.selectedFrameAware = selectedFrameAware;
		this.context = context;
		this.sequence = sequence;
	}

	@Override
	public int getCount() {
		if (sequence == null)
			return 0;
		else
			return sequence.getMediaFrames().size();
	}

	@Override
	public MediaFrame getItem(int position) {
		if (sequence == null) throw new IllegalStateException("No Sequence has been set for this Adapter");
		
		if (position >= 0 && position < sequence.getMediaFrames().size())
			return sequence.getMediaFrames().get(position);
		else
			return null;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		PictogramView view;
		MediaFrame mediaFrame = getItem(position);
		
		if (convertView == null) {
			view = new PictogramView(context, 15f, false);
		} else
			view = (PictogramView)convertView;


        if (mediaFrame.getContent().size() == 1) {
            view.setImageFromId(mediaFrame.getPictogramId());
        } else {
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.icon_choose);
            view.setImage(icon);
        }

        //view.setEmblemsVisible(mode);


        if (selectedFrameAware != null) {
            final boolean isFrameMarked = selectedFrameAware.isFrameMarked(mediaFrame);

            // Check if the view is selected
            //sequence.getId() == selectedSequencePictogramViewPair.sequence.getId()
            if (isFrameMarked) {
                // Set the background-color for the selected item
                view.setBackgroundColor(context.getResources().getColor(R.color.giraf_page_indicator_active));
            }
            else
            {
                view.setBackgroundDrawable(null);
            }
        }

        if (onAdapterGetViewListener != null)
            onAdapterGetViewListener.onAdapterGetView(position, view);

        return view;
	}

    public Drawable resizeDrawable(Drawable srcDrawable, int width, int height)
    {
        Bitmap b = ((BitmapDrawable) srcDrawable).getBitmap();
        Drawable finalDrawable = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(b, width, height, false));

        return finalDrawable;
    }

    public boolean getMode(){return mode;}

    public void setMode(boolean value){mode = value;}

    public boolean getDraggability(){return draggable;}

    public void setDraggability(boolean value){draggable = value;}
	
	public void setOnAdapterGetViewListener(OnAdapterGetViewListener onCreateViewListener) {
		this.onAdapterGetViewListener = onCreateViewListener;
	}
	
	public OnAdapterGetViewListener getOnAdapterGetViewListener() {
		return this.onAdapterGetViewListener;
	}
	
	public interface OnAdapterGetViewListener {
		public void onAdapterGetView(int position, View view);
	}

    public Sequence getSequence()
    {
        return sequence;
    }
}

