package com.cimraffi.android.birdeye.dialog;

import android.content.Context;
import android.view.View;
import com.cimraffi.android.birdeye.R;

public class DialogUtil {

    public static void showAlertDialog(Context context, String message) {
        final CustomDialog dialog = new CustomDialog(context);
        dialog.setMessage(message);
        dialog.setCancel(false);
        dialog.addButton(
                context.getResources().getString(R.string.confirm), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
        dialog.show();
    }

    public static void showAlertDialog(Context context, String message, View.OnClickListener listener) {
        final CustomDialog dialog = new CustomDialog(context);
        dialog.setMessage(message);
        dialog.setCancel(false);
        dialog.addButton(
                context.getResources().getString(R.string.confirm), listener);
        dialog.show();
    }

    public static LoadingDialog showLoadingDialog(Context context,String msg){
        LoadingDialog dialog = new LoadingDialog(context,msg);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

}
