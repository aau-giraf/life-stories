package dk.aau.cs.giraf.tortoise.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.EditChoiceFrameView;
import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.interfaces.OnCurrentFrameEventListener;
import dk.aau.cs.giraf.tortoise.interfaces.OnMainLayoutEventListener;
import dk.aau.cs.giraf.tortoise.interfaces.OnMediaFrameEventListener;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.SequenceViewGroup;
import dk.aau.cs.giraf.tortoise.SequenceAdapter;
import dk.aau.cs.giraf.tortoise.PictogramView.OnDeleteClickListener;
import dk.aau.cs.giraf.tortoise.SequenceViewGroup.OnNewButtonClickedListener;
import dk.aau.cs.giraf.tortoise.SequenceViewGroup.OnRearrangeListener;
import dk.aau.cs.giraf.tortoise.PictogramView;

public class EditModeActivity extends TortoiseActivity implements OnCurrentFrameEventListener {


	private static final int DIALOG_SAVE = 1;
	private static final int DIALOG_EXIT = 2;
	private static final int DIALOG_PROMT_FOR_TITLE = 3;
	private static final int DIALOG_SELECT_CHOICE = 4;
	
	static int selectedChoice = 0;
	public List<OnMainLayoutEventListener> mainLayoutListeners =
			new ArrayList<OnMainLayoutEventListener>();
	public List<OnCurrentFrameEventListener> currentFrameListeners =
			new ArrayList<OnCurrentFrameEventListener>();
	public List<OnMediaFrameEventListener> mediaFrameListeners =
			new ArrayList<OnMediaFrameEventListener>();
    EditModeFrameView currentEditModeFrame;
	RelativeLayout menuBar;
    SequenceViewGroup sequenceViewGroup;
    GDialog dialogAddFrames;
    GDialog gdialog;

    private boolean dialogAddFramesActive;
    private boolean isInEditMode;
    private SequenceAdapter adapter;
    private Sequence sequence;
    private int lastPosition;
	
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
		//mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

		int template = this.getIntent().getExtras().getInt("template");
		if(template == -1) {
			LifeStory.getInstance().setCurrentStory(new Sequence());
		}
		else {
			LifeStory.getInstance().setCurrentTemplate(EditModeActivity.this.getApplicationContext(), template); // TODO: kan EditModeActivity. slettes?!?
			//TODO: Render template again when fixed.
			// renderTemplate();
		}

		renderEditMenu();

        // TODO: Always true.. for now (Dan)
        isInEditMode = true;

        // Set current sequence
        sequence = LifeStory.getInstance().getCurrentStory();

        // Create Adapter
        adapter = setupAdapter();

        // Create Sequence Group
        sequenceViewGroup = setupSequenceViewGroup(adapter);
	}

	@Override
	public void onBackPressed() {
		renderDialog(DIALOG_EXIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

        // Remove the highlight from the add pictogram button
        final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup);
        sequenceGroup.placeDownAddNewButton();

        //Add pictograms to NEW MediaFrame
		if (resultCode == RESULT_OK && requestCode == 1) {
			int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");

			if (checkoutIds.length == 0) {
				Toast t = Toast.makeText(EditModeActivity.this, "Ingen pictogrammer valgt.", Toast.LENGTH_LONG);
				t.show();
			}
			else
			{
                MediaFrame newMediaFrame = new MediaFrame();

                for(int id : checkoutIds)
                {
                    Pictogram pictogram = PictoFactory.getPictogram(this, id);
                    newMediaFrame.addContent(pictogram);
                }

                List<MediaFrame> mediaFrames = new ArrayList<MediaFrame>();
                mediaFrames = sequence.getMediaFrames();
                mediaFrames.add(newMediaFrame);

                sequence.setMediaFrames(mediaFrames);

                adapter.notifyDataSetChanged();


                /*List<Integer> pictoIDList = new ArrayList<Integer>();

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
                renderPictograms();*/



			}
		}
        //Change story image
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
                // We expect a null pointer exception if the pictogram is without image
                // TODO: Investigate if this still happens with the new DB.
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
        //Change choice icon
        else if (resultCode == RESULT_OK && requestCode == 3){
            try{
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    Toast t = Toast.makeText(EditModeActivity.this, "Ingen pictogrammer valgt.", Toast.LENGTH_LONG);
                    t.show();
                }
                else
                {
                    try{
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        sequence.getMediaFrames().get(lastPosition).setChoicePictogram(picto);
                        renderChoiceIcon(lastPosition);
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
        // Add picotgrams to EXISTING MediaFrame
        else if(resultCode == RESULT_OK && requestCode == 4){
            try{
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");

                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt.");
                }
                else
                {
                    MediaFrame mediaFrame = sequence.getMediaFrame(lastPosition);

                    for(int id : checkoutIds)
                    {
                        Pictogram pictogram = PictoFactory.getPictogram(this, id);
                        mediaFrame.addContent(pictogram);
                    }

                    adapter.notifyDataSetChanged();
                    renderContentPictograms(lastPosition);
                }
            }
            catch (Exception e){
                GuiHelper.ShowToast(this, e.toString());
            }
        }
	}

    private SequenceAdapter setupAdapter() {
        final SequenceAdapter adapter = new SequenceAdapter(this, sequence);

        // Setup delete handler.
        adapter.setOnAdapterGetViewListener(new SequenceAdapter.OnAdapterGetViewListener() {
            @Override
            public void onAdapterGetView(final int position, final View view) {

                if (view instanceof PictogramView) {
                    PictogramView pictoView = (PictogramView) view;

                    pictoView
                            .setOnDeleteClickListener(new OnDeleteClickListener() {
                                @Override
                                public void onDeleteClick() {
                                    sequence.deleteMediaFrame(position);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                }
            }
        });

        return adapter;
    }

    private SequenceViewGroup setupSequenceViewGroup(final SequenceAdapter adapter) {
        final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup);
        sequenceGroup.setEditModeEnabled(isInEditMode);
        sequenceGroup.setAdapter(adapter);

        // Handle rearrange
        sequenceGroup
                .setOnRearrangeListener(new OnRearrangeListener() {
                    @Override
                    public void onRearrange(int indexFrom, int indexTo) {
                        LifeStory.getInstance().getCurrentStory().rearrange(indexFrom, indexTo);
                        adapter.notifyDataSetChanged();
                    }
                });

        // Handle new view
        sequenceGroup
                .setOnNewButtonClickedListener(new OnNewButtonClickedListener() {
                    @Override
                    public void onNewButtonClicked() {
                        final SequenceViewGroup sequenceGroup = (SequenceViewGroup) findViewById(R.id.sequenceViewGroup);
                        sequenceGroup.liftUpAddNewButton();

                        addPictograms(findViewById(R.id.addMediaFrame));
                    }
                });

        sequenceGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                                    int position, long id) {
                renderAddContentMenu(position);
            }
        });

        return sequenceGroup;
    }

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
					CheckBox template = (CheckBox)dialog.findViewById(R.id.templateCheckbox);
					CheckBox story = (CheckBox)dialog.findViewById(R.id.storyCheckbox);
                    LifeStory ls = LifeStory.getInstance();

					if(template.isChecked()) {
                        ls.addTemplate();
                        saveSequence(ls.getCurrentStory(),
                                dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.STORY,
                                ls.getGuardian().getId());
					}
					if(story.isChecked()) {
                        ls.addStory();
                        saveSequence(ls.getCurrentStory(),
                                dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.STORY,
                                ls.getChild().getId());
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

    public void renderContentPictograms(int position){

        if(sequence.getMediaFrames().get(position).getContent().size() == 0)
        {
            sequence.getMediaFrames().remove(position);
            adapter.notifyDataSetChanged();
            dismissAddContentDialog(getCurrentFocus());
        }
        else
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
            LinearLayout newChoiceContent = (LinearLayout) dialogAddFrames.findViewById(R.id.newChoiceContent2);
            newChoiceContent.removeAllViews();

            for(Pictogram p : sequence.getMediaFrames().get(position).getContent()) {
                EditChoiceFrameView choiceFramView = new EditChoiceFrameView(this, sequence.getMediaFrames().get(position), p, params);
                choiceFramView.addDeleteButton(position);
                newChoiceContent.addView(choiceFramView);
            }

            renderChoiceIcon(position);
            adapter.notifyDataSetChanged();
        }


    }


	public void renderAddContentMenu(int position) {

        if(sequence.getMediaFrames().get(position).getContent().size() == 0)
        {
            sequence.getMediaFrames().remove(position);
            adapter.notifyDataSetChanged();
        }
        else
        {
        dialogAddFrames = new GDialog(this, LayoutInflater.from(this).inflate(R.layout.dialog_add_content,null));

        if(dialogAddFrames.isShowing())
        {
            dialogAddFrames.dismiss();
        }

        dialogAddFramesActive = true;
        lastPosition = position;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
        LinearLayout newChoiceContent = (LinearLayout) dialogAddFrames.findViewById(R.id.newChoiceContent2);
        newChoiceContent.removeAllViews();

        for(Pictogram p : sequence.getMediaFrames().get(position).getContent()) {
            EditChoiceFrameView choiceFramView = new EditChoiceFrameView(this, sequence.getMediaFrames().get(position), p, params);
            choiceFramView.addDeleteButton(position);
            newChoiceContent.addView(choiceFramView);
        }

        adapter.notifyDataSetChanged();

        renderChoiceIcon(position);
        dialogAddFrames.show();
        }
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

        ImageButton print = (ImageButton)findViewById(R.id.printSequence);
        print.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //GuiHelper.ShowToast(EditModeActivity.this, "Testtest?");
                EditModeActivity.this.printSequence();
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

    public void dismissAddContentDialog(View v){
        dialogAddFrames.dismiss();
        dialogAddFramesActive = false;
        adapter.notifyDataSetChanged();
//        renderPictograms();
    }

    public void addPictograms(View v) {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "multi");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());

        switch(v.getId()){
            case R.id.addChoice2:
                EditModeActivity.this.startActivityForResult(i, 4);
                break;
            case R.id.addMediaFrame:
                EditModeActivity.this.startActivityForResult(i, 1);
                break;
            default:
                GuiHelper.ShowToast(this, "EditModeActivity -> addPictograms(View): Supplied View not recognized!");
                break;
        }
    }

    public void chooseChoicePictogram(View v){
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        EditModeActivity.this.startActivityForResult(i, 3);
    }

    public void renderChoiceIcon(int position){
        ImageView choiceIcon = (ImageView) dialogAddFrames.findViewById(R.id.choiceIcon);
        ImageView deleteBtn = (ImageView) dialogAddFrames.findViewById(R.id.removeChoiceIcon);

        Pictogram currentChoiceIcon = sequence.getMediaFrames().get(position).getChoicePictogram();

        if (currentChoiceIcon == null){

            Bitmap defaultBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.question);

            choiceIcon.setImageBitmap(defaultBitmap);
            deleteBtn.setVisibility(View.GONE);
        }
        else{
            choiceIcon.setImageBitmap(currentChoiceIcon.getImageData());
            deleteBtn.setVisibility(View.VISIBLE);

        }
        adapter.notifyDataSetChanged();
//TODO:        renderPictograms();
    }

    public void removeChoiceIcon(View v){
        sequence.getMediaFrames().get(lastPosition).setChoicePictogram(null);
        renderChoiceIcon(lastPosition);
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

    private void saveSequence(Sequence currentStory, dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType type, int id) {
        DBController.getInstance().saveSequence(currentStory, type, id, getApplicationContext());
    }

    public void printSequence(){
        GuiHelper.ShowToast(this, "Are you still there?");
        Bitmap combinedSequence = combineFrames();
        sendSequenceToEmail(combinedSequence, "dan.skoett.petersen@gmail.com", LifeStory.getInstance().getCurrentStory().getTitle(), "Hej Dan");
    }

    public Bitmap combineFrames(){

        int frameDimens = sequence.getMediaFrames().get(0).getContent().get(0).getImageData().getWidth();
        int numframes = sequence.getMediaFrames().size();
        int spacing = 10;

        int width = ((frameDimens+spacing)*numframes)-spacing;
        int height = frameDimens;

        Bitmap combinedSequence;
        combinedSequence = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(combinedSequence);


        float leftOffset = 0f;
        for(MediaFrame frame : sequence.getMediaFrames()){
            comboImage.drawBitmap(frame.getContent().get(0).getImageData(), leftOffset, 0f, null);
            leftOffset += frameDimens+spacing;
        }

        return combinedSequence;
    }

    public void sendSequenceToEmail(Bitmap seqImage, String emailAddress, String subject, String message){

        FileOutputStream out;
        try {
            out = new FileOutputStream("tempSeq");
            seqImage.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File tempSeq = new File("data/data/dk.aau.cs.giraf.tortoise/tempSeq.png");


        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("message/rfc822");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, message);
        email.putExtra(Intent.EXTRA_STREAM, tempSeq);

        try{
        startActivity(Intent.createChooser(email, "Vælg en email-klient"));
        }catch (android.content.ActivityNotFoundException ex) {
            GuiHelper.ShowToast(this, "Fuck you.");
        }

        boolean deleted = tempSeq.delete();
    }

}