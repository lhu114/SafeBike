package com.safering.safebike.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;

/**
 * Created by park on 2015-11-01.
 */
public class LicenseDialogFragment extends DialogFragment{
    private TextView btnPositive;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (getActivity()).getLayoutInflater();


        View contentView = inflater.inflate(R.layout.custom_dialog_license, null);


        builder.setView(contentView);


        btnPositive = (TextView) contentView.findViewById(R.id.btn_custom_dialog_license);
        btnPositive.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }

        });


        return builder.create();
    }
}
