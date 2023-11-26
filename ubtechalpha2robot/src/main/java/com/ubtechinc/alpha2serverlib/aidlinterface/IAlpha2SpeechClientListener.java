package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlpha2SpeechClientListener extends IInterface {
   void onServerCallBack(String var1) throws RemoteException;

   void onServerPlayEnd(boolean var1) throws RemoteException;

   public abstract static class Stub extends Binder implements IAlpha2SpeechClientListener {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener";
      static final int TRANSACTION_onServerCallBack = 1;
      static final int TRANSACTION_onServerPlayEnd = 2;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener");
      }

      public static IAlpha2SpeechClientListener asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener");
            return (IAlpha2SpeechClientListener)(iin != null && iin instanceof IAlpha2SpeechClientListener ? (IAlpha2SpeechClientListener)iin : new IAlpha2SpeechClientListener.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener");
            String _arg0 = data.readString();
            this.onServerCallBack(_arg0);
            reply.writeNoException();
            return true;
         }
         case 2:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener");
            boolean _arg0 = 0 != data.readInt();
            this.onServerPlayEnd(_arg0);
            reply.writeNoException();
            return true;
         }
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements IAlpha2SpeechClientListener {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener";
         }

         public void onServerCallBack(String text) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener");
               _data.writeString(text);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onServerPlayEnd(boolean isEnd) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SpeechClientListener");
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
