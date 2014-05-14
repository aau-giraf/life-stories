package dk.aau.cs.giraf.tortoise.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.gui.GButton;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleEditActivity extends ScheduleActivity {
    public List<List<ImageView>> weekdayLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // check whether tablet is in portrait or landscape mode and set the layout accordingly
        // landscape mode shows mode days than portrait mode
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            isInLandscape = true;
            setContentView(R.layout.schedule_edit_activity);
            GButton b = (GButton) findViewById(R.id.schedule_image_button);
        } else {
            isInLandscape = false;
            setContentView(R.layout.schedule_edit_activity_portrait);
        }

        showAddButtons();

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null) {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }

        weekdaySequences = new ArrayList<Sequence>();

        // add empty sequences for each week day
        for (int i = 0; i < 7; i++) {
            weekdaySequences.add(i, new Sequence());
        }
        LifeStory.getInstance().setCurrentStory(new Sequence());
    }

    @Override
    public void onResume() {
        // this method is also called after oncreate()
        // makes sure that current weekday is also marked after resume of the app
        super.onResume();

        // mark the current weekday in the scheduler
        markCurrentWeekday();
    }

    public void weekdaySelected(View v) {
        markCurrentWeekday();
    }

    // this method handles pictograms sent back via an intent from pictosearch
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 2) {
            // i think this is triggered when a profile image is chosen from pictosearch
            try {
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                } else {
                    try {
                        LifeStory.getInstance().setCurrentStory(new Sequence());
                        LifeStory.getInstance().getCurrentStory().setTitlePictoId(checkoutIds[0]);
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
                        bitmap = LayoutTools.getSquareBitmap(bitmap);
                        bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 20);
                        LifeStory.getInstance().getCurrentStory().setTitleImage(bitmap);
                        // TODO: fix this to not just write on top of image
                        GButton storyImage = (GButton) findViewById(R.id.schedule_image_button);
                        storyImage.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                    }
                    //We expect a null pointer exception if the pictogram is without image
                    //TODO: Investigate if this still happens with the new DB.
                    // It still does
                    catch (NullPointerException e) {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (Exception e) {
                GuiHelper.ShowToast(this, e.toString() + " - rcode 2.");
            }
        } else if (resultCode == RESULT_OK && requestCode == 3) {
            // this code is executed when the week scheduler requests an image from pictosearch
            try {
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                } else // when pictograms are received
                {
                    try {
                        MediaFrame mf = new MediaFrame();

                        // add all pictograms to list and add them to a sequence
                        for (int id : checkoutIds) {
                            Pictogram pictogram = PictoFactory.getPictogram(this, id);
                            mf.addContent(pictogram);
                        }

                        weekdaySequences.get(weekdaySelected).getMediaFrames().add(mf);

                        if (weekdayLayout.getChildCount() > 0) {
                            weekdayLayout.removeViewAt(weekdayLayout.getChildCount() - 1); // remove add button
                        }
                        // add item to scroll view
                        addItems(mf, weekdayLayout);
                        weekdayLayout.addView(addButton()); // add the add button again


                    } catch (NullPointerException e) {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (NullPointerException e) {
                GuiHelper.ShowToast(this, "Fejl");
            }
        } else if (resultCode == RESULT_OK && requestCode == 4) {
            try {
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");

                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt.");
                } else {
                    MediaFrame mediaFrame = weekdaySequences.get(weekdaySelected).getMediaFrame(lastPosition);

                    for (int id : checkoutIds) {
                        Pictogram pictogram = PictoFactory.getPictogram(this, id);
                        mediaFrame.addContent(pictogram);
                    }

                    renderSchedule(true);
                    updateMultiChoiceDialog(lastPosition);
                }
            } catch (Exception e) {
                GuiHelper.ShowToast(this, e.toString());
            }
        }
        //Change choice icon
        else if (resultCode == RESULT_OK && requestCode == 5){
            try{
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    Toast t = Toast.makeText(this, "Ingen pictogrammer valgt.", Toast.LENGTH_LONG);
                    t.show();
                }
                else
                {
                    try{
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        weekdaySequences.get(weekdaySelected).getMediaFrames().get(lastPosition).setChoicePictogram(picto);
                        renderChoiceIcon(lastPosition, this);
                    }
                    //We expect a null pointer exception if the pictogram is without image
                    //TODO: Investigate if this still happens with the new DB.
                    // It still does
                    catch (NullPointerException e){
                        Toast t = Toast.makeText(this, "Der skete en uventet fejl.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            } catch (Exception e){
                GuiHelper.ShowToast(this, e.toString());
            }
        }
    }

    // this is just a variable for a workaround
    //  public static LinearLayout weekdayLayout;

    public boolean saveSchedule(View v) {

        Sequence scheduleSeq = LifeStory.getInstance().getCurrentStory();

        if (scheduleSeq.getTitlePictoId() == 0){
            GuiHelper.ShowToast(this, "Skema er ikke gemt!, vælg et titel-pictogram");
            return false;
        }
        Editable title = ((EditText) findViewById(R.id.scheduleName)).getText();
        if (title != null) {
            scheduleSeq.setTitle(title.toString());
        }else if(title.equals(""))
        {
            // if no title, set a default one
            scheduleSeq.setTitle(getString(R.string.unnamed_sequence));
        }

        boolean s1 = true;
        //Loops trough the days sequences and saves them to the database
        for (Sequence daySeq : super.weekdaySequences) {
            daySeq.setTitle("");
            daySeq.setTitlePictoId(1);
            s1 = s1 && DBController.getInstance().saveSequence(daySeq,
                    dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULEDDAY,
                    LifeStory.getInstance().getChild().getId(),
                    getApplicationContext());
            MediaFrame mf = new MediaFrame();
            mf.setNestedSequenceID(daySeq.getId());
            scheduleSeq.getMediaFrames().add(mf);
        }

        //Saves the week sequence with reference to the days
        boolean s2 = DBController.getInstance().saveSequence(scheduleSeq,
                dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULE,
                LifeStory.getInstance().getChild().getId(),
                getApplicationContext());

        if (s1 && s2){
            GuiHelper.ShowToast(this, "Skema gemt");
            return true;
        }
        GuiHelper.ShowToast(this, "Skema er ikke gemt!");
        return false;
    }

    public void addPictograms(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "multi");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.this.startActivityForResult(i, 4);
    }

    public void chooseChoicePictogram(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch",
                "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.this.startActivityForResult(i, 5);
    }

    public void removeChoiceIcon(View v){
        weekdaySequences.get(weekdaySelected).getMediaFrames().get(lastPosition).setChoicePictogram(null);
        renderChoiceIcon(lastPosition, this);
    }

    public class DrawView extends View {
        Paint paint = new Paint();

        public DrawView(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            canvas.drawRect(30, 30, 80, 80, paint);
            paint.setStrokeWidth(0);
            paint.setColor(Color.CYAN);
            canvas.drawRect(33, 60, 77, 77, paint);
            paint.setColor(Color.YELLOW);
            canvas.drawRect(33, 33, 77, 60, paint);

        }

    }


}