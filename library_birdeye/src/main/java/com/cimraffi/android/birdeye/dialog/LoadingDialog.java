package com.cimraffi.android.birdeye.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.cimraffi.android.birdeye.R;

public class LoadingDialog extends Dialog {
    private AnimationDrawable spinner;
    private String msg;

    public LoadingDialog(Context context) {
        super(context, R.style.CustomDialog);
    }

    public LoadingDialog(Context context, String text) {
        super(context, R.style.CustomDialog);
        this.msg = text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCanceledOnTouchOutside(false);//单击dialog之外的地方，不可以dismiss掉dialog
        ImageView spinnerView = (ImageView) findViewById(R.id.spinnerImageView);
        spinner = (AnimationDrawable) spinnerView.getBackground();
        spinner.start();
        TextView tv_msg = (TextView) findViewById(R.id.message);
        if (!TextUtils.isEmpty(msg)) {
            tv_msg.setVisibility(View.VISIBLE);
            tv_msg.setText(msg);
        } else {
            tv_msg.setVisibility(View.GONE);
        }
    }

    @Override
    public void show() {
        try {
            if (!isShowing()) {
                super.show();
            }
        } catch (Exception e) {
        }

    }

    @Override
    public void dismiss() {
        try {
            if (isShowing()) {
                if (spinner != null) {
                    spinner.stop();
                    spinner = null;
                }
                super.dismiss();
            }
        } catch (Exception e) {
        }
    }

}
