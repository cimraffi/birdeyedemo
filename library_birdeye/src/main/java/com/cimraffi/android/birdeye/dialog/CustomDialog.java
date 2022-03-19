package com.cimraffi.android.birdeye.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.cimraffi.android.birdeye.R;

import com.cimraffi.android.birdeye.utils.DensityUtils;

public class CustomDialog extends Dialog {

    private Context context;
    private TextView tv_title;//标题
    private ImageView iv_icon;
    private EditText et_message, et_submessage;
    private Button bt_left, bt_right;
    private int pxOf10pid;
    private Activity activity;
    private View line1;

    public CustomDialog(Context context) {
        this(context, R.style.CustomDialog);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        pxOf10pid = DensityUtils.dip2px(context, 10);
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
        View view = View.inflate(context, R.layout.dialog_custom, null);
        setContentView(view);

        bt_left = (Button) view.findViewById(R.id.bt_left);
        bt_right = (Button) view.findViewById(R.id.bt_right);
        //标题
        tv_title = (TextView) view.findViewById(R.id.tv_title);

        iv_icon = (ImageView) view.findViewById(R.id.iv_icon);

        et_message = (EditText) view.findViewById(R.id.et_message);

        et_submessage = (EditText) view.findViewById(R.id.et_submessage);

        line1 = (View) view.findViewById(R.id.v_line1);

        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int height = metric.heightPixels * 1 / 2;   // 屏幕高度（像素）*1/2
        et_message.setMaxHeight(height);
        setCanceledOnTouchOutside(false);
    }

    /**
     * 更新对话框内容
     *
     * @param messge
     */
    public void updateMessage(String messge) {
        if (this.isShowing()) {
            et_message.setText(messge);
        }
    }

    /**
     * 设置标题
     *
     * @param title
     * @return
     */
    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            tv_title.setVisibility(View.GONE);
        } else {
            tv_title.setText(title);
            tv_title.setVisibility(View.VISIBLE);
            iv_icon.setVisibility(View.GONE);
        }
    }

    public void setIcon() {
        tv_title.setVisibility(View.GONE);
        iv_icon.setVisibility(View.VISIBLE);
    }

    public void setIcon(int id) {
        tv_title.setVisibility(View.GONE);
        iv_icon.setVisibility(View.VISIBLE);
        iv_icon.setBackgroundResource(id);
    }

    /**
     * 设置内容
     *
     * @param message
     * @return
     */
    public void setMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            et_message.setVisibility(View.GONE);
        } else {
            et_message.setText(message);
            et_message.setVisibility(View.VISIBLE);
        }
    }

    public void setSubMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            et_submessage.setVisibility(View.GONE);
        } else {
            et_submessage.setText(message);
            et_submessage.setVisibility(View.VISIBLE);
        }
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
    public void addButton(String left, View.OnClickListener listener) {
        if (!TextUtils.isEmpty(left)) {
            bt_left.setText(left);
            bt_left.setTextColor(context.getResources().getColor(R.color.color_green));
            bt_left.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    if (listener != null) {
                        listener.onClick(v);
                    }
                }
            });
        }
    }

    /**
     * 添加按钮，最多3个，按顺序排列，如不添加则没有按钮
     *
     * @param listener
     * @return
     */
    public void addButton(String left, String right, DialogButtonClickListener listener) {
        if (!TextUtils.isEmpty(left)) {
            bt_left.setText(left);
            if (TextUtils.isEmpty(right)) {
                bt_left.setTextColor(context.getResources().getColor(R.color.color_green));
            } else {
                bt_left.setTextColor(context.getResources().getColor(R.color.text_default));
            }
            bt_left.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    if (listener != null) {
                        listener.leftClick();
                    }
                }
            });
        }
        if (!TextUtils.isEmpty(right)) {
            bt_right.setVisibility(View.VISIBLE);
            bt_right.setText(right);
            bt_right.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!bt_right.getText().toString().equals(context.getResources().getString(R.string.retry))){
                        dismiss();
                    }

                    if (listener != null) {
                        listener.rightClic();
                    }
                }
            });

            bt_left.setBackgroundResource(R.drawable.dialog_left_btn);
            bt_left.setPadding(0, pxOf10pid, 0, pxOf10pid);
            line1.setVisibility(View.VISIBLE);
        }
    }

    public void setRightButtonText(String btnText){
        bt_right.setText(btnText);
    }

    public String getRightButtonText(){
        return bt_right.getText().toString();
    }

    public void setRightButtonClickable(boolean clickable){
        if (clickable){
            bt_right.setClickable(true);
            bt_right.setTextColor(activity.getResources().getColor(R.color.color_green));
        }else {
            bt_right.setClickable(false);
            bt_right.setTextColor(activity.getResources().getColor(R.color.text_gray));
        }
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
        public void leftClick();

        public void rightClic();
    }
}
