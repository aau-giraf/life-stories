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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GDialogMessage;
import dk.aau.cs.giraf.gui.GRadioButton;
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
    GDialog printAlignmentDialog;

    private boolean dialogAddFramesActive;
    private boolean isInEditMode;
    private SequenceAdapter adapter;
    private Sequence sequence;
    private int lastPosition;
    private File[] file;
    private String debugEmail = null; // Set this to debug the print sequence function!
	
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

        // If called with template, initialize it. Otherwise reate new sequence.
		initSequence(getIntent());

		renderEditMenu();

        // TODO: Always true.. for now
        isInEditMode = true;

        // Create Adapter
        adapter = setupAdapter();

        // Create Sequence Group
        sequenceViewGroup = setupSequenceViewGroup(adapter);

        // Init print alignment dialog
        printAlignmentDialog = new GDialog(this, LayoutInflater.from(this).inflate(R.layout.dialog_print_alignment, null));
	}

    /**
     * Initializes the edit-mode if a template is passed with the intent.
     * Otherwise it creates a new sequence.
     * @param intent
     */
    private void initSequence(Intent intent) {
        int template = intent.getIntExtra("template", -1);


        if(template == -1)
        {
            LifeStory.getInstance().setCurrentStory(new Sequence());
        }
        else
        {
            Sequence passedSequence = LifeStory.getInstance().getStories().get(template);
            LifeStory.getInstance().setCurrentStory(passedSequence);
        }

        // Set current sequence
        sequence = LifeStory.getInstance().getCurrentStory();
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

                addContentToMediaFrame(newMediaFrame, checkoutIds);

                /*for(int id : checkoutIds)
                {
                    Pictogram pictogram = PictoFactory.getPictogram(this, id);
                    newMediaFrame.addContent(pictogram);
                }*/

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
                    GButton storyImage = (GButton) findViewById(R.id.storyImage);
                    storyImage.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
			    }
                // We expect a null pointer exception if the pictogram is without image
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
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");
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

                    addContentToMediaFrame(mediaFrame, checkoutIds);

                    /*for(int id : checkoutIds)
                    {
                        Pictogram pictogram = PictoFactory.getPictogram(this, id);
                        mediaFrame.addContent(pictogram);
                    }*/

                    adapter.notifyDataSetChanged();
                    renderContentPictograms(lastPosition);
                }
            }
            catch (Exception e){
                GuiHelper.ShowToast(this, e.toString());
            }
        }
	}

    @Override
    protected void onResume() {
        super.onResume();
        if(file != null)
        {
            for(File f : file)
            {
                f.delete();
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
        boolean showOverlay = true;
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		switch (dialogId)
        {
		case DIALOG_SAVE:
			dialog.setContentView(R.layout.dialog_save);

			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			Button saveYes = (Button)dialog.findViewById(R.id.btn_yes);
			saveYes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
                    LifeStory ls = LifeStory.getInstance();

                    ls.addStory();
                    saveSequence(ls.getCurrentStory(),
                            dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.STORY,
                            ls.getChild().getId());

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

        //Delete the mediaframe at position if there is no content left.
        if(sequence.getMediaFrames().get(position).getContent().size() == 0)
        {
            sequence.getMediaFrames().remove(position);
            adapter.notifyDataSetChanged();
        }
        //Otherwise, render the menu.
        else
        {
        dialogAddFrames = new GDialog(this, LayoutInflater.from(this).inflate(R.layout.dialog_add_content,null));

        //If the dialog is already showing, dismiss it.
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
		GButton storyImage = (GButton) findViewById(R.id.storyImage);
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
			storyImage.setBackgroundDrawable(new BitmapDrawable(getResources(), LifeStory.getInstance().getCurrentStory().getTitleImage()));
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

		GButton exit = (GButton)findViewById(R.id.exitEditMode);
		exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditModeActivity.this.renderDialog(DIALOG_EXIT);
			}
		});

        GButton save = (GButton)findViewById(R.id.save);
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

        GButton print = (GButton)findViewById(R.id.printSequence);
        print.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //GuiHelper.ShowToast(EditModeActivity.this, "Testtest?");
                EditModeActivity.this.openPrintAlignmentDialogBox();
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

    public void openPrintAlignmentDialogBox(){
        printAlignmentDialog.show();
    }

    public void printSequence(View v) {
        GRadioButton verticalButton = (GRadioButton) printAlignmentDialog.findViewById(R.id.vertical);
        Bitmap[] combinedSequence;

        //Warn the user and stop if trying to print empty sequence.
        if(sequence.getMediaFrames().size() == 0)
        {
            GuiHelper.ShowToast(this, "Du forsøgte at sende en tom sekvens.");
            printAlignmentDialog.dismiss();
            return;
        }


        if (verticalButton.isChecked())
            combinedSequence = combineFrames("vertical");
        else
            combinedSequence = combineFrames("horizontal");

        // Set debug-email in class variable "debugEmail" when debugging!
        String email;
        if (debugEmail == null)
            email = LifeStory.getInstance().getGuardian().getEmail();
        else
            email = debugEmail;

        String message;
        if(combinedSequence.length == 1)
        {
            message = "Print det vedhæftede billede som helsidet billede på A4-størrelse papir for 3x3 cm piktogrammer.";
        }else
        {
            message = "Print de vedhæftede billeder som helsidede billeder på A4-størrelse papir for 3x3 cm piktogrammer.";
        }


        sendSequenceToEmail(combinedSequence, email, "Livshistorie: " + LifeStory.getInstance().getCurrentStory().getTitle(), message);
    }


    /**
     * Combines all pictograms in a number of bitmaps either horizontally or vertically.
     * @param direction Can be either "horizontal" or "vertical". Determines the direction in which the pictograms will be added.
     * @return An array of bitmaps containing pictograms.
     */
    private Bitmap[] combineFrames(String direction){

        int frameDimens = sequence.getMediaFrames().get(0).getContent().get(0).getImageData().getHeight();
        int numframes = sequence.getMediaFrames().size();

        // Adjust spacing and offSet to have optimal number of pics / page.
        int spacing = 18;
        float offSet = 35f;

        int totalSeqLengthInPixels = ((frameDimens+spacing)*numframes);

        // Dimensions of pictograms in mm when printed.
        int printedDimens = 30;

        int a4height = (int) ((297.0/printedDimens)*frameDimens);
        int a4width = (int) ((210.0/printedDimens)*frameDimens);

        float center = (float)(a4width/2-frameDimens/2);

        int numberOfCanvases = (int) Math.ceil(totalSeqLengthInPixels/(a4height-offSet));
        int numberPicsPerLine = (int)Math.floor((a4height-offSet)/(totalSeqLengthInPixels/numframes));
        int numberOfPicsAdded = 0;

        List<Canvas> comboImage = new ArrayList<Canvas>();

        Bitmap[] combinedSequence = new Bitmap[numberOfCanvases];

        for(int i = 0; i < numberOfCanvases; i++)
        {

            if(direction == "vertical")
            {
                combinedSequence[i] = Bitmap.createBitmap(a4width, a4height, Bitmap.Config.ARGB_8888);
                comboImage.add(i, new Canvas(combinedSequence[i]));

                float offSetTemp = offSet;
                for (int ii = 0; ii < numberPicsPerLine && numberOfPicsAdded < numframes; ii++) {
                    Bitmap bm = sequence.getMediaFrames().get(ii).getContent().get(0).getImageData();
                    comboImage.get(i).drawBitmap(bm, center, offSetTemp, null);
                    offSetTemp += frameDimens + spacing;
                    numberOfPicsAdded++;
                }
            }
            else if(direction == "horizontal")
            {
                // Swapped height and width to "turn the paper".
                combinedSequence[i] = Bitmap.createBitmap(a4height, a4width, Bitmap.Config.ARGB_8888);
                comboImage.add(i, new Canvas(combinedSequence[i]));

                float offSetTemp = offSet;
                for (int ii = 0; ii < numberPicsPerLine && numberOfPicsAdded < numframes; ii++) {
                    Bitmap bm = sequence.getMediaFrames().get(ii).getContent().get(0).getImageData();
                    comboImage.get(i).drawBitmap(bm, offSetTemp, center, null);
                    offSetTemp += frameDimens + spacing;
                    numberOfPicsAdded++;
                }
            }
            else
            {
                // This should never be reachable, unless there is an error in the code
                // (if method is called with other than horizontal / vertical).
                GuiHelper.ShowToast(this, "Der skete en uventet fejl, prøv igen.");
            }
        }

        return combinedSequence;
    }

    public void sendSequenceToEmail(Bitmap[] seqImage, String emailAddress, String subject, String message){

        int numOfImages = seqImage.length;
        String[] filename = new String[numOfImages];


        for(int i = 0; i < numOfImages; i++)
        {
            filename[i] = "Sekvens del " + (i+1) + " af " + numOfImages + ".png";
        }

        file = getOutputMediaFile(filename, numOfImages);

        try{
            for(int i = 0; i < numOfImages; i++)
            {
                FileOutputStream out = new FileOutputStream(file[i]);
                seqImage[i].compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
            }
        }
        catch(Exception e){
            GuiHelper.ShowToast(this, e.toString());
        }

        ArrayList<Uri> fileUris = new ArrayList<Uri>();

        for(int i = 0; i < numOfImages; i++)
        {
            fileUris.add(Uri.fromFile(file[i]));
        }

        Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
        email.setType("message/rfc822");//("image/jpeg");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, message);
        email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
        int stop = 0;

        try{
        startActivity(Intent.createChooser(email, "Vælg en email-klient"));
        }catch (android.content.ActivityNotFoundException ex) {
            GuiHelper.ShowToast(this, "Fejl: Email klient ikke fundet!");
        }
        printAlignmentDialog.dismiss();
    }

    /**
     * Based on: http://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
     *
     * @param fileName
     * @return file
     */
    private File[] getOutputMediaFile(String[] fileName, int numOfImages){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile[] = new File[numOfImages];

        for(int i = 0; i < numOfImages; i++)
        {
            mediaFile[i] = new File(mediaStorageDir.getPath() + File.separator + fileName[i]);
        }

        return mediaFile;
    }

    public void verticalRButtonClicked(View v){
        GRadioButton radioButton = (GRadioButton) printAlignmentDialog.findViewById(R.id.horizontal);
        radioButton.setChecked(false);
    }

    public void horizontalRButtonClicked(View v){
        GRadioButton radioButton = (GRadioButton) printAlignmentDialog.findViewById(R.id.vertical);
        radioButton.setChecked(false);
    }

    public void dialogPrintAlignmentCancel(View v){
        printAlignmentDialog.dismiss();
    }
}