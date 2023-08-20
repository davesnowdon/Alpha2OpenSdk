package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlpha2XmppCallBack extends IInterface {
   void onReceiveMessage(String var1) throws RemoteException;

   public abstract static class Stub extends Binder implements IAlpha2XmppCallBack {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack";
      static final int TRANSACTION_onReceiveMessage = 1;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack");
      }

      public static IAlpha2XmppCallBack asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack");
            return (IAlpha2XmppCallBack)(iin != null && iin instanceof IAlpha2XmppCallBack ? (IAlpha2XmppCallBack)iin : new IAlpha2XmppCallBack.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack");
            String _arg0 = data.readString();
            this.onReceiveMessage(_arg0);
            reply.writeNoException();
            return true;
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements IAlpha2XmppCallBack {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack";
         }

         public void onReceiveMessage(String message) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack");
               _data.writeString(message);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }
      }
   }
}
