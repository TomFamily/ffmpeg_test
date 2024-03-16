/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.example.pc_client;

/**
 * 作用：客户端 -数据-》服务端
 */
public interface IManagerInterface extends android.os.IInterface
{
  /** Default implementation for IManagerInterface. */
  public static class Default implements IManagerInterface
  {
    /**
         * Demonstrates some basic types that you can use as parameters
         * and return values in AIDL.
         */
    @Override public void test() throws android.os.RemoteException
    {
    }
    @Override public void setCallBack(ICallbacklInterface callback) throws android.os.RemoteException
    {
    }
    @Override public void basicTypes2(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements IManagerInterface
  {
    private static final String DESCRIPTOR = "com.example.pc_client.IManagerInterface";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.example.pc_client.IManagerInterface interface,
     * generating a proxy if needed.
     */
    public static IManagerInterface asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof IManagerInterface))) {
        return ((IManagerInterface)iin);
      }
      return new Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_test:
        {
          data.enforceInterface(descriptor);
          this.test();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_setCallBack:
        {
          data.enforceInterface(descriptor);
          ICallbacklInterface _arg0;
          _arg0 = ICallbacklInterface.Stub.asInterface(data.readStrongBinder());
          this.setCallBack(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_basicTypes2:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          long _arg1;
          _arg1 = data.readLong();
          boolean _arg2;
          _arg2 = (0!=data.readInt());
          float _arg3;
          _arg3 = data.readFloat();
          double _arg4;
          _arg4 = data.readDouble();
          String _arg5;
          _arg5 = data.readString();
          this.basicTypes2(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
          reply.writeNoException();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements IManagerInterface
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
           * Demonstrates some basic types that you can use as parameters
           * and return values in AIDL.
           */
      @Override public void test() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_test, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().test();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setCallBack(ICallbacklInterface callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_setCallBack, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setCallBack(callback);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void basicTypes2(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(anInt);
          _data.writeLong(aLong);
          _data.writeInt(((aBoolean)?(1):(0)));
          _data.writeFloat(aFloat);
          _data.writeDouble(aDouble);
          _data.writeString(aString);
          boolean _status = mRemote.transact(Stub.TRANSACTION_basicTypes2, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().basicTypes2(anInt, aLong, aBoolean, aFloat, aDouble, aString);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static IManagerInterface sDefaultImpl;
    }
    static final int TRANSACTION_test = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_setCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_basicTypes2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    public static boolean setDefaultImpl(IManagerInterface impl) {
      // Only one user of this interface can use this function
      // at a time. This is a heuristic to detect if two different
      // users in the same process use this function.
      if (Proxy.sDefaultImpl != null) {
        throw new IllegalStateException("setDefaultImpl() called twice");
      }
      if (impl != null) {
        Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static IManagerInterface getDefaultImpl() {
      return Proxy.sDefaultImpl;
    }
  }
  /**
       * Demonstrates some basic types that you can use as parameters
       * and return values in AIDL.
       */
  public void test() throws android.os.RemoteException;
  public void setCallBack(ICallbacklInterface callback) throws android.os.RemoteException;
  public void basicTypes2(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws android.os.RemoteException;
}
