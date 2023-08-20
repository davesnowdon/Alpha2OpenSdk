package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlphaActionService extends IInterface {
   int registerActionClient(IAlphaActionClient var1) throws RemoteException;

   void unRegisterActionClient(IAlphaActionClient var1) throws RemoteException;

   boolean playActionFile(String var1) throws RemoteException;

   boolean playActionName(String var1) throws RemoteException;

   void stopActionPlay() throws RemoteException;

   void onEventHandlerTrigger(int var1, byte[] var2) throws RemoteException;

   boolean isCompleted() throws RemoteException;

   void getActionList(IAlphaActionListListener var1) throws RemoteException;

   public abstract static class Stub extends Binder implements IAlphaActionService {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService";
      static final int TRANSACTION_registerActionClient = 1;
      static final int TRANSACTION_unRegisterActionClient = 2;
      static final int TRANSACTION_playActionFile = 3;
      static final int TRANSACTION_playActionName = 4;
      static final int TRANSACTION_stopActionPlay = 5;
      static final int TRANSACTION_onEventHandlerTrigger = 6;
      static final int TRANSACTION_isCompleted = 7;
      static final int TRANSACTION_getActionList = 8;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
      }

      public static IAlphaActionService asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            return (IAlphaActionService)(iin != null && iin instanceof IAlphaActionService ? (IAlphaActionService)iin : new IAlphaActionService.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         String _arg0;
         boolean _result;
         IAlphaActionClient _arg0;
         switch(code) {
         case 1:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            _arg0 = IAlphaActionClient.Stub.asInterface(data.readStrongBinder());
            int _result = this.registerActionClient(_arg0);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
         case 2:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            _arg0 = IAlphaActionClient.Stub.asInterface(data.readStrongBinder());
            this.unRegisterActionClient(_arg0);
            reply.writeNoException();
            return true;
         case 3:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            _arg0 = data.readString();
            _result = this.playActionFile(_arg0);
            reply.writeNoException();
            reply.writeInt(_result ? 1 : 0);
            return true;
         case 4:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            _arg0 = data.readString();
            _result = this.playActionName(_arg0);
            reply.writeNoException();
            reply.writeInt(_result ? 1 : 0);
            return true;
         case 5:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            this.stopActionPlay();
            reply.writeNoException();
            return true;
         case 6:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            int _arg0 = data.readInt();
            byte[] _arg1 = data.createByteArray();
            this.onEventHandlerTrigger(_arg0, _arg1);
            reply.writeNoException();
            return true;
         case 7:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            boolean _result = this.isCompleted();
            reply.writeNoException();
            reply.writeInt(_result ? 1 : 0);
            return true;
         case 8:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            IAlphaActionListListener _arg0 = IAlphaActionListListener.Stub.asInterface(data.readStrongBinder());
            this.getActionList(_arg0);
            reply.writeNoException();
            return true;
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements IAlphaActionService {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService";
         }

         public int registerActionClient(IAlphaActionClient client) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            int _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               _data.writeStrongBinder(client != null ? client.asBinder() : null);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
               _result = _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public void unRegisterActionClient(IAlphaActionClient client) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               _data.writeStrongBinder(client != null ? client.asBinder() : null);
               this.mRemote.transact(2, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public boolean playActionFile(String strActionFile) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            boolean _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               _data.writeString(strActionFile);
               this.mRemote.transact(3, _data, _reply, 0);
               _reply.readException();
               _result = 0 != _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public boolean playActionName(String strActionName) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            boolean _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               _data.writeString(strActionName);
               this.mRemote.transact(4, _data, _reply, 0);
               _reply.readException();
               _result = 0 != _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public void stopActionPlay() throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               this.mRemote.transact(5, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onEventHandlerTrigger(int nEventType, byte[] param) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               _data.writeInt(nEventType);
               _data.writeByteArray(param);
               this.mRemote.transact(6, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public boolean isCompleted() throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            boolean _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               this.mRemote.transact(7, _data, _reply, 0);
               _reply.readException();
               _result = 0 != _reply.readInt();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

            return _result;
         }

         public void getActionList(IAlphaActionListListener listener) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionService");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               this.mRemote.transact(8, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }
      }
   }
}
