package dk.aau.cs.giraf.tortoise;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import android.graphics.Bitmap;

public class SequenceAdapter extends BaseAdapter {

    // Adapter for list of sequences in the context of the given child

	private Sequence sequence;
	private Context context;
	
	private OnAdapterGetViewListener onAdapterGetViewListener;

	public SequenceAdapter(Context context, Sequence sequence) {
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
			view = new PictogramView(context, 24f);
		} else
			view = (PictogramView)convertView;

        view.setImageFromId(mediaFrame.getPictogramId());

        if (onAdapterGetViewListener != null)
            onAdapterGetViewListener.onAdapterGetView(position, view);

        return view;
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

