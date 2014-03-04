package dk.aau.cs.giraf.tortoise;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.tortoise.PictogramView.OnDeleteClickListener;
import dk.aau.cs.giraf.tortoise.SequenceListAdapter.OnAdapterGetViewListener;

public class MainActivity extends Activity {
	
	private final int DIALOG_DELETE = 1;
	private boolean isInEditMode = false;
	private boolean isInTemplateMode = false;
	private boolean canFinish;
	private SequenceListAdapter sequenceAdapter;
	private Bitmap childImage;
	private Bitmap guardianImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup_activity);

        // Warn user and do not execute Tortoise if not launched from Giraf
		if (getIntent().getExtras() == null) {
			Toast t = Toast.makeText(this, 
					"Tortoise skal startes fra GIRAF", Toast.LENGTH_LONG);
			t.show();
			finish();
		}
        // If launched from Giraf, then execute!
		else {
            // Initialize image and name of profile
			ImageView profileImage = (ImageView)findViewById(R.id.profileImage);
			TextView profileName = (TextView)findViewById(R.id.child_name);

			Intent i = getIntent();
			Helper h = new Helper(this);

            // Set guardian- and child profiles
			LifeStory.getInstance().setGuardian(
					h.profilesHelper.getProfileById(i.getLongExtra("currentGuardianID", -1)));
			LifeStory.getInstance().setChild(
					h.profilesHelper.getProfileById(i.getLongExtra("currentChildID", -1)));
			profileName.setText(LifeStory.getInstance().getChild().getFirstname());
			setProfileImages();
			profileImage.setImageBitmap(childImage);

            // Clear existing life stories
			LifeStory.getInstance().getStories().clear();
			LifeStory.getInstance().getTemplates().clear();

            // Set templates belonging to the chosen guardian and stories belonging to the chosen child
			JSONSerializer js = new JSONSerializer();
			try {
				LifeStory.getInstance().setTemplates(
						js.loadSettingsFromFile(
								getApplicationContext(), 
								LifeStory.getInstance().getGuardian().getId()));
				LifeStory.getInstance().setStories(
						js.loadSettingsFromFile(
								getApplicationContext(),
								LifeStory.getInstance().getChild().getId()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Initialize grid view
			GridView sequenceGrid = (GridView)findViewById(R.id.sequence_grid);
			sequenceAdapter = initAdapter();
			sequenceGrid.setAdapter(sequenceAdapter);
			
			// Creates clean sequence and starts the sequence activity - ready to add pictograms.
			final ImageButton createButton = (ImageButton)findViewById(R.id.add_button);
			
			createButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					canFinish = false;
					Intent i = new Intent(getApplicationContext(), EditModeActivity.class);
					i.putExtra("template", -1);
					
					startActivity(i);
				}
			});

			// Load Sequence
			sequenceGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	
					Intent i;

					if(isInTemplateMode) {
						canFinish = false;
						i = new Intent(getApplicationContext(), EditModeActivity.class);
						i.putExtra("template", arg2);
					}
					else {
						canFinish = false;
						i = new Intent(getApplicationContext(), ViewModeActivity.class);
						i.putExtra("story", arg2);
					}
	
					startActivity(i);
				}		
			});
			
			// Edit mode switcher button
			ToggleButton button = (ToggleButton) findViewById(R.id.edit_mode_toggle);
			
			button.setOnClickListener(new ImageButton.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ToggleButton button = (ToggleButton)v;
					isInEditMode = button.isChecked();
					GridView sequenceGrid = (GridView) findViewById(R.id.sequence_grid);
					
					// Make sure that all views currently not visible will have the correct editmode when they become visible
					sequenceAdapter.setEditModeEnabled(isInEditMode);
	
					//createButton.setVisibility(isInEditMode ? View.VISIBLE : View.GONE);
					
					// Update the editmode of all visible views in the grid
					for (int i = 0; i < sequenceGrid.getChildCount(); i++) {
						View view = sequenceGrid.getChildAt(i);
						
						if (view instanceof PictogramView) {
							((PictogramView)view).setEditModeEnabled(isInEditMode);
						}
					}
				}
			});
			
			// Template mode switcher button
			ToggleButton templateToggle = (ToggleButton) findViewById(R.id.template_mode_toggle);
			
			templateToggle.setOnClickListener(new ImageButton.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ToggleButton button = (ToggleButton)v;
					ImageView profileImage = (ImageView)findViewById(R.id.profileImage);
					TextView profileName = (TextView)findViewById(R.id.child_name);
					if(button.isChecked()) {
						Profile g = LifeStory.getInstance().getGuardian();
						profileName.setText(
								g.getFirstname() + " " + g.getSurname());
						profileImage.setImageBitmap(guardianImage);
					}
					else {
						Profile c = LifeStory.getInstance().getChild();
						profileName.setText(c.getFirstname());
						profileImage.setImageBitmap(childImage);
					}
					isInTemplateMode = button.isChecked();
					sequenceAdapter.setTemplateModeEnabled(isInTemplateMode);
					sequenceAdapter.notifyDataSetChanged();
				}
			});
		}
	}		
	
	public SequenceListAdapter initAdapter() {
		final SequenceListAdapter adapter = new SequenceListAdapter(this);
		
		adapter.setOnAdapterGetViewListener(new OnAdapterGetViewListener() {
			
			@Override
			public void onAdapterGetView(final int position, View view) { 
				if (view instanceof PictogramView) {
					
					PictogramView pictoView = (PictogramView) view;
					
					pictoView.setOnDeleteClickListener(new OnDeleteClickListener() {
						
						@Override
						public void onDeleteClick() {
							renderDialog(DIALOG_DELETE, position);
						}
					});
				}
			}
		});
		
		return adapter;
	}
	
	@Override
	protected void onStart() {
		canFinish = true;
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		if(canFinish) {
			finish();
		}
		super.onStop();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater()
				.inflate(R.menu.activity_tortoise_startup_screen, menu);
		return true;
	}	
	
	@Override
	protected void onResume() {
		ToggleButton templateMode = (ToggleButton)findViewById(R.id.template_mode_toggle);
		ToggleButton editMode = (ToggleButton) findViewById(R.id.edit_mode_toggle);
		ImageView profileImage = (ImageView)findViewById(R.id.profileImage);
		TextView profileName = (TextView)findViewById(R.id.child_name);
		
		isInEditMode = false;
		isInTemplateMode = false;
		templateMode.setChecked(false);
		editMode.setChecked(false);
		Profile c = LifeStory.getInstance().getChild();
		profileName.setText(c.getFirstname());
		profileImage.setImageBitmap(childImage);
		sequenceAdapter.setEditModeEnabled(isInEditMode);
		sequenceAdapter.setTemplateModeEnabled(isInTemplateMode);
		sequenceAdapter.notifyDataSetChanged();
		super.onResume();
	}
	
	private void setProfileImages() {
		
		Bitmap bm;
		if(LifeStory.getInstance().getChild().getPicture() != null) {
			bm = LayoutTools.decodeSampledBitmapFromFile(
			LifeStory.getInstance().getChild().getPicture(), 100, 100);
			
		}
		else {
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
		}
		
		childImage = LayoutTools.getRoundedCornerBitmap(bm, this, 10);
		
		if(LifeStory.getInstance().getGuardian().getPicture() != null) {
			bm = LayoutTools.decodeSampledBitmapFromFile(
			LifeStory.getInstance().getGuardian().getPicture(), 100, 100);
		}
		else {
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
		}
		
		guardianImage = LayoutTools.getRoundedCornerBitmap(bm, this, 10);
	}
	
	public void renderDialog(int dialogId, final int position) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		String storyName;

        // If isInTemplateMode is true then the guardian profile is active. If not, the child profile is active.
		if(isInTemplateMode) {
			storyName = LifeStory.getInstance().getTemplates().get(position).getTitle();
		}
		else {
			storyName = LifeStory.getInstance().getStories().get(position).getTitle();
		}

        // Dialog that prompts for deleting a story or template
		switch (dialogId) {
		case DIALOG_DELETE:
			dialog.setContentView(R.layout.dialog_custom);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			TextView exitTitle = (TextView)dialog.findViewById(R.id.titleTextView);
			exitTitle.setText(R.string.dialog_delete_title);
			TextView exitMessage = (TextView)dialog.findViewById(R.id.messageTextView);
			exitMessage.setText(getResources().getString(R.string.dialog_delete_message) + " \"" + storyName + "\"");
			Button exitYes = (Button)dialog.findViewById(R.id.btn_yes);
			exitYes.setText(R.string.yes);
			exitYes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					JSONSerializer js = new JSONSerializer();
					if(isInTemplateMode) {
						LifeStory.getInstance().getTemplates().remove(position);
						try {
							js.saveSettingsToFile(getApplicationContext(),
									LifeStory.getInstance().getTemplates(),
									LifeStory.getInstance().getGuardian().getId());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						LifeStory.getInstance().getStories().remove(position);
						try {
							js.saveSettingsToFile(getApplicationContext(),
									LifeStory.getInstance().getStories(), 
									LifeStory.getInstance().getChild().getId());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					sequenceAdapter.setItems();
					sequenceAdapter.notifyDataSetChanged();
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
}
