package dk.aau.cs.giraf.tortoise.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GTooltipBasic;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.LayoutTools;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.DBController;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleEditActivity extends ScheduleActivity
{
    public List<List<ImageView>> weekdayLists;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // check whether tablet is in portrait or landscape mode and set the layout accordingly
        // landscape mode shows mode days than portrait mode
        int screenOrientation = getResources().getConfiguration().orientation;
        if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            isInLandscape = true;
            setContentView(R.layout.schedule_edit_activity);
            ImageButton b = (ImageButton) findViewById(R.id.schedule_image_button);
            GTooltipBasic tooltip = new GTooltipBasic(b, "Tryk her for at tilføje et billede til ugeskemaet", R.drawable.ic_launcher);
            tooltip.Show();
        }
        else
        {
            isInLandscape = false;
            setContentView(R.layout.schedule_edit_activity_portrait);
        }

        showAddButtons();

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }

        weekdaySequences = new ArrayList<Sequence>();

        // add empty sequences for each week day
        for(int i = 0; i < 7; i++)
        {
            weekdaySequences.add(i, new Sequence());
        }
        LifeStory.getInstance().setCurrentStory(new Sequence());
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
            canvas.drawRect(33, 60, 77, 77, paint );
            paint.setColor(Color.YELLOW);
            canvas.drawRect(33, 33, 77, 60, paint );

        }

    }

    @Override
    public void onResume()
    {
        // this method is also called after oncreate()
        // makes sure that current weekday is also marked after resume of the app
        super.onResume();

        // mark the current weekday in the scheduler
        markCurrentWeekday();
    }

    public void weekdaySelected(View v)
    {
        markCurrentWeekday();
    }

    // this is just a variable for a workaround
  //  public static LinearLayout weekdayLayout;

    // this method handles pictograms sent back via an intent from pictosearch
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 2)
        {
            // i think this is triggered when a profile image is chosen from pictosearch
            try{
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds"); // .getLongArray("checkoutIds");
                if (checkoutIds.length == 0) {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                }
                else
                {
                    try{
                        LifeStory.getInstance().setCurrentStory(new Sequence());
                        LifeStory.getInstance().getCurrentStory().setTitlePictoId(checkoutIds[0]);
                        Pictogram picto = PictoFactory.getPictogram(getApplicationContext(), checkoutIds[0]);
                        Bitmap bitmap = picto.getImageData(); //LayoutTools.decodeSampledBitmapFromFile(picto.getImagePath(), 150, 150);
                        bitmap = LayoutTools.getSquareBitmap(bitmap);
                        bitmap = LayoutTools.getRoundedCornerBitmap(bitmap, getApplicationContext(), 20);
                        LifeStory.getInstance().getCurrentStory().setTitleImage(bitmap);
                        ImageView storyImage = (ImageView) findViewById(R.id.schedule_image_button);
                        storyImage.setImageBitmap(bitmap);
                    }
                    //We expect a null pointer exception if the pictogram is without image
                    //TODO: Investigate if this still happens with the new DB.
                    // It still does
                    catch (NullPointerException e)
                    {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (Exception e)
            {
                GuiHelper.ShowToast(this, e.toString() + " - rcode 2.");
            }
        }
        else if (resultCode == RESULT_OK && requestCode == 3)
        {
            // this code is executed when the week scheduler requests an image from pictosearch
            try
            {
                int[] checkoutIds = data.getExtras().getIntArray("checkoutIds");
                if (checkoutIds.length == 0)
                {
                    GuiHelper.ShowToast(this, "Ingen pictogrammer valgt");
                }
                else // when pictograms are received
                {
                    try
                    {
                        MediaFrame mf = new MediaFrame();

                        // add all pictograms to list and add them to a sequence
                        for(int id : checkoutIds)
                        {
                            Pictogram pictogram = PictoFactory.getPictogram(this, id);
                            mf.addContent(pictogram);
                        }

                        weekdaySequences.get(weekdaySelected).getMediaFrames().add(mf);

                        if (weekdayLayout.getChildCount() > 0){
                            weekdayLayout.removeViewAt(weekdayLayout.getChildCount() - 1); // remove add button
                        }
                        // add item to scroll view
                        addItems(mf, weekdayLayout);
                        weekdayLayout.addView(addButton()); // add the add button again


                    }
                    catch (NullPointerException e)
                    {
                        GuiHelper.ShowToast(this, "Der skete en uventet fejl");
                    }
                }
            } catch (NullPointerException e)
            {
                GuiHelper.ShowToast(this, "Fejl");
            }
        }

    }
    public boolean saveSchedule(View v){
        Sequence scheduleSeq = LifeStory.getInstance().getCurrentStory();

        if (scheduleSeq.getTitlePictoId() == 0){
            GuiHelper.ShowToast(this, "Skema er ikke gemt!, vælg et title pictogram");
            return false;
        }
        Editable title = ((EditText) findViewById(R.id.scheduleName)).getText();
        if (title != null){
            scheduleSeq.setTitle(title.toString());
        }

        boolean s1 = true;
        for(Sequence daySeq: super.weekdaySequences){
            daySeq.setTitle("");       //test value
            daySeq.setTitlePictoId(1); //test value
            s1 = s1 && DBController.getInstance().saveSequence(daySeq,
                    dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType.SCHEDULEDDAY,
                    LifeStory.getInstance().getChild().getId(),
                    getApplicationContext());
            MediaFrame mf = new MediaFrame();
            mf.setNestedSequenceID(daySeq.getId());
            scheduleSeq.getMediaFrames().add(mf);
        }

        // TODO hardcoded save for both child and guardian
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
}