package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlpha2SerialPortRcvClient extends IInterface {
   void onListenSerialPortRcvData(byte[] var1, int var2) throws RemoteException;

   public abstract static class Stub extends Binder implements IAlpha2SerialPortRcvClient {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient";
      static final int TRANSACTION_onListenSerialPortRcvData = 1;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient");
      }

      public static IAlpha2SerialPortRcvClient asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient");
            return (IAlpha2SerialPortRcvClient)(iin != null && iin instanceof IAlpha2SerialPortRcvClient ? (IAlpha2SerialPortRcvClient)iin : new IAlpha2SerialPortRcvClient.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient");
            byte[] _arg0 = data.createByteArray();
            int _arg1 = data.readInt();
            this.onListenSerialPortRcvData(_arg0, _arg1);
            reply.writeNoException();
            return true;
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements IAlpha2SerialPortRcvClient {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient";
         }

         public void onListenSerialPortRcvData(byte[] bytes, int len) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient");
               _data.writeByteArray(bytes);
               _data.writeInt(len);
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
