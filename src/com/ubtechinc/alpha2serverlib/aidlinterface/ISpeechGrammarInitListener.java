package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISpeechGrammarInitListener extends IInterface {
   void speechGrammarInitCallback(String var1, int var2) throws RemoteException;

   public abstract static class Stub extends Binder implements ISpeechGrammarInitListener {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener";
      static final int TRANSACTION_speechGrammarInitCallback = 1;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener");
      }

      public static ISpeechGrammarInitListener asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener");
            return (ISpeechGrammarInitListener)(iin != null && iin instanceof ISpeechGrammarInitListener ? (ISpeechGrammarInitListener)iin : new ISpeechGrammarInitListener.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener");
            String _arg0 = data.readString();
            int _arg1 = data.readInt();
            this.speechGrammarInitCallback(_arg0, _arg1);
            reply.writeNoException();
            return true;
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements ISpeechGrammarInitListener {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener";
         }

         public void speechGrammarInitCallback(String grammarID, int nErrorCode) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener");
               _data.writeString(grammarID);
               _data.writeInt(nErrorCode);
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
