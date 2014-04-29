package dk.aau.cs.giraf.tortoise.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.EditChoiceFrameView;
import dk.aau.cs.giraf.tortoise.Frame;
import dk.aau.cs.giraf.tortoise.FrameDragShadowBuilder;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.controller.JSONSerializer;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame.OnContentChangedEventListener;
import dk.aau.cs.giraf.tortoise.interfaces.OnCurrentFrameEventListener;
import dk.aau.cs.giraf.tortoise.interfaces.OnMainLayoutEventListener;
import dk.aau.cs.giraf.tortoise.interfaces.OnMediaFrameEventListener;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.Sequence;

public class EditModeActivity extends TortoiseActivity implements OnCurrentFrameEventListener {


	private static final int DIALOG_SAVE = 1;
	private static final int DIALOG_EXIT = 2;
	private static final int DIALOG_PROMT_FOR_TITLE = 3;
	private static final int DIALOG_SELECT_CHOICE = 4;
	
	static int selectedChoice = 0;
    private boolean dialogAddFramesActive;
	
    EditModeFrameView currentEditModeFrame;
	RelativeLayout menuBar;
	RelativeLayout mainLayout;
    GDialog dialogAddFrames;


	public List<OnMainLayoutEventListener> mainLayoutListeners =
			new ArrayList<OnMainLayoutEventListener>();
	public List<OnCurrentFrameEventListener> currentFrameListeners = 
			new ArrayList<OnCurrentFrameEventListener>();
	public List<OnMediaFrameEventListener> mediaFrameListeners =
			new ArrayList<OnMediaFrameEventListener>();
	
	public void addOnMainLayoutEventListener(OnMainLayoutEventListener mainLayoutListener) {
		this.mainLayoutListeners.add(mainLayoutListener);
	}
	
	public void addOnCurrentFrameEventListener(OnCurrentFrameEventListener currentFrameListener) {
		this.currentFrameListeners.add(currentFrameListener);
	}
	
	public void removeOnCurrentFrameEventListener(OnCurrentFrameEventListener currentFrameListener) {
		this.currentFrameListeners.remove(currentFrameListener);
	}
	
	public void addOnMediaFrameChangedListener(OnMediaFrameEventListener mediaFrameListener) {
		this.mediaFrameListeners.add(mediaFrameListener);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		menuBar = (RelativeLayout) findViewById(R.id.menuBar);
		mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
		
		int template = this.getIntent().getExtras().getInt("template");
		if(template == -1) {
			LifeStory.getInstance().setCurrentStory(new Sequence());
		}
		else {
			LifeStory.getInstance().setCurrentTemplate(EditModeActivity.this.getApplicationContext(), template); // TODO: kan EditModeActivity. slettes?!?
			renderTemplate();
		}
		
		mainLayout.setOnDragListener(new OnDragListener() {
			
			@Override
			public boolean onDrag(View v, DragEvent e) {
				return mainLayoutDrag(v, e);
			}
		});
		mainLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for(OnMainLayoutEventListener e : mainLayoutListeners) {
					e.OnMainLayoutTouchListener();
				}
				if(menuBar.getChildAt(0).getId() == R.id.choiceMenu) {
					EditModeActivity.this.renderEditMenu();
				}
				else {
					EditText storyName = (EditText) findViewById(R.id.storyName);
					storyName.clearFocus();
				}
			}
		});
		renderEditMenu();
	}

	@Override 
	public void onBackPressed() {
		renderDialog(DIALOG_EXIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK && requestCode == 1) {
			int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");
			
			if (checkoutIds.length == 0) {
				Toast t = Toast.makeText(EditModeActivity.this, "Ingen pictogrammer valgt.", Toast.LENGTH_LONG);
				t.show();
			}
			else
			{
                List<Integer> pictoIDList = new ArrayList<Integer>();

                // get the pictograms that are currently being shown
                List<Pictogram> pictoList = currentEditModeFrame.getMediaFrame().getContent();

                // put all their IDs in a list
                for(Pictogram p : pictoList)
                {
                    pictoIDList.add(p.getPictogramID());
                }

				for (int i = 0; i < checkoutIds.length; i++)
                {
					Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[i]);
					picto.renderAll();

                    boolean shouldAddToList = true;

                    // if pictogram already exists, don't add it. We don't want duplicates
                    for (Integer element : pictoIDList)
                    {
                        if(element == picto.getPictogramID())
                        {
                            shouldAddToList = false;
                        }
                    }

                    if(shouldAddToList)
                    {
                        // add pictogram
                        pictoIDList.add(picto.getPictogramID());
                        currentEditModeFrame.getMediaFrame().addContent(picto);
                    }
				}
                renderPictograms();
			}
		}
		else if (resultCode == RESULT_OK && requestCode == 2) {
          try{
			int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
			if (checkoutIds.length == 0) {
				Toast t = Toast.makeText(EditModeActivity.this, "Ingen pictogrammer valgt.", Toast.LENGTH_LONG);
				t.show();
			}
			else
			{
                try{
                    LifeStory.getInstance().getCurrentStory().setTitlePictoId(checkoutIds[0]);
                    Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                    Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
                    bitmap = LayoutTools.getSquareBitmap(bitmap);
                    bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 20);
                    LifeStory.getInstance().getCurrentStory().setTitleImage(bitmap);
                    ImageView storyImage = (ImageView) findViewById(R.id.storyImage);
                    storyImage.setImageBitmap(bitmap);
			    }
                //We expect a null pointer exception if the pictogram is without image
                //TODO: Investigate if this still happens with the new DB.
                // It still does
                catch (NullPointerException e){
                    Toast t = Toast.makeText(EditModeActivity.this, "Der skete en uventet fejl.", Toast.LENGTH_SHORT);
                    t.show();

                }
            }
          } catch (Exception e){
            GuiHelper.ShowToast(this, e.toString());
          }
		}
	}

    // placed here so it can be accessed from all places in switch case
    GDialog gdialog;

	public void renderDialog(int dialogId) {
        // TODO: Start herfra mandag d. 24
        boolean showOverlay = true; // TODO: remove this and replace all dialogs with Gdialogs!
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		switch (dialogId)
        {
		case DIALOG_SAVE:
			dialog.setContentView(R.layout.dialog_save);
			CheckBox template = (CheckBox)dialog.findViewById(R.id.templateCheckbox);
			template.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Button saveYes = (Button)dialog.findViewById(R.id.btn_yes);
					if(isChecked) {
						saveYes.setAlpha(1f);
						saveYes.setEnabled(true);
					}
					else {
						saveYes.setAlpha(0.3f);
						saveYes.setEnabled(false);
					}
					
				}
			});
			CheckBox story = (CheckBox)dialog.findViewById(R.id.storyCheckbox);
			story.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Button saveYes = (Button)dialog.findViewById(R.id.btn_yes);
					if(isChecked) {
						saveYes.setAlpha(1f);
						saveYes.setEnabled(true);
					}
					else {
						saveYes.setAlpha(0.3f);
						saveYes.setEnabled(false);
					}
					
				}
			});
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			Button saveYes = (Button)dialog.findViewById(R.id.btn_yes);
			saveYes.setAlpha(0.3f);
			saveYes.setEnabled(false);
			saveYes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					JSONSerializer js = new JSONSerializer();
					CheckBox template = (CheckBox)dialog.findViewById(R.id.templateCheckbox);
					CheckBox story = (CheckBox)dialog.findViewById(R.id.storyCheckbox);
					if(template.isChecked()) {
						LifeStory.getInstance().addTemplate();
						try {
							js.saveSettingsToFile(getApplicationContext(), 
									LifeStory.getInstance().getTemplates(), LifeStory.getInstance().getGuardian().getId());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(story.isChecked()) {
						LifeStory.getInstance().addStory();
						try {
							js.saveSettingsToFile(getApplicationContext(),
									LifeStory.getInstance().getStories(), LifeStory.getInstance().getChild().getId());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					finish();
				}
			});
			Button saveNo = (Button)dialog.findViewById(R.id.btn_no);
			saveNo.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					
				}
			});
			break;
		case DIALOG_EXIT:
			/*dialog.setContentView(R.layout.dialog_custom);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			TextView exitTitle = (TextView)dialog.findViewById(R.id.titleTextView);
			exitTitle.setText(R.string.dialog_exit_title);
			TextView exitMessage = (TextView)dialog.findViewById(R.id.messageTextView);
			exitMessage.setText(R.string.dialog_exit_message);
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
			});*/

            gdialog = new GDialogMessage(this,
                    R.drawable.ic_launcher,
                    getString(R.string.dialog_exit_title),
                    getResources().getString(R.string.dialog_exit_message),
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                            finish();
                        }
                    });
            gdialog.show();
            showOverlay = false;
			break;
		case DIALOG_PROMT_FOR_TITLE:
            /*
			dialog.setContentView(R.layout.dialog_custom);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			TextView promtTitle = (TextView)dialog.findViewById(R.id.titleTextView);
			promtTitle.setText(R.string.dialog_promt_for_title_title);
			TextView promtMessage = (TextView)dialog.findViewById(R.id.messageTextView);
			promtMessage.setText(R.string.dialog_promt_for_title_message);
			Button promtNo = (Button)dialog.findViewById(R.id.btn_no);
			promtNo.setVisibility(View.GONE);
			Button promtYes = (Button)dialog.findViewById(R.id.btn_yes);
			promtYes.setText(R.string.ok);
			promtYes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});*/

            GDialog gdialog;

            gdialog = new GDialogMessage(this,
                    R.drawable.ic_launcher,
                    getString(R.string.dialog_promt_for_title_title),
                    getResources().getString(R.string.dialog_promt_for_title_message),
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                        }
                    });
            gdialog.show();
			break;
		case DIALOG_SELECT_CHOICE:
			final int numChoices = LifeStory.getInstance().getCurrentStory().getNumChoices();
			dialog.setContentView(R.layout.dialog_select_choice);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			LinearLayout choiceContent = (LinearLayout)dialog.findViewById(R.id.choiceContent);
			RadioGroup choiceGroup = new RadioGroup(this);
			choiceGroup.setOrientation(LinearLayout.VERTICAL);
			for (int i=0; i < numChoices ; i++) {
				RadioButton rb = new RadioButton(this);
				rb.setId(i+1);
				rb.setTextColor(getResources().getColor(R.color.text_color));
				rb.setTextSize(22);
				CharSequence label = "  " + getResources().getString(R.string.choice)+ " " + (i+1);
				rb.setText(label);
				rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked) {
							selectedChoice = buttonView.getId();
						}
					}
				});
				choiceGroup.addView(rb);
			}
			choiceContent.addView(choiceGroup);
			Button choiceYes = (Button)dialog.findViewById(R.id.btn_yes);
			choiceYes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					if(selectedChoice != 0) {
						MediaFrame mediaFrame = currentEditModeFrame.getMediaFrame();
						for (MediaFrame m : LifeStory.getInstance().getCurrentStory().getMediaFrames()) {
							if (m.getChoiceNumber() == selectedChoice) {
								currentEditModeFrame.setMediaFrame(m);
								m.addFrame(currentEditModeFrame.getFrame());
							}
						}
						selectedChoice = 0;
						LifeStory.getInstance().getCurrentStory().getMediaFrames().remove(mediaFrame);
//						renderPictograms((LinearLayout)dialogAddFrames.findViewById(R.id.newChoiceContent2));
					}
				}
			});
			Button choiceNo = (Button)dialog.findViewById(R.id.btn_no);
			choiceNo.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			break;
		default:
			break;
		}

        if(showOverlay)
        {
            dialog.show();
        }
	}
	
	public void updateMediaFrameChoice(MediaFrame mediaFrame, boolean isChoice) {
		if (isChoice) {
			mediaFrame.setChoiceNumber(LifeStory.getInstance().getCurrentStory().getNumChoices() + 1);
			LifeStory.getInstance().getCurrentStory().incrementNumChoices();
		}
		else if (!isChoice) {
			mediaFrame.setChoiceNumber(0);
			LifeStory.getInstance().getCurrentStory().decrementNumChoices();
		}
		renderPictograms();
	}

    /**
     * Updates the views to show associated pictograms. This will update the choice dialog and the main view.
     *
     */
	public void renderPictograms() {
        LinearLayout newChoiceContent = (LinearLayout) dialogAddFrames.findViewById(R.id.newChoiceContent2);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(145, 145);
        List<Pictogram> pictograms = currentEditModeFrame.getMediaFrame().getContent();

		if(pictograms.size() == 0)
        {
			newChoiceContent.removeAllViews();
			currentEditModeFrame.detachPictograms();
		}
		else
        {
			newChoiceContent.removeAllViews();
			currentEditModeFrame.detachPictograms();

            //TODO: THIS IS A WORKAROUND!!
            // If doalog is shown - put pictograms on it.
            if(dialogAddFramesActive)
            {
                for(Pictogram p : currentEditModeFrame.getMediaFrame().getContent()) {
                    EditChoiceFrameView choiceFramView = new EditChoiceFrameView(this, currentEditModeFrame.getMediaFrame(), p, params);
                    choiceFramView.addDeleteButton();
                    newChoiceContent.addView(choiceFramView);
                }
            }
            // Otherwise show the pictogram on current frame.
            else
            {
                currentEditModeFrame.setPictogram(currentEditModeFrame.getMediaFrame().getContent().get(0));

            }





		}
	}



	public void renderAddContentMenu() {


        dialogAddFrames = new GDialog(this, LayoutInflater.from(this).inflate(R.layout.dialog_add_frames,null));

        dialogAddFramesActive = true;
		renderPictograms();
        dialogAddFrames.show();
	}

    /**
     * Loads / displays life story edit menu. It is here new life stories are edited.
     * The layout file related to this method is edit_menu.xml
     */
	public void renderEditMenu()
    {
		renderMenuBar(R.layout.edit_menu);
		ImageView storyImage = (ImageView) findViewById(R.id.storyImage);
		storyImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent i = new Intent();
				i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", 
					"dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
				i.putExtra("purpose", "single");
				i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
				i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
				EditModeActivity.this.startActivityForResult(i, 2);
			}
		});
		if(LifeStory.getInstance().getCurrentStory().getTitleImage() != null) {
			storyImage.setImageBitmap(LifeStory.getInstance().getCurrentStory().getTitleImage());
		}
		EditText storyName = (EditText) findViewById(R.id.storyName);
        storyName.setHighlightColor(00000000);


		storyName.setInputType(InputType.TYPE_NULL);
		if(LifeStory.getInstance().getCurrentStory().getTitle() != "") {
			storyName.setText(LifeStory.getInstance().getCurrentStory().getTitle());
		}
		storyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				    in.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.SHOW_FORCED);
				}
				else {
					((EditText) v).setInputType(InputType.TYPE_NULL);
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				    in.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});
		storyName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				LifeStory.getInstance().getCurrentStory().setTitle(s.toString());
			}
		});
		storyName.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					v.clearFocus();
				}
				return false;
			}
		});
		storyName.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((EditText) v).setInputType(InputType.TYPE_CLASS_TEXT);
				return false;
			}
		});
		ImageButton addSmallFrame = (ImageButton)findViewById(R.id.smallFrame);
		addSmallFrame.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				EditModeFrameView frame = LayoutTools.getEditModeFrameView(EditModeActivity.this, mainLayout, 192, 192);
				frame.getMediaFrame().setOnContentChangedListener(new OnContentChangedEventListener() {
					
					@Override
					public void OnIsChoiceListener(MediaFrame mediaFrame, boolean isChoice) {
						EditModeActivity.this.updateMediaFrameChoice(mediaFrame, isChoice);	
					}

					@Override
					public void OnContentSizeChanged(MediaFrame mediaFrame) {
						//TODO: Is this even used (will outcomment)
                        EditModeActivity.this.renderPictograms();
					}
				});
				ClipData data = ClipData.newPlainText("", "");
				FrameDragShadowBuilder shadowBuilder = new FrameDragShadowBuilder(frame);
				v.startDrag(data, shadowBuilder, frame, 0);
				return false;
			}
		});
		ImageButton addLargeFrame = (ImageButton)findViewById(R.id.largeFrame);
		addLargeFrame.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				EditModeFrameView frame = LayoutTools.getEditModeFrameView(EditModeActivity.this, mainLayout, 304, 304);
				frame.getMediaFrame().setOnContentChangedListener(new OnContentChangedEventListener() {
					
					@Override
					public void OnIsChoiceListener(MediaFrame mediaFrame, boolean isChoice) {
						EditModeActivity.this.updateMediaFrameChoice(mediaFrame, isChoice);
						
					}

					@Override
					public void OnContentSizeChanged(MediaFrame mediaFrame) {
						//TODO: Is this used?
                        EditModeActivity.this.renderPictograms();
						
					}
				});
				ClipData data = ClipData.newPlainText("", "");
				FrameDragShadowBuilder shadowBuilder = new FrameDragShadowBuilder(frame);
				v.startDrag(data, shadowBuilder, frame, 0);
				return false;
			}
		});
		ImageButton exit = (ImageButton)findViewById(R.id.exitEditMode);
		exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditModeActivity.this.renderDialog(DIALOG_EXIT);
			}
		});
		
		ImageButton save = (ImageButton)findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (LifeStory.getInstance().getCurrentStory().getTitlePictoId() == 0
						|| LifeStory.getInstance().getCurrentStory().getTitle().equals("")) {
					EditModeActivity.this.renderDialog(DIALOG_PROMT_FOR_TITLE);
				}
				else {
					renderDialog(DIALOG_SAVE);
				}
			}
		});
		ImageView bin = (ImageView)findViewById(R.id.bin);
		bin.setImageResource(R.drawable.bin_closed);
		bin.setOnDragListener(new OnDragListener() {
			
			@Override
			public boolean onDrag(View v, DragEvent event) {
				switch(event.getAction()) {
				case DragEvent.ACTION_DRAG_STARTED:
					if (event.getLocalState() instanceof EditModeFrameView)
						return true;
					else
						return false;
				case DragEvent.ACTION_DRAG_ENTERED:
					((ImageView) findViewById(R.id.bin))
					.setImageResource(R.drawable.bin_open);
					break;
				case DragEvent.ACTION_DRAG_EXITED:
					((ImageView) findViewById(R.id.bin))
					.setImageResource(R.drawable.bin_closed);
					break;
				case DragEvent.ACTION_DROP:
					EditModeFrameView frame = (EditModeFrameView) event.getLocalState();
					LayoutTools.removeEditModeFrameView(frame);
					EditModeActivity.this.currentEditModeFrame = null;
					((ImageView) findViewById(R.id.bin))
					.setImageResource(R.drawable.bin_closed);
					break;
				default:
					return false;
				}
				return true;
			}
		});
	}

    /**
     * Adds a linear layout View to menuBar
     * @param id
     */
	public void renderMenuBar(int id) {
		LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout menu = (RelativeLayout)inflater.inflate(id, null);
		if (menuBar.getChildCount() > 0) {
			menuBar.removeAllViews();
		}
		menuBar.addView(menu);
	}

    /**
     * Makes sure that the View v is positioned on the mainLayout where is is dropped
     * @param v
     * @param event
     * @return
     */
	public boolean mainLayoutDrag(View v, DragEvent event) {

        // Grim, grim GRIM switch!
		switch(event.getAction()) {
		case DragEvent.ACTION_DROP:
			EditModeFrameView view = (EditModeFrameView) event.getLocalState();
			LayoutTools.placeFrame(mainLayout, view, 
					(int) (event.getX() - (view.width / 2)), 
					(int) (event.getY() - (view.height / 2)));
			break;
		default:
			return true;
		}
		return true;
	}
	
	public void renderTemplate() {
		RelativeLayout.LayoutParams params;

        // For every MediaFrame in the current LifeStory...
		for (MediaFrame m : LifeStory.getInstance().getCurrentStory().getMediaFrames()) {
			m.setOnContentChangedListener(new OnContentChangedEventListener() {
				
				@Override
				public void OnIsChoiceListener(MediaFrame mediaFrame, boolean isChoice) {
                    EditModeActivity.this.updateMediaFrameChoice(mediaFrame, isChoice);
				}

				@Override
				public void OnContentSizeChanged(MediaFrame mediaFrame) {
					//TODO: Used?
					//EditModeActivity.this.renderPictograms((LinearLayout)dialogAddFrames.findViewById(R.id.newChoiceContent2));
				}
			});
			for(Pictogram p : m.getContent()) {
				p.renderAll();
			}		
			List<Frame> frames = m.getFrames();
			for(Frame f : frames) {
				EditModeFrameView editModeFrameView = 
						new EditModeFrameView(this, getApplicationContext(), 
								mainLayout, m, f, f.getHeight(), f.getWidth());
				params = new RelativeLayout.LayoutParams(f.getWidth(),f.getHeight());
				params.leftMargin = f.getPosition().x;
				params.topMargin = f.getPosition().y;
				editModeFrameView.setLayoutParams(params);
				if(m.getChoiceNumber() > 0) {
					editModeFrameView.addText("Valg " + m.getChoiceNumber());
				}
				if (m.getContent().size() == 1)
					editModeFrameView.setPictogram(m.getContent().get(0));
				Log.i("mainLayout", "Choice: " + m.getChoiceNumber());
				mainLayout.addView(editModeFrameView);
			}
		}
	}
	
	public void removeAllViews() {
		mainLayout.removeAllViews();
		menuBar.removeAllViews();
		for(MediaFrame mf : LifeStory.getInstance().getCurrentStory().getMediaFrames()) {
			for(Pictogram p : mf.getContent()) {
				if (p.getParent() instanceof FrameLayout)
					((FrameLayout)p.getParent()).removeView(p);
				else if(p.getParent() instanceof EditModeFrameView)
					((EditModeFrameView)p.getParent()).removeView(p);
			}
		}
	}

    public void dismissDialog(View v){
        dialogAddFrames.dismiss();
        dialogAddFramesActive = false;
        renderPictograms();
    }

    public void addPictograms(View v) {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "multi");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());

        EditModeActivity.this.startActivityForResult(i, 1);
    }

    /**
     * Possibly obsolete, as it overrides an identical method
     * @param menu
     * @return
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void OnCurrentFrameChanged(
			EditModeFrameView editModeFrameView, int ChoiceNumber) {
	}

}