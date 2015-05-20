package dk.aau.cs.giraf.lifestory.activities;

import java.util.List;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.lifestory.ChoiceFrameView;
import dk.aau.cs.giraf.lifestory.Frame;
import dk.aau.cs.giraf.lifestory.helpers.LifeStory;
import dk.aau.cs.giraf.lifestory.controller.MediaFrame;
import dk.aau.cs.giraf.lifestory.R;

public class ViewModeActivity extends TortoiseActivity {

	private final int DIALOG_EXIT = 1;
	
	ViewModeFrameView currentViewModeFrame;
	RelativeLayout menuBar;
	RelativeLayout mainLayout;
	LinearLayout choices;
	static boolean isLocked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		menuBar = (RelativeLayout) findViewById(R.id.menuBar);
		//mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
		renderViewMenu();
		choices = (LinearLayout)findViewById(R.id.choiceContent);
		int index = this.getIntent().getExtras().getInt("story");
		if(index >= 0) {
			LifeStory.getInstance().setCurrentStory(this, index);
			renderStory();
		}
		else {
			throw new IndexOutOfBoundsException("No story exist..!");
		}
		mainLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentViewModeFrame != null) {
					choices.removeAllViews();
				}
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		renderDialog(DIALOG_EXIT);
	}
	
	@Override
	protected void onStop() {
		finish();
		super.onStop();
	}
	
	public void renderViewMenu() {
		renderMenuBar(R.layout.view_menu);
		ImageButton leftArrow = (ImageButton)findViewById(R.id.previous);
		leftArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LifeStory.getInstance().setPreviousStory(ViewModeActivity.this.getApplicationContext());
				renderStory();
			}
		});
		ImageButton rightArrow = (ImageButton)findViewById(R.id.next);
		rightArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LifeStory.getInstance().setNextStory(ViewModeActivity.this.getApplicationContext());
				renderStory();
				
			}
		});
		ToggleButton lockScreen = (ToggleButton)findViewById(R.id.lockScreen);
		lockScreen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isLocked) {
					ViewModeActivity.this.choices.removeAllViews();
					lockLayout((LinearLayout) findViewById(R.id.parentLayout), false);
					isLocked = true;
				}
				else {
					ViewModeActivity.this.renderPictos();
					lockLayout((LinearLayout) findViewById(R.id.parentLayout), true);
					isLocked = false;
				}
			}
		});
		ImageButton exit = (ImageButton)findViewById(R.id.exitViewMode);
		exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				renderDialog(DIALOG_EXIT);
				
			}
		});
	}
	
	public void renderDialog(int dialogId) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		switch (dialogId) {
		case DIALOG_EXIT:
			dialog.setContentView(R.layout.dialog_custom);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			TextView exitTitle = (TextView)dialog.findViewById(R.id.titleTextView);
			exitTitle.setText(R.string.dialog_exit_title);
			Button exitYes = (Button)dialog.findViewById(R.id.btn_yes);
			exitYes.setText(R.string.yes);
			exitYes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					finish();
				}
			});
			Button exitNo = (Button)dialog.findViewById(R.id.btn_no);
			exitNo.setText(R.string.no);
			exitNo.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				dialog.dismiss();
					
				}
			});
			break;
		default:
			break;
		}
		dialog.show();
	}
	
	public void lockLayout(ViewGroup v, boolean lock) {
		for(int i = 0; i < v.getChildCount(); i++) {
			View view = v.getChildAt(i);
			if(view != findViewById(R.id.lockScreen)) {
				view.setEnabled(lock);
			}
			if(view instanceof ViewGroup) {
				lockLayout((ViewGroup)view, lock);
			}
		}
	}
	
	public void renderPictos() {
		choices.removeAllViews();
		choices.setOnDragListener(new OnDragListener() {
			
			@Override
			public boolean onDrag(View v, DragEvent event) {
				switch (event.getAction()) {
				case DragEvent.ACTION_DROP:
					Pictogram p = (Pictogram)event.getLocalState();
					movePictogram(p, p.getParent(), v);
					break;
				default:
					break;
				}
				return true;
			}
		});
		if(currentViewModeFrame != null) {
			for(Pictogram p : currentViewModeFrame.getMediaFrame().getContent()) {
				if(!(p.getParent() instanceof ViewModeFrameView)) {
					p.setOnTouchListener(new OnTouchListener() {
						
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if(v.getParent() instanceof ChoiceFrameView) {
								ClipData data = ClipData.newPlainText("", "");
								DragShadowBuilder shadowBuilder = new DragShadowBuilder((View) v.getParent());
								v.startDrag(data, shadowBuilder, v, 0);
							}
							else if (v.getParent() instanceof ViewModeFrameView) {
								currentViewModeFrame = (ViewModeFrameView) v.getParent();
								renderPictos();
								RelativeLayout menuBar = (RelativeLayout) findViewById(R.id.menuBar);
								float scale = getApplicationContext().getResources().getDisplayMetrics().density;
								int parentWidth = (int) (menuBar.getWidth() - (120 / scale));
								ViewModeFrameView source = (ViewModeFrameView)v.getParent();
								DragShadowBuilder shadowBuilder = new DragShadowBuilder(source);
								shadowBuilder.onProvideShadowMetrics(new Point(parentWidth, parentWidth), 
										new Point(parentWidth/2, parentWidth/2));
								ClipData data = ClipData.newPlainText("", "");
								v.startDrag(data, shadowBuilder, v, 0);
							}
							return false;
						}
					});
					p.setOnDragListener(new OnDragListener() {
						
						@Override
						public boolean onDrag(View v, DragEvent event) {
							switch (event.getAction()) {
							case DragEvent.ACTION_DRAG_STARTED:
								if(event.getLocalState() == v) {
									if(v.getParent() instanceof ChoiceFrameView) {
										((ChoiceFrameView)v.getParent()).setVisibility(View.GONE);
									}
									else if(v.getParent() instanceof ViewModeFrameView) {
										v.setVisibility(View.GONE);
									}
								}
								break;
							case DragEvent.ACTION_DROP:
								Pictogram p = (Pictogram)event.getLocalState();
								movePictogram(p, p.getParent(), v.getParent());
								break;
							case DragEvent.ACTION_DRAG_ENDED:
								if(event.getLocalState() == v) {
									if(v.getParent() instanceof ChoiceFrameView) {
										((ChoiceFrameView)v.getParent()).setVisibility(View.VISIBLE);
										v.setVisibility(View.VISIBLE);
									}
									else if(v.getParent() instanceof ViewModeFrameView) {
										v.setVisibility(View.VISIBLE);
									}
								}
							default:
								break;
							}
							return true;
						}
					});
					if(p.getParent() instanceof ChoiceFrameView) {
						((ChoiceFrameView)p.getParent()).removeView(p);
					}
					addPictogramToChoices(p);
				}	
			}
		}
	}
	
	public void addPictogramToChoices(Pictogram pictogram) {
		LinearLayout.LayoutParams params = getLinearLayoutParams();
		ChoiceFrameView child = new ChoiceFrameView(this, currentViewModeFrame.getMediaFrame(), pictogram, params);
		child.setLayoutParams(params);
		choices.addView(child);
	}

	public LinearLayout.LayoutParams getLinearLayoutParams() {
		RelativeLayout menuBar = (RelativeLayout) findViewById(R.id.menuBar);
		float scale = getApplicationContext().getResources().getDisplayMetrics().density;
		int parentWidth = (int) (menuBar.getWidth() - (120 / scale));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(parentWidth, parentWidth);
		params.topMargin = (int) (10 / scale);
		params.leftMargin = (int) (10 / scale);
		params.rightMargin = (int) (10 / scale);
		return params;
	}
	
	public void movePictogram(Pictogram p, Object source, Object target) {
		if(source != target) {
			if(source instanceof ViewModeFrameView) {
				ViewModeFrameView s = (ViewModeFrameView)source;
				if(target instanceof ViewModeFrameView) {
					ViewModeFrameView t = (ViewModeFrameView)target;
					if(isPictogramInList(p, t)) {
						if(t.getChildCount() == 1) {
							Pictogram pTarget = t.getPictogram();
							t.removePictogram();
							s.removeView(p);
							t.setPictogram(p);
							s.setPictogram(pTarget);
						}
						else {
							s.removePictogram();
							t.setPictogram(p);
						}
					}
				}
				else if (target instanceof ChoiceFrameView ||
							target instanceof LinearLayout) {
					if(isPictogramInList(p, currentViewModeFrame)) {
						s.removePictogram();
						addPictogramToChoices(p);
					}
				}
			}
			else if (source instanceof ChoiceFrameView) {
				ChoiceFrameView s = (ChoiceFrameView)source;
				if(target instanceof ViewModeFrameView) {
					ViewModeFrameView t = (ViewModeFrameView)target;
					if(isPictogramInList(p, t)) {
						if(t.getChildCount() == 1) {
							Pictogram pTarget = t.getPictogram();
							t.removePictogram();
							s.removeView(p);
							choices.removeView(s);
							t.setPictogram(p);
							addPictogramToChoices(pTarget);
							
						}
						else {
							s.removeView(p);
							choices.removeView(s);
							t.setPictogram(p);
						}
					}
				}
			}
		}
	}

    /**
     * Returns true if the provided Pictogram is in the provided ViewModeFrameView
     * @param p
     * @param e
     * @return
     */
	public boolean isPictogramInList(Pictogram p, ViewModeFrameView e) {
		boolean isTrue;
		int index = e.getMediaFrame().getContent().indexOf(p);
		if(index == -1) {
			isTrue = false;
		}
		else {
			Pictogram pictogram = e.getMediaFrame().getContent().get(index);
			if(p == pictogram) {
				isTrue = true;
			}
			else
				isTrue = false;
		}
		return isTrue;
	}
	
	public void renderStory() {
		RelativeLayout.LayoutParams params;
		choices.removeAllViews();
		mainLayout.removeAllViews();
		detatchAllPictos();
		
		for (MediaFrame m : LifeStory.getInstance().getCurrentStory().getMediaFrames()) {
			for(Pictogram p : m.getContent()) {
				p.renderAll();
			}		
			List<Frame> frames = m.getFrames();
			for(Frame f : frames) {
				ViewModeFrameView viewModeFrameView = 
						new ViewModeFrameView(this, getApplicationContext(), 
								mainLayout, m, f, f.getHeight(), f.getWidth());
				params = new RelativeLayout.LayoutParams(f.getWidth(),f.getHeight());
				params.leftMargin = f.getPosition().x;
				params.topMargin = f.getPosition().y;
				viewModeFrameView.setLayoutParams(params);
				if (m.getContent().size() == 1)
					viewModeFrameView.setPictogram(m.getContent().get(0));
				mainLayout.addView(viewModeFrameView);
			}
		}
	}
	
	public void renderMenuBar(int id) {
		LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout menu = (LinearLayout)inflater.inflate(id, null);
		if (menuBar.getChildCount() > 0) {
			menuBar.removeAllViews();
		}
		menuBar.addView(menu);
	}

	public void detatchAllPictos() {
		for(MediaFrame m : LifeStory.getInstance().getCurrentStory().getMediaFrames()) {
			for(Pictogram p : m.getContent()) {
				if (p.getParent() instanceof FrameLayout)
					((FrameLayout)p.getParent()).removeView(p);
				else if(p.getParent() instanceof ViewModeFrameView)
					((ViewModeFrameView)p.getParent()).removePictogram();
			}
		}
	}
}
