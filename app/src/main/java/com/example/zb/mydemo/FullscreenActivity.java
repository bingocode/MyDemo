package com.example.zb.mydemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    private static final String HOSTNMAE_LAB="192.168.1.109";
    private static final String HOSTNMAE_DOM="10.131.245.201";
    private static final int PORT=10900;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private String idd=null;
    private TextView mShowView=null;
    private View mControlsView=null;
    private EditText mInputView=null;
    private boolean mVisible;
    private OutputStream os;
    private Handler handler;
    private Button bn;
     /*class MyHandler extends Handler {
        FullscreenActivity  mActivity;// 弱引用

        public MyHandler(FullscreenActivity activity) {

// TODO Auto-generated constructor stub
            mActivity=(FullscreenActivity)(activity);

        }
        @Override
        public void handleMessage(Message msg) {
            // 如果消息来自子线程
            if (msg.what == 0x234) {
                // 将读取的内容追加显示在文本框中
                mActivity.mShowView.append("\n" + msg.obj.toString());
            }
        }
    }*/
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mShowView = (TextView)findViewById(R.id.fullscreen_content);
        mInputView=(EditText)findViewById(R.id.input);
        bn=(Button)findViewById(R.id.dummy_button);
            Intent intent=getIntent();
            idd=intent.getStringExtra("id");
            bn.setOnTouchListener(mDelayHideTouchListener);
            bn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mInputView.getText().toString().equals("")||mInputView.getText().toString()==null)
                        Toast.makeText(FullscreenActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                    else
                        try {
                            // 将用户在文本框内输入的内容写入网络

                            { os.write((idd+":"+mInputView.getText().toString() + "\r\n").getBytes());

                                // 清空input文本框数据
                                mInputView.setText("");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            });
            mShowView.setMovementMethod(new ScrollingMovementMethod());
        // Set up the user interaction to manually show or hide the system UI.
        mShowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        handler = new Handler(){
                public void handleMessage(Message msg) {
                    // 如果消息来自子线程
                    if (msg.what == 0x234) {
                        // 将读取的内容追加显示在文本框中
                        mShowView.append("\n" + msg.obj.toString());
                    }
                }
            };

            new Thread(networkTask).start();
        }
    // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

    Runnable networkTask = new Runnable() {


        public void run() {
            try {


                    Socket socket;
                socket = new Socket(HOSTNMAE_DOM, PORT);
                // 客户端启动ClientThread线程不断读取来自服务器的数据
                new Thread(new ClientThread(socket, handler)).start();
                os = socket.getOutputStream();

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    };
        @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);

            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mShowView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mShowView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    class ClientThread implements Runnable {
        private Handler handler1;
        // 该线程所处理的Socket所对应的输入流
        private BufferedReader br = null;

        public ClientThread(Socket socket, Handler handler) throws IOException {
            this.handler1 = handler;
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String content = null;
                // 不断读取Socket输入流的内容
                while ((content = br.readLine()) != null) {
                    // 每当读到来自服务器的数据之后，发送消息通知程序界面显示该数据
                    Message msg = new Message();
                    msg.what = 0x234;
                    msg.obj = content;
                    handler1.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

