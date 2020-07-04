## 动画演示
<img src="https://github.com/tsdking/android_aidl_demo/raw/master/image/aidl_demo.gif">

## AIDL Client
<img src="https://github.com/tsdking/android_aidl_demo/raw/master/image/AIDL_Client.png">

## AIDL Server
<img src="https://github.com/tsdking/android_aidl_demo/raw/master/image/AIDL_Server.png">

## AIDL 文件
<img src="https://github.com/tsdking/android_aidl_demo/raw/master/image/aidl.png">

- aidl定义
```aidl
// IUserAidlInterface.aidl
package com.appgole.aidldemo;

// Declare any non-default types here with import statements
import com.appgole.aidldemo.User;
import com.appgole.aidldemo.IUserAidlCallback;
interface IUserAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean addUser(in User user);
    List<User> getUserList();
    void registerCallback(in IUserAidlCallback callback);
    void unregisterCallback(in IUserAidlCallback callback);
}
```

- Server端代码
```java
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
```

- 客户端代码
```java
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
```