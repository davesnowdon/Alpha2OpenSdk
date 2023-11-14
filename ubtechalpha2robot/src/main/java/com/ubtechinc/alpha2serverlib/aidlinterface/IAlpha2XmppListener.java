package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlpha2XmppListener extends IInterface {
   int registerXmppCallBackListener(String var1, IAlpha2XmppCallBack var2) throws RemoteException;

   int unRegisterXmppCallBackListener(IAlpha2XmppCallBack var1) throws RemoteException;

   void sendCustomXmppMessage(int var1, String var2, String var3) throws RemoteException;

   public abstract static class Stub extends Binder implements IAlpha2XmppListener {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener";
      static final int TRANSACTION_registerXmppCallBackListener = 1;
      static final int TRANSACTION_unRegisterXmppCallBackListener = 2;
      static final int TRANSACTION_sendCustomXmppMessage = 3;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
      }

      public static IAlpha2XmppListener asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
            return (IAlpha2XmppListener)(iin != null && iin instanceof IAlpha2XmppListener ? (IAlpha2XmppListener)iin : new IAlpha2XmppListener.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
            String _arg0 = data.readString();
            IAlpha2XmppCallBack _arg1 = IAlpha2XmppCallBack.Stub.asInterface(data.readStrongBinder());
            int _result = this.registerXmppCallBackListener(_arg0, _arg1);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
         }
         case 2:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
            IAlpha2XmppCallBack _arg0 = IAlpha2XmppCallBack.Stub.asInterface(data.readStrongBinder());
            int _result = this.unRegisterXmppCallBackListener(_arg0);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
         }
         case 3:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
            int _arg0 = data.readInt();
            String _arg1 = data.readString();
            String _arg2 = data.readString();
            this.sendCustomXmppMessage(_arg0, _arg1, _arg2);
            reply.writeNoException();
            return true;
         }
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements IAlpha2XmppListener {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener";
         }

         public int registerXmppCallBackListener(String appID, IAlpha2XmppCallBack callBack) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            int _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
               _data.writeString(appID);
               _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
               _result = _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public int unRegisterXmppCallBackListener(IAlpha2XmppCallBack callBack) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            int _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
               _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
               this.mRemote.transact(2, _data, _reply, 0);
               _reply.readException();
               _result = _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public void sendCustomXmppMessage(int type, String appID, String message) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppListener");
               _data.writeInt(type);
               _data.writeString(appID);
               _data.writeString(message);
               this.mRemote.transact(3, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }
      }
   }
}
