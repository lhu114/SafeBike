package com.safering.safebike.login;

/**
 * Created by Tacademy on 2015-10-29.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;

public class LoginFailDialogFragment extends DialogFragment{
    //로그인 실패시 보이는 다이얼로그
    private String title = "";
    private String content = "";
    private Button btnPositive;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = ((LoginActivity)getActivity()).getLayoutInflater();

        View contentView = inflater.inflate(R.layout.custom_dialog_content,null);

        TextView textContent = (TextView)contentView.findViewById(R.id.text_custom_dialog_content);
        textContent.setText(content);
        textContent.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));

        textContent = (TextView)contentView.findViewById(R.id.text_custom_dialog_title);
        textContent.setText(title);
        textContent.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        builder.setView(contentView);
        btnPositive = (Button)contentView.findViewById(R.id.btn_custom_dialog_positive);
        btnPositive.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

/*
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });*/

        //alert.getWindow().setBackgroundDrawableResource(R.color.colorNaviHeader);

        return builder.create();
    }

    public void setContent(String title,String content){
        this.title = title;
        this.content = content;

    }


}
