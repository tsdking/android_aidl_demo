package com.appgole.aidldemo;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.annotation.NonNull;

public class MessengerService extends Service {
    public MessengerService() {
    }

    private Handler mH = new H();
    private Messenger serverMessenger = new Messenger(mH);

    @Override
    public IBinder onBind(Intent intent) {
        return serverMessenger.getBinder();
    }


    private static class H extends Handler {
        private final int FLAG_MSG_FROM_CLIENT = 10086;
        private final int FLAG_MSG_FROM_SERVER = 10087;

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == FLAG_MSG_FROM_CLIENT) {
                String requestParam = msg.getData().getString("request");
                Messenger clientMessenger = msg.replyTo;
                Message responseMessage = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("response", "来自Server的消息，收到:" + requestParam);
                responseMessage.setData(bundle);
                responseMessage.what = FLAG_MSG_FROM_SERVER;
                if (clientMessenger != null) {
                    try {
                        clientMessenger.send(responseMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
