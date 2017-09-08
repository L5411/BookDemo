package com.l_5411.bookdemo.chapter_8;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.l_5411.bookdemo.R;

import java.net.URL;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class Chapter8Activity extends AppCompatActivity {

    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, Chapter8Activity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter8);
        mContext = this;
        ButterKnife.bind(this);

    }

    @OnClick({R.id.add_window})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_window:
                addWindow();
                break;
            default:
                break;
        }
    }

    private void addWindow() {
        final Button mFloatingButton = new Button(mContext);
        mFloatingButton.setText("Button");
        final LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                0,
                0,
                PixelFormat.TRANSPARENT
        );
        mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        mLayoutParams.x = 100;
        mLayoutParams.y = 300;

        mFloatingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        mLayoutParams.x = rawX;
                        mLayoutParams.y = rawY;
                        getWindowManager().updateViewLayout(mFloatingButton, mLayoutParams);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        getWindowManager().addView(mFloatingButton, mLayoutParams);
    }

}
