package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISpeechCallBackListener extends IInterface {
   void onCallBack(int var1, String var2) throws RemoteException;

   void onPlayEnd(boolean var1) throws RemoteException;

   public abstract static class Stub extends Binder implements ISpeechCallBackListener {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener";
      static final int TRANSACTION_onCallBack = 1;
      static final int TRANSACTION_onPlayEnd = 2;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener");
      }

      public static ISpeechCallBackListener asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener");
            return (ISpeechCallBackListener)(iin != null && iin instanceof ISpeechCallBackListener ? (ISpeechCallBackListener)iin : new ISpeechCallBackListener.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener");
            int _arg0 = data.readInt();
            String _arg1 = data.readString();
            this.onCallBack(_arg0, _arg1);
            reply.writeNoException();
            return true;
         }
         case 2:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener");
            boolean _arg0 = 0 != data.readInt();
            this.onPlayEnd(_arg0);
            reply.writeNoException();
            return true;
         }
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements ISpeechCallBackListener {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener";
         }

         public void onCallBack(int type, String text) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener");
               _data.writeInt(type);
               _data.writeString(text);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onPlayEnd(boolean isEnd) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener");
               _data.writeInt(isEnd ? 1 : 0);
               this.mRemote.transact(2, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }
      }
   }
}
