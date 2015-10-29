package com.safering.safebike.login;

/**
 * Created by Tacademy on 2015-10-29.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class LoginFailDialogFragment extends DialogFragment{
    //로그인 실패시 보이는 다이얼로그

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("Dialog Fragment");
        builder.setMessage("LoginFail");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Yes Click", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }
}
