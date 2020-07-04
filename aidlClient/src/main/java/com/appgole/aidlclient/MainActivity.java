package com.appgole.aidlclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.appgole.aidldemo.IUserAidlCallback;
import com.appgole.aidldemo.IUserAidlInterface;
import com.appgole.aidldemo.User;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private  IUserAidlInterface userAidlInterface;
    private ServiceConnection serviceConnection;
    private TextView textView;
    private int counter;
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
}