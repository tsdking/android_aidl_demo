package com.appgole.aidlclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appgole.aidldemo.IUserAidlCallback;
import com.appgole.aidldemo.IUserAidlInterface;
import com.appgole.aidldemo.User;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private IUserAidlInterface userAidlInterface;
    private ServiceConnection serviceConnection;
    private TextView textView;
    private int counter;
    private ServiceConnection messengerConn;
    private Messenger serverMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
    }


    public void addUser(View view) {
        if (userAidlInterface != null) {
            try {
                counter++;
                User user = new User();
                user.setAge(counter);
                user.setName("index-" + counter);
                userAidlInterface.addUser(user);
                textView.setText(user.toString());
            } catch (RemoteException e) {
                showToast(e.getMessage());
            }
        }
    }

    public void getUserList(View view) {
        if (userAidlInterface != null) {
            try {
                List<User> userList = userAidlInterface.getUserList();
                if (userList != null) {
                    showToast("total:" + userList.size());
                    textView.setText(userList.toString());
                }
            } catch (RemoteException e) {
                showToast(e.getMessage());
            }
        }
    }


    public void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void serverBind(View view) {
        serviceConnection = new ServiceConnection() {
            private static final String TAG = "ServiceConnection";

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                showToast("onServiceConnected:" + name.getClassName());
                userAidlInterface = IUserAidlInterface.Stub.asInterface(service);
                if (userAidlInterface != null) {
                    try {
                        userAidlInterface.registerCallback(new IUserAidlCallback.Stub() {
                            @Override
                            public void success(String result) throws RemoteException {
                                Log.d(TAG, "success: " + result);
                                showToast("success:" + result);
                            }

                            @Override
                            public void fail(String msg, int code) throws RemoteException {
                                Log.e(TAG, "fail:" + msg + "," + code);
                                showToast("fail:" + msg + "," + code);
                            }
                        });
                    } catch (RemoteException e) {
                        showToast("RemoteException:" + e.getMessage());
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                showToast("onServiceDisconnected:" + name.getClassName());
            }
        };
        Intent service = new Intent();
        service.setPackage("com.appgole.aidldemo");
        service.setAction("com.appgole.aidldemo.ServerService");
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void serverUnBind(View view) {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
        if (userAidlInterface != null) {
            try {
                userAidlInterface.unregisterCallback(null);
            } catch (RemoteException e) {
                showToast(e.getMessage());
            } finally {
                userAidlInterface = null;
            }
        }
    }


    private Messenger clientMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String response = msg.getData().getString("response");
            textView.setText(response);
            return true;
        }
    }));


    public void bindMessenger(View view) {
        messengerConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serverMessenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serverMessenger = null;
            }
        };
        Intent service = new Intent();
        service.setComponent(new ComponentName("com.appgole.aidldemo", "com.appgole.aidldemo.MessengerService"));
        bindService(service, messengerConn, Context.BIND_AUTO_CREATE);
    }

    public void sendMessenger(View view) {
        final int FLAG_MSG_FROM_CLIENT = 10086;
        if (serverMessenger != null) {
            Message obtain = Message.obtain();
            obtain.what = FLAG_MSG_FROM_CLIENT;
            Bundle data = new Bundle();
            data.putString("request", "测试请求数据" + System.currentTimeMillis());
            obtain.setData(data);
            obtain.replyTo = clientMessenger;
            try {
                serverMessenger.send(obtain);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (messengerConn != null) {
            unbindService(messengerConn);
            messengerConn = null;
        }
        super.onDestroy();
    }
}