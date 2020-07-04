package com.appgole.aidldemo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ServerService extends Service {
    private ArrayList<User> userList = new ArrayList<>();
    private H mH = new H();

    private static class H extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }


    public ServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new InternalServer();
    }

    private class InternalServer extends IUserAidlInterface.Stub {
        IUserAidlCallback callback;

        @Override
        public boolean addUser(final User user) throws RemoteException {
            if (user == null) {
                callback.fail("user is null", -1);
                return false;
            }
            //模拟耗时操作
            mH.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        callback.success("添加成功:" + user.toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
            return userList.add(user);
        }

        @Override
        public List<User> getUserList() throws RemoteException {
            return userList;
        }

        @Override
        public void registerCallback(IUserAidlCallback callback) throws RemoteException {
            this.callback = callback;
        }

        @Override
        public void unregisterCallback(IUserAidlCallback callback) throws RemoteException {
            this.callback = null;
        }
    }


    @Override
    public void onDestroy() {
        if (mH != null) {
            mH.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
