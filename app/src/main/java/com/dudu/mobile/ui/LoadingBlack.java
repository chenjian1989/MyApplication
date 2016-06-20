
package com.dudu.mobile.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;

import com.dudu.mobile.R;

public class LoadingBlack extends Dialog {
    private Context context;

    /**
     * 提示信息请不要太长，最多四个字 ！
     */
    private String msg;

    private GifView gf1;

    private TextView tv;

    public LoadingBlack(Context context) {
        super(context);
        this.context = context;

        this.setCancelable(true);
    }

    public LoadingBlack(Context context, int theme, String msg) {
        super(context, theme);
        this.context = context;
        this.msg = msg;

        this.setCancelable(true);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_progress);
        tv = (TextView) findViewById(R.id.msg);
        tv.setText(msg);
    }

    public void onStart() {
        gf1 = (GifView) findViewById(R.id.gif);
        gf1.setGifImage(R.drawable.loading);
        Resources r = context.getResources();
        gf1.setShowDimension(r.getDimensionPixelSize(R.dimen.bigloading_w_h),
                r.getDimensionPixelSize(R.dimen.bigloading_w_h));
    }

    public void dismiss() {
        super.dismiss();
        if (gf1 != null) {
            gf1.reCircle();
            gf1 = null;
        }
    }
}
