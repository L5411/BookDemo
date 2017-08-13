package com.l_5411.bookdemo.chapter_2.socket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.l_5411.bookdemo.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TCPClientActivity extends AppCompatActivity {

    private static final String TAG = TCPClientActivity.class.getSimpleName();

    public static Intent newIntent(Context context) {
        return new Intent(context, TCPClientActivity.class);
    }

    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SEND_NEW_MSG = 2;
    private static final int MESSAGE_SOCKET_CONNECTED = 3;

    private PrintWriter mPrintWriter;
    private Socket mClientSocket;

    @BindView(R.id.send)
    Button mSendButton;

    @BindView(R.id.msg_container)
    TextView mMsgContainer;

    @BindView(R.id.msg)
    EditText mMsgEdit;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVE_NEW_MSG:
                    mMsgContainer.setText(mMsgContainer.getText() + (String) msg.obj);
                    break;
                case MESSAGE_SEND_NEW_MSG:
                    mMsgEdit.setText("");
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "self " + time + ":" + msg.obj + "\n";
                    mMsgContainer.setText(mMsgContainer.getText() + showedMsg);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    mSendButton.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);
        ButterKnife.bind(this);

        Intent service = new Intent(this, TCPServerService.class);
        startService(service);
        new Thread() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
    }

    @OnClick(R.id.send)
    public void onClick(View v) {
        final String msg = mMsgEdit.getText().toString();
        if(!msg.isEmpty() && mPrintWriter != null) {
            new Thread(){
                @Override
                public void run() {
                    mPrintWriter.println(msg);
                    mHandler.obtainMessage(MESSAGE_SEND_NEW_MSG, msg).sendToTarget();
                }
            }.start();
        }
    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("Connect server success.");
            } catch (IOException e) {
                SystemClock.sleep(1000);
                System.out.println("Connect tcp server failed, retry...");
            }
        }

        try {
            // 接受服务器消息
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()
            ));
            while (!TCPClientActivity.this.isFinishing()) {
                String msg = in.readLine();
                System.out.println("receive: " + msg);
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "Server " + time + ":" + msg + "\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg).sendToTarget();
                }
            }
            System.out.println("quit...");
            mPrintWriter.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String formatDateTime(long l) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(l));
    }
}
