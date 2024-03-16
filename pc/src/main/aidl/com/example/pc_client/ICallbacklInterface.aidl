// ICallbacklInterface.aidl
package com.example.pc_client;

// Declare any non-default types here with import statements

interface ICallbacklInterface {
    void call();
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}