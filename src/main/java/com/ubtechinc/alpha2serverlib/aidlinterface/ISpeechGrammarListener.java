package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISpeechGrammarListener extends IInterface {
   void onSpeechGrammarResult(String var1, String var2) throws RemoteException;

   void onSpeechGrammarError(int var1) throws RemoteException;

   public abstract static class Stub extends Binder implements ISpeechGrammarListener {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener";
      static final int TRANSACTION_onSpeechGrammarResult = 1;
      static final int TRANSACTION_onSpeechGrammarError = 2;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener");
      }

      public static ISpeechGrammarListener asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener");
            return (ISpeechGrammarListener)(iin != null && iin instanceof ISpeechGrammarListener ? (ISpeechGrammarListener)iin : new ISpeechGrammarListener.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener");
            String _arg0 = data.readString();
            String _arg1 = data.readString();
            this.onSpeechGrammarResult(_arg0, _arg1);
            reply.writeNoException();
            return true;
         case 2:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener");
            int _arg0 = data.readInt();
            this.onSpeechGrammarError(_arg0);
            reply.writeNoException();
            return true;
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements ISpeechGrammarListener {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener";
         }

         public void onSpeechGrammarResult(String strResultType, String strResult) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener");
               _data.writeString(strResultType);
               _data.writeString(strResult);
               this.mRemote.transact(1, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onSpeechGrammarError(int nErrorCode) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener");
               _data.writeInt(nErrorCode);
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
