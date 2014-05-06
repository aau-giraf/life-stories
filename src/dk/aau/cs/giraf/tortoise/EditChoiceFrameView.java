package dk.aau.cs.giraf.tortoise;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.activities.EditModeActivity;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import android.graphics.Bitmap;

public class EditChoiceFrameView extends RelativeLayout implements OnClickListener{
	
	float scale;
	FrameLayout innerLayout;
	ImageView btn;
	private MediaFrame mediaFrame;
    Pictogram pictogram;
	EditModeActivity mainActivity;
    int position;
	
	public EditChoiceFrameView(EditModeActivity mainActivity, MediaFrame mediaFrame, Pictogram pictogram, LinearLayout.LayoutParams params) {
		super(mainActivity.getApplicationContext());
		this.mainActivity = mainActivity;
		this.setMediaFrame(mediaFrame);
        this.pictogram = pictogram;
		this.scale = mainActivity.getApplicationContext().getResources().getDisplayMetrics().density;
		this.setLayoutParams(params);
		this.btn = null;
		innerLayout = new FrameLayout(mainActivity.getApplicationContext());
		RelativeLayout.LayoutParams innerParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		innerParams.setMargins(2, 2, 2, 2);
        //innerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		innerLayout.setLayoutParams(innerParams);
		innerLayout.setBackgroundResource(R.layout.border);
		innerLayout.setPadding((int)(15*scale), (int) (15*scale), (int) (15*scale), (int) (15*scale));

        Drawable picToDrawable = new BitmapDrawable(getResources(), pictogram.getImageData());
		innerLayout.setBackgroundDrawable(picToDrawable);
		this.addView(innerLayout);
	}
	
	public void addDeleteButton(int position) {
		if(btn == null) {
            this.position = position;
			btn = new ImageView(mainActivity.getApplicationContext());
			btn.setImageResource(R.drawable.btn_delete);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			btn.setLayoutParams(params);
			btn.setPadding(0, 0, 0, 0);
			btn.setBackgroundColor(Color.TRANSPARENT);
			btn.setOnClickListener(this);
			this.addView(btn);
		}
		else
			throw new IllegalStateException("Delete button allready added.");
	}

	@Override
	public void onClick(View v) {
/*		if(this.getMediaFrame().getFrames().size() > 1
			&& this.getMediaFrame().getContent().size() == 2) {
			Toast t = Toast.makeText(mainActivity, "Piktogram kan ikke fjernes.", Toast.LENGTH_LONG);
			t.show();
		}
		else {
			this.getMediaFrame().removeContent(this.pictogram);
		}*/


        this.getMediaFrame().removeContent(this.pictogram);

        mainActivity.dismissAddContentDialog(v);
        mainActivity.renderAddContentMenu(position);

	}

	public MediaFrame getMediaFrame() {
		return mediaFrame;
	}

	public void setMediaFrame(MediaFrame mediaFrame) {
		this.mediaFrame = mediaFrame;
	}
			
		

}
