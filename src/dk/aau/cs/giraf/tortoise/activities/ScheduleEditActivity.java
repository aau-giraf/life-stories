package dk.aau.cs.giraf.tortoise.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.R;
import dk.aau.cs.giraf.tortoise.controller.Sequence;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;

public class ScheduleEditActivity extends ScheduleActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_edit_activity);

        // Get intent, action and MIME type
        Intent intent = getIntent();

        if (intent.getExtras() == null)
        {
            GuiHelper.ShowToast(this, "Ingen data modtaget fra Tortoise");
            finish();
        }

        //ListHandler lh = new ListHandler();
        //lh.ilmerscorner();

        /*
        setContentView(R.layout.schedule_edit_activity);

        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);*/

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

class ListHandler extends ScheduleEditActivity
{
    private ArrayAdapter<String> listAdapter;

    public void ilmerscorner()
    {
        GuiHelper.ShowToast(this, "..");
        try{
            ListView lv = (ListView) findViewById(R.id.listView);

            // Create and populate a List of planet names.
            String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",
                    "Jupiter", "Saturn", "Uranus", "Neptune"};

            ArrayList<String> planetList = new ArrayList<String>();
            planetList.addAll(Arrays.asList(planets));


            // Create ArrayAdapter using the planet list.
            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList);

            // Add more planets. If you passed a String[] instead of a List<String>
            // into the ArrayAdapter constructor, you must not add more items.
            // Otherwise an exception will occur.
            listAdapter.add( "Ceres" );
            listAdapter.add( "Pluto" );
            listAdapter.add( "Haumea" );
            listAdapter.add( "Makemake" );
            listAdapter.add( "Eris" );

            // Set the ArrayAdapter as the ListView's adapter.
            lv.setAdapter( listAdapter );
        } catch (Exception ex)
        {
            GuiHelper.ShowToast(this, ex.toString());
        }
    }
}
