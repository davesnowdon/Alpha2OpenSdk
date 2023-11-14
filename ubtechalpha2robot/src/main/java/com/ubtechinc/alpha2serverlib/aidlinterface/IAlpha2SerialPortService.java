package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlpha2SerialPortService extends IInterface {
   int registerSerialPortRcvListener(IAlpha2SerialPortRcvClient var1) throws RemoteException;

   int unRegisterSerialPortRcvListener(IAlpha2SerialPortRcvClient var1) throws RemoteException;

   boolean sendCommand(byte var1, byte var2, byte[] var3, int var4) throws RemoteException;

   boolean sendRawData(byte[] var1, int var2) throws RemoteException;

   public abstract static class Stub extends Binder implements IAlpha2SerialPortService {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService";
      static final int TRANSACTION_registerSerialPortRcvListener = 1;
      static final int TRANSACTION_unRegisterSerialPortRcvListener = 2;
      static final int TRANSACTION_sendCommand = 3;
      static final int TRANSACTION_sendRawData = 4;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
      }

      public static IAlpha2SerialPortService asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
            return (IAlpha2SerialPortService)(iin != null && iin instanceof IAlpha2SerialPortService ? (IAlpha2SerialPortService)iin : new IAlpha2SerialPortService.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
            IAlpha2SerialPortRcvClient _arg0 = IAlpha2SerialPortRcvClient.Stub.asInterface(data.readStrongBinder());
            int _arg1 = this.registerSerialPortRcvListener(_arg0);
            reply.writeNoException();
            reply.writeInt(_arg1);
            return true;
         }
         case 2:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
            IAlpha2SerialPortRcvClient _arg0 = IAlpha2SerialPortRcvClient.Stub.asInterface(data.readStrongBinder());
            int _arg1 = this.unRegisterSerialPortRcvListener(_arg0);
            reply.writeNoException();
            reply.writeInt(_arg1);
            return true;
         }
         case 3:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
            byte _arg0 = data.readByte();
            byte _arg1 = data.readByte();
            byte[] _arg2 = data.createByteArray();
            int _arg3 = data.readInt();
            boolean _result = this.sendCommand(_arg0, _arg1, _arg2, _arg3);
            reply.writeNoException();
            reply.writeInt(_result ? 1 : 0);
            return true;
         }
         case 4:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
            byte[] _arg0 = data.createByteArray();
            int _arg1 = data.readInt();
            boolean _result = this.sendRawData(_arg0, _arg1);
            reply.writeNoException();
            reply.writeInt(_result ? 1 : 0);
            return true;
         }
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements IAlpha2SerialPortService {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService";
         }

         public int registerSerialPortRcvListener(IAlpha2SerialPortRcvClient cb) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            int _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
               _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
               _result = _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public int unRegisterSerialPortRcvListener(IAlpha2SerialPortRcvClient cb) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            int _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
               _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
               this.mRemote.transact(2, _data, _reply, 0);
               _reply.readException();
               _result = _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public boolean sendCommand(byte nSessionID, byte nCmd, byte[] nParam, int nLen) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            boolean _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
               _data.writeByte(nSessionID);
               _data.writeByte(nCmd);
               _data.writeByteArray(nParam);
               _data.writeInt(nLen);
               this.mRemote.transact(3, _data, _reply, 0);
               _reply.readException();
               _result = 0 != _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public boolean sendRawData(byte[] data, int nLen) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            boolean _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortService");
               _data.writeByteArray(data);
               _data.writeInt(nLen);
               this.mRemote.transact(4, _data, _reply, 0);
               _reply.readException();
               _result = 0 != _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }
      }
   }
}
