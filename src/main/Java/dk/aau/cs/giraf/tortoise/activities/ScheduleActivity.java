package dk.aau.cs.giraf.tortoise.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.aau.cs.giraf.gui.GDialog;
import dk.aau.cs.giraf.gui.GToggleButton;
//import dk.aau.cs.giraf.gui.GirafPictogram;
import dk.aau.cs.giraf.gui.GirafInflatableDialog;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.EditChoiceFrameView;
import dk.aau.cs.giraf.tortoise.ProgressTracker;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.MediaFrame;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;

public class ScheduleActivity extends TortoiseActivity
{

    protected final String DELETE_SEQUENCES_TAG = "DELETE_SEQUENCES_TAG";
    protected final int PICTO_EDIT_PICTOGRAM_CALL = 4;
    protected final String PICTO_ADMIN_PACKAGE = "dk.aau.cs.giraf.pictosearch";
    protected final String PICTO_ADMIN_CLASS = PICTO_ADMIN_PACKAGE + "." + "PictoAdminMain";
    protected final String PICTO_INTENT_CHECKOUT_ID = "checkoutIds";

    // this is just a variable for a workaround
    public static LinearLayout weekdayLayout;
    public LinearLayout level1;
    GirafInflatableDialog replaceDialog;
    public GDialog multichoiceDialog;
    public static List<Sequence> weekdaySequences;
    public static int weekdaySelected;
    int lastPosition;
    protected int pictogramEditPos = -1;
    //int currentActivity = 0;
    int currentWeekday = 0;
    protected int[] progressActivity = new int[2];
    protected ProgressTracker progress = new ProgressTracker();
    int[] currentActivity = {-1,-1,-1,-1,-1,-1,-1};

    ArrayList<boolean[]> markedActivities = new ArrayList<boolean[]>();

    public void startPictosearch(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "single");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());

        this.startActivityForResult(i, 2);
    }

    public void startPictosearchForScheduler(View v)
    {
        Intent i = new Intent();
        i.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
        i.putExtra("purpose", "multi");
        i.putExtra("currentChildID", LifeStory.getInstance().getChild().getId());
        i.putExtra("currentGuardianID", LifeStory.getInstance().getGuardian().getId());
        ScheduleEditActivity.weekdayLayout = (LinearLayout) v.getParent();
        determineWeekSection(v);

        this.startActivityForResult(i, 3);
    }


    public void dismissAddContentDialog(View vs, View pictoView, int index, final int day)
    {
        multichoiceDialog.dismiss();

        RelativeLayout v = (RelativeLayout) level1.getChildAt(weekdaySelected); // the +1 is to choose the element at depth 2
        ScrollView level2 = (ScrollView) v.getChildAt(1);
        LinearLayout level3 = (LinearLayout) level2.getChildAt(0);
        ImageView selected = (ImageView) level3.getChildAt(index);
        if(selected != null){

            setPictogramSize(index, selected, day);
        }

    }

    public void showMultiChoiceDialog(final int position, int day, final View currentView, Activity activity, final int day2)
    {
            lastPosition = position;
            multichoiceDialog = new GDialog(this, LayoutInflater.from(this).inflate(R.layout.dialog_add_content, null));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
            final LinearLayout newChoiceContent = (LinearLayout) multichoiceDialog.findViewById(R.id.newChoiceContent2);
            newChoiceContent.removeAllViews();

            for(Pictogram p : weekdaySequences.get(day).getMediaFrames().get(position).getContent()) {
                final EditChoiceFrameView choiceFramView = new EditChoiceFrameView(this, weekdaySequences.get(day).getMediaFrames().get(position), p, params);
                if(activity instanceof ScheduleEditActivity)
                {
                    choiceFramView.addDeleteButton(position);
                }
                else if(activity instanceof ScheduleViewActivity)
                {
                    choiceFramView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateContent(view, position, currentView);
                            //renderSchedule(false);
                            dismissAddContentDialog(view, currentView, position, day2);
                        }
                    });
                }
                newChoiceContent.addView(choiceFramView);
            }

            // If we in view mode, hide edit buttons and text.
            if(activity instanceof ScheduleViewActivity)
            {
                multichoiceDialog.findViewById(R.id.addChoice2).setVisibility(View.GONE);
                multichoiceDialog.findViewById(R.id.choicesText).setVisibility(View.GONE);
            }


            //renderChoiceIcon(position, this);
            multichoiceDialog.show();
    }

    private void updateContent(View view, int index, View pictoView) {
        Pictogram selectedPictogram = weekdaySequences.get(weekdaySelected).getMediaFrames().get(lastPosition).getContent().get(getViewIndex(view));
        List<Pictogram> newContent = new ArrayList<Pictogram>();
        newContent.add(selectedPictogram);
        weekdaySequences.get(weekdaySelected).getMediaFrames().get(lastPosition).setContent(newContent);
        progress.setChangedDays(weekdaySequences);
        renderSchedule(false);
    }

    public void updateMultiChoiceDialog(int position)
    {
        int day = weekdaySelected;

        if(weekdaySequences.get(day).getMediaFrames().get(position).getContent().size() == 0)
        {
            weekdaySequences.get(day).getMediaFrames().remove(position);
            dismissAddContentDialog(getCurrentFocus(), null, 0, 0);
        }
        else
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
            LinearLayout newChoiceContent = (LinearLayout) multichoiceDialog.findViewById(R.id.newChoiceContent2);
            newChoiceContent.removeAllViews();

            for(Pictogram p : weekdaySequences.get(day).getMediaFrames().get(position).getContent()) {
                EditChoiceFrameView choiceFramView = new EditChoiceFrameView(this, weekdaySequences.get(day).getMediaFrames().get(position), p, params);
                choiceFramView.addDeleteButton(position);
                newChoiceContent.addView(choiceFramView);

            }

            //renderChoiceIcon(position, this);

        }
        renderSchedule(true);
    }

    /**
     * Updates the frames shown on the schedule. Set addButtonVisible to true in edit mode, false in view mode.
     * @param addButtonVisible
     */
    public void renderSchedule(Boolean addButtonVisible)
    {
        level1 = (LinearLayout) findViewById(R.id.completeWeekLayout);

        int childcount = level1.getChildCount();

        markedActivities = new ArrayList<boolean[]>();

        // find each of the individual week days
        for (int i = 0; i < childcount; i++)
        {
            try
            {
                // TODO: fix hardcoding of 1
                RelativeLayout v = (RelativeLayout) level1.getChildAt(i); // the +1 is to choose the element at depth 2
                ScrollView level2 = (ScrollView) v.getChildAt(1);
                LinearLayout level3 = (LinearLayout) level2.getChildAt(0);
                level3.removeAllViews();

                    for(MediaFrame mf : weekdaySequences.get(i).getMediaFrames())
                    {

                        addItems(mf, level3, i);
                    }
                markedActivities.add(i, new boolean[weekdaySequences.get(i).getMediaFrames().size()]);

                if(addButtonVisible)
                {
                    level3.addView(addButton());
                }


            }catch (Exception ex)
            {
                GuiHelper.ShowToast(this, "Der skete en fejl");
            }

        }
        //showAddButtons();
    }

    public void addItems(MediaFrame mf, LinearLayout layout, final int day)
    {
        try
        {
            List<Pictogram> pictoList = unpackSequence(mf);
                        // if only one pictogram is in the sequence, just display it in its respective week day
            if(pictoList.size() == 1)
            {
                addPictogramToDay(pictoList.get(0).getImageData(), layout, day);
            }
            else if(pictoList.size() > 1)
            {
                Bitmap choiceImage = ConstructWhiteBackground(BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_choose));
                addPictogramToDay(choiceImage, layout, day);
            }
        }
        catch (Exception ex)
        {
            GuiHelper.ShowToast(this, ex.toString() + " addbtn");
        }

    }

    public Bitmap ConstructWhiteBackground(Bitmap bitmap){

        Helper helper = new Helper(getApplicationContext());

        //Bitmap bitmap = helper.pictogramHelper.getPictogramById(id).getImage();
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

        return newBitmap;
    }

    public List<Pictogram> unpackSequence(MediaFrame mf)
    {
        return mf.getContent();
    }

    // this method returns an imageview containing the add pictogram button with a plus on it
    public ImageView addButton()
    {
        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 0); // only pad top of pictogram to create space between them
        iv.setLayoutParams(lp);
        iv.setBackgroundResource(R.layout.border_selected);

        int xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);

        Drawable resizedDrawable = resizeDrawable(R.drawable.icon_add, xy, xy);
        iv.setImageDrawable(resizedDrawable);

        // set listener on the add button so it starts pictosearch when clicked
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPictosearchForScheduler(view);
            }
        });

        // return the imageview with the plus image on it
        return iv;
    }

    public void addPictogramToDay(Bitmap bm, final LinearLayout layout, final int day) {

        ImageView iw = new ImageView(this);

        int xy = 0;

        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(layout.getMeasuredWidth(),layout.getMeasuredWidth());


        iw.setLayoutParams(param);
        iw.setScaleX(0.8f);
        iw.setScaleY(0.8f);

        /*This is set so that the pictograms are placed in the middle. ONLY works on Xlarge layout*/


        // use wider buttons when in portrait mode
        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
            // small buttons
            xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
        }/* else {
            // big buttons
            xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_portrait);
        }*/

        iw.setImageBitmap(resizeBitmap(bm, xy, xy)); // the same value is used for height and width because the pictogram should be square

        // set padding of each imageview containing
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        iw.setLayoutParams(lp);
        iw.setPadding(0, 0, 0, 0);

        final LinearLayout Wlayout = layout;

        // remove pictogram in the linear view contained in the scroll view
        iw.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                int index = getViewIndex(v);

                determineWeekSection(v);
                pictogramEditPos = index;
                // if we're in view (citizen) mode
                if(ScheduleActivity.this instanceof ScheduleViewActivity)
                {
                    /*Check if picto marked*/
                    if(!markedActivities.get(weekdaySelected)[index]) {
                        // this adds the cancel image on top of the original drawable of the pictogram
                        ImageView iv = (ImageView) Wlayout.getChildAt(getViewIndex(v));
                        Resources r = getResources();
                        Drawable[] dlayers = new Drawable[2];
                        dlayers[0] = iv.getDrawable();
                        int xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
                        dlayers[1] = resizeDrawable(r.getDrawable(R.drawable.cancel_button), xy, xy);
                        LayerDrawable layerDrawable = new LayerDrawable(dlayers);
                        iv.setImageDrawable(layerDrawable);
                        /*replaceDialog = GirafInflatableDialog.newInstance(
                                getApplicationContext().getString(R.string.delete_schedules),
                                getApplicationContext().getString(R.string.delete_this) + " "
                                        + getApplicationContext().getString(R.string.marked_schedules),
                                R.layout.dialog_replace);
                        replaceDialog.show(getSupportFragmentManager(), DELETE_SEQUENCES_TAG);*/
                        markedActivities.get(weekdaySelected)[index] = true;
                        progress.setMarkedActivities(markedActivities);
                    }
                    else {
                        ImageView iv = (ImageView) Wlayout.getChildAt(getViewIndex(v));
                        List<Pictogram> pics = weekdaySequences.get(weekdaySelected).getMediaFrame(index).getContent(); // check for size
                        if (pics.size() > 1) {
                            int xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
                            BitmapDrawable bitDraw = new BitmapDrawable(getResources(),ConstructWhiteBackground(BitmapFactory.decodeResource(getResources(), R.drawable.icon_choose)));
                            Drawable resizedDrawable = resizeDrawable(bitDraw, xy, xy);
                            iv.setImageDrawable(resizedDrawable);
                            markedActivities.get(weekdaySelected)[index] = false;
                            progress.setMarkedActivities(markedActivities);
                        } else {
                            int xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
                            BitmapDrawable bitDraw = new BitmapDrawable(getResources(), ConstructWhiteBackground(pics.get(0).getImageData()));
                            Drawable resizedDrawable = resizeDrawable(bitDraw, xy, xy);
                            iv.setImageDrawable(resizedDrawable);
                            markedActivities.get(weekdaySelected)[index] = false;
                            progress.setMarkedActivities(markedActivities);
                        }
                    }
                // longclick has the functionality to remove a selected pictogram if in edit mode
                }else
                {
                    int position = getViewIndex(v);
                    Wlayout.removeView(v);
                    weekdaySequences.get(weekdaySelected).getMediaFrames().remove(position);
                }
                return true;
            }
        });

        iw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // index of pictogram being clicked
                int index = getViewIndex(v);

                // update weekdaySelected
                determineWeekSection(v);

                // show there is more than one choice in view mode
                if (weekdaySequences.get(weekdaySelected).getMediaFrames().get(index).getContent().size() > 1 &&
                        (currentActivity[weekdaySelected] == index-1 || currentActivity[weekdaySelected] == 0)) {
                    showMultiChoiceDialog(index, weekdaySelected, v, ScheduleActivity.this, day);
                }
                // Sets this particular view to be the currant activity
                else {
                    setPictogramSize(index, v, day);
                }
            }
        });

        // add pictogram to week day and make sure the add button is always at the bottom of the week day
        layout.addView(iw); // add new pictogram
    }

    public void setPictogramSize(final int index, View v, final int day){
        LinearLayout dayLayout = (LinearLayout) v.getParent();
        int pictoCount = dayLayout.getChildCount();
        if(index == 0 || (index-1) == currentActivity[weekdaySelected] || (index+1) == currentActivity[weekdaySelected])
        {
            currentActivity[weekdaySelected] = index;
            setPictogramSizes((View) v.getParent());
            progressActivity[0] = day;
            progressActivity[1] = index;
            progress.setProgress(progressActivity);

                        /*Re-size*/
            v.setScaleX(1.2f);
            v.setScaleY(1.2f);
            ImageView iv = (ImageView) dayLayout.getChildAt(index);
            iv.setColorFilter(null);
            iv.setBackgroundColor(Color.TRANSPARENT);

        } else if((index+1) == pictoCount && dayLayout.getChildAt(index).getScaleX() == 1.2f)
        {
            ImageView iv = (ImageView) dayLayout.getChildAt(index);
            progressActivity[1] = -1;
            progress.setProgress(progressActivity);

                        /*Re-size*/
            iv.setScaleY(0.4f);
            iv.setScaleX(0.4f);
                        /*Adding grey scale*/
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            iv.setColorFilter(filter);
            iv.setBackgroundColor(Color.GRAY);
        }
    }
    public void resumeProgress(int day) {
        switch(day) {
            case 0:
                LinearLayout mondayLayout = (LinearLayout) findViewById(R.id.layoutMonday);
                if (progressActivity[1] == -1) {
                    grayPreviousDays(day + 1);
                    currentActivity[day] = 0;
                } else {
                    currentActivity[day] = progressActivity[1] - 1;
                    mondayLayout.getChildAt(progressActivity[1]).performClick();
                }
                break;
            case 1:
                LinearLayout tuesdayLayout = (LinearLayout) findViewById(R.id.layoutTuesday);
                if (progressActivity[1] == -1) {
                    grayPreviousDays(day + 1);
                    currentActivity[day] = 0;
                } else {
                    currentActivity[day] = progressActivity[1] - 1;
                    grayPreviousDays(day);
                    progressActivity[1] = currentActivity[day] + 1;
                    tuesdayLayout.getChildAt(progressActivity[1]).performClick();
                }
                break;
            case 2:
                LinearLayout wednesdayLayout = (LinearLayout) findViewById(R.id.layoutWednesday);
                if (progressActivity[1] == -1) {
                    grayPreviousDays(day + 1);
                    currentActivity[day] = 0;
                } else {
                    currentActivity[day] = progressActivity[1] - 1;
                    grayPreviousDays(day);
                    progressActivity[1] = currentActivity[day] + 1;
                    wednesdayLayout.getChildAt(progressActivity[1]).performClick();
                }
                break;
            case 3:
                LinearLayout thursdayLayout = (LinearLayout) findViewById(R.id.layoutThursday);
                if (progressActivity[1] == -1) {
                    grayPreviousDays(day + 1);
                    currentActivity[day] = 0;
                } else {
                    currentActivity[day] = progressActivity[1] - 1;
                    grayPreviousDays(day);
                    progressActivity[1] = currentActivity[day] + 1;
                    thursdayLayout.getChildAt(progressActivity[1]).performClick();
                }
                break;
            case 4:
                LinearLayout fridayLayout = (LinearLayout) findViewById(R.id.layoutFriday);
                if (progressActivity[1] == -1) {
                    grayPreviousDays(day + 1);
                    currentActivity[day] = 0;
                } else {
                    currentActivity[day] = progressActivity[1] - 1;
                    grayPreviousDays(day);
                    progressActivity[1] = currentActivity[day] + 1;
                    fridayLayout.getChildAt(progressActivity[1]).performClick();
                }
                break;
            case 5:
                LinearLayout saturdayLayout = (LinearLayout) findViewById(R.id.layoutSaturday);
                if (progressActivity[1] == -1) {
                    grayPreviousDays(day + 1);
                    currentActivity[day] = 0;
                } else {
                    currentActivity[day] = progressActivity[1] - 1;
                    grayPreviousDays(day);
                    progressActivity[1] = currentActivity[day] + 1;
                    saturdayLayout.getChildAt(progressActivity[1]).performClick();
                }
                break;
            case 6:
                LinearLayout sundayLayout = (LinearLayout) findViewById(R.id.layoutSunday);
                if (progressActivity[1] == -1) {
                    grayPreviousDays(day + 1);
                    currentActivity[day] = 0;
                } else {
                    currentActivity[day] = progressActivity[1] - 1;
                    grayPreviousDays(day);
                    progressActivity[1] = currentActivity[day] + 1;
                    sundayLayout.getChildAt(progressActivity[1]).performClick();
                }
                break;
        }
    }


    private void grayPreviousDays(int days) {
        int tempCurrentActivity = currentActivity[days];
        for (int i = 0; i < days; i++) {
            switch(i) {
                case 0:
                    LinearLayout mondayLayout = (LinearLayout) findViewById(R.id.layoutMonday);
                    currentActivity[i] = mondayLayout.getChildCount();
                    setPictogramSizes(mondayLayout);
                    if (currentActivity[i] != 0) {
                        mondayLayout.getChildAt(mondayLayout.getChildCount() - 1).performClick();
                        mondayLayout.getChildAt(mondayLayout.getChildCount() - 1).performClick();
                    }
                    break;
                case 1:
                    LinearLayout tuesdayLayout = (LinearLayout) findViewById(R.id.layoutTuesday);
                    currentActivity[i] = tuesdayLayout.getChildCount();
                    setPictogramSizes(tuesdayLayout);
                    if (currentActivity[i] != 0) {
                        tuesdayLayout.getChildAt(tuesdayLayout.getChildCount() - 1).performClick();
                        tuesdayLayout.getChildAt(tuesdayLayout.getChildCount() - 1).performClick();
                    }
                    break;
                case 2:
                    LinearLayout wednesdayLayout = (LinearLayout) findViewById(R.id.layoutWednesday);
                    currentActivity[i] = wednesdayLayout.getChildCount();
                    setPictogramSizes(wednesdayLayout);
                    if (currentActivity[i] != 0) {
                        wednesdayLayout.getChildAt(wednesdayLayout.getChildCount() - 1).performClick();
                        wednesdayLayout.getChildAt(wednesdayLayout.getChildCount() - 1).performClick();
                    }
                    break;
                case 3:
                    LinearLayout thursdayLayout = (LinearLayout) findViewById(R.id.layoutThursday);
                    currentActivity[i] = thursdayLayout.getChildCount();
                    setPictogramSizes(thursdayLayout);
                    if (currentActivity[i] != 0) {
                        thursdayLayout.getChildAt(thursdayLayout.getChildCount() - 1).performClick();
                        thursdayLayout.getChildAt(thursdayLayout.getChildCount() - 1).performClick();
                    }
                    break;
                case 4:
                    LinearLayout fridayLayout = (LinearLayout) findViewById(R.id.layoutFriday);
                    currentActivity[i] = fridayLayout.getChildCount();
                    setPictogramSizes(fridayLayout);
                    if (currentActivity[i] != 0) {
                        fridayLayout.getChildAt(fridayLayout.getChildCount() - 1).performClick();
                        fridayLayout.getChildAt(fridayLayout.getChildCount() - 1).performClick();
                    }
                    break;
                case 5:
                    LinearLayout saturdayLayout = (LinearLayout) findViewById(R.id.layoutSaturday);
                    currentActivity[i] = saturdayLayout.getChildCount();
                    setPictogramSizes(saturdayLayout);
                    if (currentActivity[i] != 0) {
                        saturdayLayout.getChildAt(saturdayLayout.getChildCount() - 1).performClick();
                        saturdayLayout.getChildAt(saturdayLayout.getChildCount() - 1).performClick();
                    }
                    break;
                case 6:
                    LinearLayout sundayLayout = (LinearLayout) findViewById(R.id.layoutSunday);
                    currentActivity[i] = sundayLayout.getChildCount();
                    setPictogramSizes(sundayLayout);
                    if (currentActivity[i] != 0) {
                        sundayLayout.getChildAt(sundayLayout.getChildCount() - 1).performClick();
                        sundayLayout.getChildAt(sundayLayout.getChildCount() - 1).performClick();
                    }
                    break;
            }
        }
    currentActivity[days] = tempCurrentActivity;
    }

    private void setPictogramSizes(View v) {
        LinearLayout dayLayout = (LinearLayout) v;
        int pictoCount = dayLayout.getChildCount();
        for (int i = 0; i < pictoCount; i++) {
            ImageView iv = (ImageView) dayLayout.getChildAt(i);

            if(i < currentActivity[weekdaySelected]){
                /*Re-Size*/
                iv.setScaleY(0.4f);
                iv.setScaleX(0.4f);

                /*Greyscale*/
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                iv.setColorFilter(filter);
                iv.setBackgroundColor(Color.GRAY);



            }else if(i > currentActivity[weekdaySelected])
            {
                iv.setScaleX(0.8f);
                iv.setScaleY(0.8f);
                iv.setColorFilter(null);
                iv.setBackgroundColor(Color.TRANSPARENT);

            }

            /*
            iv.setBackgroundResource(0);
            iv.setPadding(0, 5, 0, 5);
            */
        }
    }

    public void setMarks(){

        for(int i = 0; i < 7; i++) {
            RelativeLayout v = (RelativeLayout) level1.getChildAt(i); // the +1 is to choose the element at depth 2
            ScrollView level2 = (ScrollView) v.getChildAt(1);
            LinearLayout level3 = (LinearLayout) level2.getChildAt(0);
            for(int index = 0; index < weekdaySequences.get(i).getMediaFrames().size(); index++) {
                if (markedActivities.get(i)[index]) {
                    // this adds the cancel image on top of the original drawable of the pictogram
                    ImageView iv = (ImageView) level3.getChildAt(index);
                    Resources r = getResources();
                    Drawable[] dlayers = new Drawable[2];
                    dlayers[0] = iv.getDrawable();
                    int xy = getResources().getInteger(R.dimen.weekschedule_picto_xy_landscape);
                    dlayers[1] = resizeDrawable(r.getDrawable(R.drawable.cancel_button), xy, xy);
                    LayerDrawable layerDrawable = new LayerDrawable(dlayers);
                    iv.setImageDrawable(layerDrawable);
                }
            }
        }
    }

    public int getViewIndex(View v)
    {
        int index;

        if(((LinearLayout) v.getParent()).getChildCount() > 0)
        {
            index = ((LinearLayout) v.getParent()).indexOfChild(v);
        }
        else
        {
            index = -1;
        }

        return index;
    }

    public void showAddButtons()
    {
        LinearLayout level1 = (LinearLayout) findViewById(R.id.completeWeekLayout);

        int childcount = level1.getChildCount();

        // find each of the individual week days
        for (int i = 0; i < childcount; i++)
        {
            try
            {
                // TODO: fix hardcoding of 1
                RelativeLayout v = (RelativeLayout) level1.getChildAt(i); // the +1 is to choose the element at depth 2
                ScrollView level2 = (ScrollView) v.getChildAt(1);
                LinearLayout level3 = (LinearLayout) level2.getChildAt(0);
                level3.addView(addButton());
            }catch (Exception ex)
            {
                GuiHelper.ShowToast(this, "Der skete en fejl");
            }

        }
    }

    public void determineWeekSection(View v)
    {
        int weekdayId = v.getId();
        LinearLayout layout;

        switch (weekdayId)
        {
            case R.id.sectionMonday:
                layout = (LinearLayout) findViewById(R.id.layoutMonday);
                ScheduleEditActivity.weekdayLayout = layout;
                weekdaySelected = Day.MONDAY.ordinal();
                break;
            case R.id.sectionTuesday:
                layout = (LinearLayout) findViewById(R.id.layoutTuesday);
                ScheduleEditActivity.weekdayLayout = layout;
                weekdaySelected = Day.TUESDAY.ordinal();
                break;
            case R.id.sectionWednesday:
                layout = (LinearLayout) findViewById(R.id.layoutWednesday);
                ScheduleEditActivity.weekdayLayout = layout;
                weekdaySelected = Day.WEDNESDAY.ordinal();
                break;
            case R.id.sectionThursday:
                layout = (LinearLayout) findViewById(R.id.layoutThursday);
                ScheduleEditActivity.weekdayLayout = layout;
                weekdaySelected = Day.THURSDAY.ordinal();
                break;
            case R.id.sectionFriday:
                layout = (LinearLayout) findViewById(R.id.layoutFriday);
                ScheduleEditActivity.weekdayLayout = layout;
                weekdaySelected = Day.FRIDAY.ordinal();
                break;
            case R.id.sectionSaturday:
                layout = (LinearLayout) findViewById(R.id.layoutSaturday);
                ScheduleEditActivity.weekdayLayout = layout;
                weekdaySelected = Day.SATURDAY.ordinal();
                break;
            case R.id.sectionSunday:
                layout = (LinearLayout) findViewById(R.id.layoutSunday);
                ScheduleEditActivity.weekdayLayout = layout;
                weekdaySelected = Day.SUNDAY.ordinal();
                break;
            default:
                determineWeekSection((View) v.getParent());
                break;
        }
    }
    public void currentWeekSection(View v)
    {
        int weekdayId = v.getId();

        switch (weekdayId)
        {
            case R.id.sequenceViewGroupMon:
                weekdaySelected = Day.MONDAY.ordinal();
                break;
            case R.id.sequenceViewGroup2:
                weekdaySelected = Day.TUESDAY.ordinal();
                break;
            case R.id.sequenceViewGroup3:
                weekdaySelected = Day.WEDNESDAY.ordinal();
                break;
            case R.id.sequenceViewGroup4:
                weekdaySelected = Day.THURSDAY.ordinal();
                break;
            case R.id.sequenceViewGroup5:
                weekdaySelected = Day.FRIDAY.ordinal();
                break;
            case R.id.sequenceViewGroup6:
                weekdaySelected = Day.SATURDAY.ordinal();
                break;
            case R.id.sequenceViewGroup7:
                weekdaySelected = Day.SUNDAY.ordinal();
                break;
            default:
                determineWeekSection((View) v.getParent());
                break;
        }
    }

    public void markCurrentWeekday()
    {
        unmarkWeekdays();

        String weekday = getWeekday();

        if(weekday.equals(getResources().getString(R.string.monday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.monday);
            btn.setToggled(false);

            // this variable is for knowing which linear layout in the scroll view in ScheduleEditActivity
            // to put pictogram in
            // TODO: ugly workaround.. should be fixed
            ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutMonday);
            LinearLayout rl = (LinearLayout) findViewById(R.id.border_monday);
            rl.setBackgroundResource(R.layout.weekday_selected);
            currentWeekday = 0;
        }else if(weekday.equals(getResources().getString(R.string.tuesday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.tuesday);
            btn.setToggled(false);

            // TODO: ugly workaround.. should be fixed
            ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutTuesday);
            LinearLayout rl = (LinearLayout) findViewById(R.id.border_tuesday);
            rl.setBackgroundResource(R.layout.weekday_selected);
            currentWeekday = 1;
        }else if(weekday.equals(getResources().getString(R.string.wednesday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.wednesday);
            btn.setToggled(false);

            // TODO: ugly workaround.. should be fixed
            ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutWednesday);

            // highlights current day with a border
            // TODO: put this in method
            LinearLayout rl = (LinearLayout) findViewById(R.id.border_wednesday);
            rl.setBackgroundResource(R.layout.weekday_selected);
            currentWeekday = 2;
        }else if(weekday.equals(getResources().getString(R.string.thursday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.thursday);
            btn.setToggled(false);

            // TODO: ugly workaround.. should be fixed
            ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutThursday);
            LinearLayout rl = (LinearLayout) findViewById(R.id.border_thursday);
            rl.setBackgroundResource(R.layout.weekday_selected);
            currentWeekday = 3;
        }else if(weekday.equals(getResources().getString(R.string.friday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.friday);
            btn.setToggled(false);

            // TODO: ugly workaround.. should be fixed
            ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutFriday);
            LinearLayout rl = (LinearLayout) findViewById(R.id.border_friday);
            rl.setBackgroundResource(R.layout.weekday_selected);
            currentWeekday = 4;
        }else if(weekday.equals(getResources().getString(R.string.saturday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.saturday);
            btn.setToggled(false);

            // TODO: ugly workaround.. should be fixed
            ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutSaturday);
            LinearLayout rl = (LinearLayout) findViewById(R.id.border_saturday);
            rl.setBackgroundResource(R.layout.weekday_selected);
            currentWeekday = 5;
        }else if(weekday.equals(getResources().getString(R.string.sunday)))
        {
            GToggleButton btn = (GToggleButton) findViewById(R.id.sunday);
            btn.setToggled(false);

            // TODO: ugly workaround.. should be fixed
            ScheduleEditActivity.weekdayLayout = (LinearLayout) findViewById(R.id.layoutSunday);
            LinearLayout rl = (LinearLayout) findViewById(R.id.border_sunday);
            rl.setBackgroundResource(R.layout.weekday_selected);
            currentWeekday = 6;
        }
    }

    public String getWeekday()
    {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEE");
        String weekday = dateFormatter.format(date);

        // return week day with first letter as uppercase - e.g Mandag
        return weekday.substring(0, 1).toUpperCase() + weekday.substring(1);
    }

    private void unmarkWeekdays()
    {
        GToggleButton btn;

        try
        {
            btn = (GToggleButton) findViewById(R.id.monday);
            btn.setToggled(true);
            btn = (GToggleButton) findViewById(R.id.tuesday);
            btn.setToggled(true);
            btn = (GToggleButton) findViewById(R.id.wednesday);
            btn.setToggled(true);
            btn = (GToggleButton) findViewById(R.id.thursday);
            btn.setToggled(true);
            btn = (GToggleButton) findViewById(R.id.friday);
            btn.setToggled(true);
            btn = (GToggleButton) findViewById(R.id.saturday);
            btn.setToggled(true);
            btn = (GToggleButton) findViewById(R.id.sunday);
            btn.setToggled(true);
        }catch (NullPointerException ex)
        {
            // the exception is ignored because it is thrown when using portrait mode
            // the exception is a work-around
        }
    }

    //TODO move common methods here
    public enum Day
    {
        MONDAY, TUESDAY, WEDNESDAY,
        THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}
