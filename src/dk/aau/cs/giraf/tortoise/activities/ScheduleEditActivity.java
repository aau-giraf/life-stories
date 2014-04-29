package dk.aau.cs.giraf.tortoise.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import dk.aau.cs.giraf.gui.GToggleButton;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;

public class ScheduleEditActivity extends ScheduleActivity
{
    Boolean deviceInPortraitMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // check whether tablet is in portrait or landscape mode and set the layout accordingly
        // landscape mode shows mode days than portrait mode
        int screenOrientation = getResources().getConfiguration().orientation;
        if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            setContentView(R.layout.schedule_edit_activity);
        }
        else
        {
            setContentView(R.layout.schedule_edit_activity_portrait);
        }

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }
    }

    public void weekdaySelected(View v)
    {
        // pushing a toggle button has no effect
        GToggleButton btn = (GToggleButton) findViewById(v.getId());
        btn.setToggled(true);
    }

    public List<Sequence> getPictograms()
    {
        List<Sequence> pictogramSequence = new List<Sequence>() {

            @Override
            public void add(int i, Sequence sequence) {

            }

            @Override
            public boolean add(Sequence sequence) {
                return false;
            }

            @Override
            public boolean addAll(int i, Collection<? extends Sequence> sequences) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends Sequence> sequences) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> objects) {
                return false;
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public Sequence get(int i) {
                return null;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public Iterator<Sequence> iterator() {
                return null;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<Sequence> listIterator() {
                return null;
            }

            @Override
            public ListIterator<Sequence> listIterator(int i) {
                return null;
            }

            @Override
            public Sequence remove(int i) {
                return null;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> objects) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> objects) {
                return false;
            }

            @Override
            public Sequence set(int i, Sequence sequence) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public List<Sequence> subList(int i, int i2) {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }
        };



        return pictogramSequence;
    }
}