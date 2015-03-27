package dk.aau.cs.giraf.tortoise.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.tortoise.R;

/**
 * Creates a fragment of a sunday from the layout file sunday.xml
 */
public class SundayFragment extends Fragment {

    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the SundayFragment
     */
    public static SundayFragment newInstance() {
        return new SundayFragment();
    }

    //Creates a view of a sunday, using the layout file sunday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.sunday, container, false);

        return rootView;
    }
}