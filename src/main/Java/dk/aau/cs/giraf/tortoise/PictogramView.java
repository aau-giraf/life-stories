package dk.aau.cs.giraf.tortoise;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Pictogram;


/**
 * Contains a pictogram and a title.
 * The view will display a delete button in the corner when the application is in editmode.
 * It also adds support for highlighting.
 */
public class PictogramView extends LinearLayout {

    public final static float NORMAL_SCALE = 0.8f;
    public final static float HIGHLIGHT_SCALE = 0.9f;
    public final static float LOWLIGHT_SCALE = 0.7f;
    public final static float MAIN_NORMAL_SCALE = 0.8f;
    public final static float MAIN_HIGHLIGHT_SCALE = 0.9f;
    public final static float MAIN_LOWLIGHT_SCALE = 0.7f;
	private final static float DEFAULT_TEXT_SIZE = 20f;

    private Helper helper;

	private RoundedImageView pictogram;
	private TextView title;
	private ImageView editEmblem;
    private ImageView deleteEmblem;
	
	private boolean isInEditMode = false;
	
	private OnDeleteClickListener onDeleteClickListener;
	
	public PictogramView(Context context) {
		super(context);
		
		initialize(context, 0, true);
	}

	public PictogramView(Context context, float radius, boolean inMain) {
		super(context);
		
		initialize(context, radius, inMain );
	}
	
	
	private void initialize(Context context, float radius, boolean inMain) {
		// Disable hardware accelleration to improve performance
		this.setLayerType(LAYER_TYPE_SOFTWARE, null);

        
		this.setOrientation(LinearLayout.VERTICAL);

		
		SquaredRelativeLayout square = new SquaredRelativeLayout(context);
		square.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        if(inMain) {
            square.addView(createImageViewForMain(radius));
            square.addView(createEmblemsForMain());

            //setupOnDeleteClickHandler();

            this.addView(square);

            this.addView(createTextView());
        }
        else{
            square.addView(createImageView(radius));
            for(View v : createEmblems()){
                square.addView(v);
            }

            //setupOnDeleteClickHandler();

            this.addView(square);

            this.addView(createTextView());
        }
	}
	
	private View createImageView(float radius) {
		pictogram = new RoundedImageView(getContext(), radius);
		pictogram.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		pictogram.setScaleX(NORMAL_SCALE);
		pictogram.setScaleY(NORMAL_SCALE);
		return pictogram;
	}

    private View createImageViewForMain(float radius) {
        pictogram = new RoundedImageView(getContext(), radius);
        pictogram.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        pictogram.setScaleX(MAIN_NORMAL_SCALE);
        pictogram.setScaleY(MAIN_NORMAL_SCALE);
        return pictogram;
    }
	
	private View createTextView() {
		title = new TextView(getContext());
		title.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		title.setGravity(Gravity.CENTER_VERTICAL);
		title.setTextSize(DEFAULT_TEXT_SIZE);
		
		return title;
	}
	
	private View createEmblemsForMain() {
		editEmblem = new ImageView(getContext());
		editEmblem.setImageResource(R.drawable.icon_edit);
        /*For when the other delete mode is implemented */
        //editEmblem.setImageResource(R.drawable.icon_edit_small);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		editEmblem.setLayoutParams(params);



		editEmblem.setPadding(4, 4, 4, 4);
		editEmblem.setBackgroundColor(Color.TRANSPARENT);

		editEmblem.setFocusable(false);

        setDeleteButtonVisible(false);

		return editEmblem;
	}
    private List<View> createEmblems() {
        editEmblem = new ImageView(getContext());
        deleteEmblem = new ImageView(getContext());

        editEmblem.setImageResource(R.drawable.icon_edit);
        deleteEmblem.setImageResource(R.drawable.btn_delete);

        editEmblem.setScaleX(0.7f);
        editEmblem.setScaleY(0.7f);
        deleteEmblem.setScaleX(0.7f);
        deleteEmblem.setScaleY(0.7f);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editEmblem.setLayoutParams(params);
        deleteEmblem.setLayoutParams(params);

        editEmblem.setY(12);
        editEmblem.setX(12);
        deleteEmblem.setY(12);
        deleteEmblem.setX(12);

        editEmblem.setPadding(4, 4, 4, 4);
        editEmblem.setBackgroundColor(Color.TRANSPARENT);
        deleteEmblem.setPadding(4, 4, 4, 4);
        deleteEmblem.setBackgroundColor(Color.TRANSPARENT);

        editEmblem.setFocusable(false);
        deleteEmblem.setFocusable(false);

        setEmblemsVisible(false);

        ArrayList<View> vs = new ArrayList<View>();
        vs.add(editEmblem);
        vs.add(deleteEmblem);

        return vs;
    }

	public void setImage(Bitmap bitmap) {
        Bitmap imageWithBG = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());  // Create another image the same size
        imageWithBG.eraseColor(Color.WHITE);  // set its background to white, or whatever color you want
        Drawable[] dList = new Drawable[2];
        Drawable d = new BitmapDrawable(getResources(), imageWithBG);
        Drawable d2 = new BitmapDrawable(getResources(), bitmap);
        dList[0] = d;
        dList[1] = d2;
        LayerDrawable layers = new LayerDrawable(dList);

        int width = layers.getIntrinsicWidth();
        int height = layers.getIntrinsicHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        layers.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        layers.draw(canvas);

        pictogram.setImageBitmap(newBitmap);
	}

    public void liftUp() {
        pictogram.setScaleX(HIGHLIGHT_SCALE);
        pictogram.setScaleY(HIGHLIGHT_SCALE);
        this.setAlpha(0.7f);
        setDeleteButtonVisible(false);
        invalidate();
    }

    public void placeDown() {
        pictogram.setScaleX(NORMAL_SCALE);
        pictogram.setScaleY(NORMAL_SCALE);
        this.setAlpha(1.0f);
        setDeleteButtonVisible(isInEditMode);
        invalidate();
    }

    public void setLowlighted() {
        pictogram.setScaleX(LOWLIGHT_SCALE);
        pictogram.setScaleY(LOWLIGHT_SCALE);
        this.setAlpha(0.4f);

        this.invalidate();
    }

    public void setSelected() {
        pictogram.setScaleX(HIGHLIGHT_SCALE);
        pictogram.setScaleY(HIGHLIGHT_SCALE);
        this.setAlpha(1f);

        this.invalidate();
    }

    public void setEmblemsVisible(boolean visible) {
        deleteEmblem.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
        editEmblem.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        invalidate();
    }

    private void setDeleteButtonVisible(boolean visible) {
        if(deleteEmblem != null){
            deleteEmblem.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
        }
        editEmblem.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        invalidate();
    }

    public boolean getEditModeEnabled() {
        return isInEditMode;
    }

    public void setEditModeEnabledForMain(boolean editMode) {
        if (editMode != isInEditMode) {
            isInEditMode = editMode;
            setDeleteButtonVisible(editMode);
        }
    }

    public void setEditModeEnabled(boolean editMode) {
        if (editMode != isInEditMode) {
            isInEditMode = editMode;
            setEmblemsVisible(editMode);
        }
    }

    public void setImageFromId(long id) {
        helper = new Helper(getContext());
        Pictogram pictogram1 = helper.pictogramHelper.getById(id);
        Bitmap bitmap = pictogram1.getImage();
        Bitmap imageWithBG = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());  // Create another image the same size
        imageWithBG.eraseColor(Color.WHITE);  // set its background to white, or whatever color you want
        Drawable[] dList = new Drawable[2];
        Drawable d = new BitmapDrawable(getResources(), imageWithBG);
        Drawable d2 = new BitmapDrawable(getResources(), bitmap);
        dList[0] = d;
        dList[1] = d2;
        LayerDrawable layers = new LayerDrawable(dList);

        int width = layers.getIntrinsicWidth();
        int height = layers.getIntrinsicHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        layers.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        layers.draw(canvas);

        pictogram.setImageBitmap(newBitmap);
    }

    public void setTitle(String newTitle)
    {
        title.setText(newTitle);
    }

    public void setupOnDeleteClickHandler() {
        editEmblem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditMode && onDeleteClickListener != null)
                    onDeleteClickListener.onDeleteClick();
            }
        });
    }
	public void setOnDeleteClickListener(OnDeleteClickListener listener) {
		onDeleteClickListener = listener;
	}
	
	public OnDeleteClickListener getOnDeleteClickListener() {
		return onDeleteClickListener;
	}
	
	public interface OnDeleteClickListener {
		public void onDeleteClick();
	}
}
