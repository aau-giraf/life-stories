package dk.aau.cs.giraf.tortoise;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ChoiceDialogFragment extends DialogFragment {
	
	private int index = 0;
	
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int choice);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	
	NoticeDialogListener mListener;

    /**
     * Static constructor that returns an instance of this class
     * @param numChoices
     * @return
     */
	public static ChoiceDialogFragment newInstance(int numChoices) {
        ChoiceDialogFragment frag = new ChoiceDialogFragment();
        Bundle args = new Bundle();
        args.putInt("numChoices", numChoices);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int numChoices = getArguments().getInt("numChoices");
		CharSequence[] choices = new CharSequence[numChoices];
		index = 0;
		for (int i=0; i < numChoices ; i++) {
			choices[i] = "Valg " + (i + 1);
		}
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Tilføj til valg")
        	.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ChoiceDialogFragment.this.index = which + 1;
					
				}
			})
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (ChoiceDialogFragment.this.index != 0)
	            		   mListener.onDialogPositiveClick(ChoiceDialogFragment.this, ChoiceDialogFragment.this.index);
	               }
	        })
	        .setNegativeButton("Afslut", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	               }
	        });
			
        return builder.create();
	}

}
