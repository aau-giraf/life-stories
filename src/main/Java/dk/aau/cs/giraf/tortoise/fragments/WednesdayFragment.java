package dk.aau.cs.giraf.tortoise.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.tortoise.R;

/**
 * Creates a fragment of a wednesday from the layout file wednesday.xml
 */
public class WednesdayFragment extends Fragment {

    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the WednesdayFragment
     */
    public static WednesdayFragment newInstance() {
        return new WednesdayFragment();
    }

    //Creates a view of a wednesday, using the layout file wednesday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.wednesday, container, false);

        return rootView;
    }
}