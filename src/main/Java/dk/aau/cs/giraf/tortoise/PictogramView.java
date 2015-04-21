package dk.aau.cs.giraf.tortoise;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dk.aau.cs.giraf.oasis.lib.Helper;


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
	private ImageButton deleteButton;
    private ImageButton editButton;
	
	private boolean isInEditMode = false;
    private boolean IsInDeleteMode = false;
	
	private OnDeleteClickListener onDeleteClickListener;
	
	public PictogramView(Context context) {
		super(context);
		
		initialize(context, 0, true);
	}

	public PictogramView(Context context, float radius, boolean inMain) {
		super(context);
		
		initialize(context, radius, inMain);
	}
	
	
	private void initialize(Context context, float radius, boolean inMain) {
		// Disable hardware accelleration to improve performance
		this.setLayerType(LAYER_TYPE_SOFTWARE, null);

        
		this.setOrientation(LinearLayout.VERTICAL);

		
		SquaredRelativeLayout square = new SquaredRelativeLayout(context);
		square.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        if(inMain) {
            square.addView(createImageViewForMain(radius));
            square.addView(createDeleteButtonForMain());
            square.addView(createEditButton());

            setupOnDeleteClickHandler();

            this.addView(square);

            this.addView(createTextView());
        }
        else{
            square.addView(createImageView(radius));
            square.addView(createDeleteButton());
            square.addView(createEditButton());

            setupOnDeleteClickHandler();

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
	
	private View createDeleteButtonForMain() {
		deleteButton = new ImageButton(getContext());
		deleteButton.setImageResource(R.drawable.btn_delete);
        /*For when the other delete mode is implemented */
        //deleteButton.setImageResource(R.drawable.icon_edit_small);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		deleteButton.setLayoutParams(params);



		deleteButton.setPadding(4, 4, 4, 4);
		deleteButton.setBackgroundColor(Color.TRANSPARENT);

		deleteButton.setFocusable(false);

        setDeleteButtonVisible(false);

		return deleteButton;
	}
    private View createDeleteButton() {
        deleteButton = new ImageButton(getContext());
        deleteButton.setImageResource(R.drawable.btn_delete);
        /*For when the other delete mode is implemented */
        //deleteButton.setImageResource(R.drawable.icon_edit_small);
        deleteButton.setScaleX(0.7f);
        deleteButton.setScaleY(0.7f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        deleteButton.setLayoutParams(params);

        deleteButton.setY(12);
        deleteButton.setX(12);

        deleteButton.setPadding(4, 4, 4, 4);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);

        deleteButton.setFocusable(false);

        setDeleteButtonVisible(false);

        return deleteButton;
    }

    private View createEditButton() {
        editButton = new ImageButton(getContext());
        editButton.setImageResource(R.drawable.icon_edit_small);


        editButton.setScaleY(0.7f);
        editButton.setScaleX(0.7f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        editButton.setLayoutParams(params);

        editButton.setX(12);
        editButton.setY(12);

        editButton.setPadding(4,4,4,4);
        editButton.setBackgroundColor(Color.TRANSPARENT);

        editButton.setFocusable(false);

        setEditButtonVisible(false);

        return editButton;


    }

	public void setImage(Bitmap bitmap) {
		pictogram.setImageBitmap(bitmap);
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


    private void setDeleteButtonVisible(boolean deleteVisible) {
        deleteButton.setVisibility(deleteVisible ? View.VISIBLE : View.INVISIBLE);
        invalidate();
    }

    private void setEditButtonVisible(boolean editVisible)
    {
        editButton.setVisibility(editVisible ? View.VISIBLE : View. INVISIBLE);
        invalidate();
    }

    public boolean getEditModeEnabled() {
        return isInEditMode;
    }

    public void setEditModeEnabled(boolean editMode, boolean deleteMode) {
        if (editMode != isInEditMode) {
            isInEditMode = editMode;
            setEditButtonVisible(editMode);
        }
        else if (deleteMode != IsInDeleteMode) {
            IsInDeleteMode = deleteMode;
            setDeleteButtonVisible(deleteMode);
        }

    }

    public void setImageFromId(int id) {
        helper = new Helper(getContext());
        pictogram.setImageBitmap(helper.pictogramHelper.getPictogramById(id).getImage());
    }

    public void setTitle(String newTitle)
    {
        title.setText(newTitle);
    }

    public void setupOnDeleteClickHandler() {
        deleteButton.setOnClickListener(new OnClickListener() {
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
