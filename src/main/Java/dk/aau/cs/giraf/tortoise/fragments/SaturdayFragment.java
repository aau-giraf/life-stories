package dk.aau.cs.giraf.tortoise.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.tortoise.R;

/**
 * Creates a fragment of a saturday from the layout file saturday.xml
 */
public class SaturdayFragment extends Fragment {

    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the SaturdayFragment
     */
    public static SaturdayFragment newInstance() {
        return new SaturdayFragment();
    }

    //Creates a view of a saturday, using the layout file saturday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.saturday, container, false);

        return rootView;
    }
}