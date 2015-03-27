package dk.aau.cs.giraf.tortoise.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.tortoise.R;

/**
 * Creates a fragment of a friday from the layout file friday.xml
 */
public class FridayFragment extends Fragment {

    /**
     * Returns an instance of this fragment, to use in a ViewPager
     * @return an instance of the FridayFragment
     */
    public static FridayFragment newInstance() {
        return new FridayFragment();
    }

    //Creates the view of a friday from the layout file friday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.friday, container, false);

        return rootView;
    }
}