package dk.aau.cs.giraf.tortoise.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dk.aau.cs.giraf.tortoise.R;

/**
 * Creates a fragment of a monday from the layout file monday.xml
 */
public class MondayFragment extends Fragment {

    /**
     * Returns an instance of this fragment, to be used in a ViewPager
     * @return an instance of the MondayFragment
     */
    public static MondayFragment newInstance() {
        return new MondayFragment();
    }

    //Creates the view of a monday, using the layout file monday.xml
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup View = (ViewGroup) inflater.inflate(
                R.layout.monday, container, false);

        return View;
    }
}
