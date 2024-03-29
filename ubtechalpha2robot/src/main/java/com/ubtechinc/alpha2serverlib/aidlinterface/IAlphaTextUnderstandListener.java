package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlphaTextUnderstandListener extends IInterface {
   void onAlpha2UnderStandError(int var1) throws RemoteException;

   void onAlpha2UnderStandTextResult(String var1) throws RemoteException;

   public abstract static class Stub extends Binder implements IAlphaTextUnderstandListener {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener";
      static final int TRANSACTION_onAlpha2UnderStandError = 1;
      static final int TRANSACTION_onAlpha2UnderStandTextResult = 2;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener");
      }

      public static IAlphaTextUnderstandListener asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener");
            return (IAlphaTextUnderstandListener)(iin != null && iin instanceof IAlphaTextUnderstandListener ? (IAlphaTextUnderstandListener)iin : new IAlphaTextUnderstandListener.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener");
            int _arg0 = data.readInt();
            this.onAlpha2UnderStandError(_arg0);
            reply.writeNoException();
            return true;
         }
         case 2:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener");
            String _arg0 = data.readString();
            this.onAlpha2UnderStandTextResult(_arg0);
            reply.writeNoException();
            return true;
         }
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements IAlphaTextUnderstandListener {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener";
         }

         public void onAlpha2UnderStandError(int nErrorCode) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener");
               _data.writeInt(nErrorCode);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onAlpha2UnderStandTextResult(String strResult) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener");
               _data.writeString(strResult);
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
