package com.cimraffi.android.birdeye.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import com.cimraffi.android.birdeye.R;

public class SelectDialog extends Dialog {

    private Context context;
    private Button btn_one, btn_two;
    private Activity activity;

    public SelectDialog(Context context) {
        this(context, R.style.CustomDialog);
    }

    public SelectDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        setHelperOwnerActivity();
        initView();
    }

    private void setHelperOwnerActivity() {
        if (context != null) {
            try {
                activity = (Activity) context;
            } catch (Exception e) {
                activity = null;
                e.printStackTrace();
            }
        }
        if (activity != null) {
            setOwnerActivity(activity);
        }
    }

    private void initView() {
        View view = View.inflate(context, R.layout.dialog_btn2, null);
        setContentView(view);

        btn_one = (Button) view.findViewById(R.id.btn_one);
        btn_two = (Button) view.findViewById(R.id.btn_two);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    /**
     * 设置是否可以取消，默认可以取消
     *
     * @param cancelable
     * @return
     */
    public void setCancel(boolean cancelable) {
        this.setCancelable(cancelable);
    }


    /**
     * 添加按钮，最多3个，按顺序排列，如不添加则没有按钮
     *
     * @param listener
     * @return
     */
    public void addButton(String one, String two, DialogButtonClickListener listener) {
        btn_one.setText(one);
        btn_one.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.oneClick();
                }
            }
        });
        btn_two.setText(two);
        btn_two.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.twoClick();
                }
            }
        });
    }

    @Override
    public void show() {
        if (activity != null && !activity.isFinishing() && !isShowing()) {
            super.show();
        }
    }

    @Override
    public void dismiss() {
        if (activity != null && !activity.isFinishing() && isShowing()) {
            super.dismiss();
        }
    }

    /**
     * 对话框按钮点击监听
     */
    public interface DialogButtonClickListener {
        public void oneClick();

        public void twoClick();
    }
}
