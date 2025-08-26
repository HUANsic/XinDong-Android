package com.touchmcu.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * 对话框工具类
 */
public class DialogUtil {
    
    private static DialogUtil instance;
    private Dialog loadingDialog;
    
    private DialogUtil() {}
    
    public static DialogUtil getInstance() {
        if (instance == null) {
            instance = new DialogUtil();
        }
        return instance;
    }
    
    /**
     * 显示简单对话框
     */
    public void showSimpleDialog(Context context, String message, String buttonText, final IResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (result != null) {
                    result.onContinue();
                }
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    
    /**
     * 显示确认对话框
     */
    public void showConfirmDialog(Context context, String title, String message, 
                                 String positiveText, String negativeText, final IResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null) {
            builder.setTitle(title);
        }
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (result != null) {
                    result.onContinue();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (result != null) {
                    result.onCancel();
                }
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    
    /**
     * 显示加载对话框
     */
    public void showLoadingDialog(Context context, String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(message);
        builder.setView(view);
        builder.setCancelable(false);
        
        loadingDialog = builder.create();
        loadingDialog.show();
    }
    
    /**
     * 隐藏加载对话框
     */
    public void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
    
    /**
     * 显示断开连接对话框
     */
    public void showDisconnectDialog(Context context, final IDisconnectResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("断开连接");
        builder.setMessage("是否断开当前蓝牙连接？");
        builder.setPositiveButton("断开", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (result != null) {
                    result.onDisconnect();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (result != null) {
                    result.onCancel();
                }
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    
    /**
     * 对话框结果回调接口
     */
    public interface IResult {
        void onContinue();
        void onCancel();
    }
    
    /**
     * 断开连接对话框结果回调接口
     */
    public interface IDisconnectResult {
        void onDisconnect();
        void onCancel();
    }
}
