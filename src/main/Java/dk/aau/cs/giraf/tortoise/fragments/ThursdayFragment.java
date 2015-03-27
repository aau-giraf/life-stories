package dk.aau.cs.giraf.tortoise.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.tortoise.R;

/**
 * Creates a fragment of a thursday from the layout file thursday.xml
 */
public class ThursdayFragment extends Fragment {

    /**
     * Returns an instance of this fragment, to be used in e ViewPager
     * @return an instance of the ThursdayFragment
     */
    public static ThursdayFragment newInstance() {
        return new ThursdayFragment();
    }

    //Creates a view of a thursday, using the layout file thursday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.thursday, container, false);

        return rootView;
    }
}