// IManagerInterface.aidl
package com.example.pc_client;

// Declare any non-default types here with import statements
import com.example.pc_client.ICallbacklInterface;

interface IManagerInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void test();
     void setCallBack(ICallbacklInterface callback);

    void basicTypes2(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}