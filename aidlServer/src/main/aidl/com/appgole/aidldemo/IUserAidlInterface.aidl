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
