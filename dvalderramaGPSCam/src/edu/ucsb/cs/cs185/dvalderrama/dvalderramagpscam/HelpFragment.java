package edu.ucsb.cs.cs185.dvalderrama.dvalderramagpscam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class HelpFragment extends DialogFragment{
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.alert_help).setMessage(R.string.help_message)
	    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dismiss();
			}
	    });
	    return builder.create();
	}
}	
