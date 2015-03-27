package dk.aau.cs.giraf.tortoise.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.tortoise.R;

/**
 * Creates a fragment of a tuesday from the layout file tuesday.xml
 */
public class TuesdayFragment extends Fragment {

    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the TuesdayFragment
     */
    public static TuesdayFragment newInstance() {
        return new TuesdayFragment();
    }

    //Creates a view of a tuesday, using the layout file tuesday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.tuesday, container, false);

        return rootView;
    }
}