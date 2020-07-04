// IUserAidlCallback.aidl
package com.appgole.aidldemo;

// Declare any non-default types here with import statements

interface IUserAidlCallback {
    void success(String result);
    void fail(String msg,int code);
}
