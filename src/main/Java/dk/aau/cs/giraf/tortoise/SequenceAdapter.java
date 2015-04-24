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
			view = new PictogramView(context, 15f, false);
		} else
			view = (PictogramView)convertView;

        if(mediaFrame.getMarked()){

            Resources r = context.getResources();

            Drawable[] dlayers = new Drawable[2];
            Pictogram picto = PictoFactory.getPictogram(context.getApplicationContext(), mediaFrame.getPictogramId());
            Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
            bitmap = LayoutTools.getSquareBitmap(bitmap);
            bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, context.getApplicationContext(), 20);
            dlayers[0] = new BitmapDrawable(context.getResources(),  bitmap);
            int xy = context.getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
            dlayers[1] = resizeDrawable(r.getDrawable(R.drawable.cancel_button), xy, xy);

            LayerDrawable layerDrawable = new LayerDrawable(dlayers);
            Drawable drawableMarkedIcon = (Drawable) layerDrawable;
            BitmapDrawable markedIcon = (BitmapDrawable) drawableMarkedIcon;
            view.setImage(markedIcon.getBitmap());
        }
        else {
            if (mediaFrame.getContent().size() == 1) {
                view.setImageFromId(mediaFrame.getPictogramId());
            } else {
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.icon_choose);
                view.setImage(icon);
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

