package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISpeechInterface extends IInterface {
   int registerSpeechCallBackListener(ISpeechCallBackListener var1) throws RemoteException;

   int unRegisterSpeechCallBackListener(ISpeechCallBackListener var1) throws RemoteException;

   void onSpeech(ISpeechCallBackListener var1, String var2) throws RemoteException;

   void onStopSpeech(ISpeechCallBackListener var1) throws RemoteException;

   void onPlay(ISpeechCallBackListener var1, String var2, String var3, String var4) throws RemoteException;

   void onPlayHigh(ISpeechCallBackListener var1, String var2, String var3, String var4) throws RemoteException;

   void onStopPlay(ISpeechCallBackListener var1) throws RemoteException;

   void setWakeState(boolean var1) throws RemoteException;

   void onTextUnderstand(String var1, IAlphaTextUnderstandListener var2) throws RemoteException;

   void initSpeechGrammar(String var1, ISpeechGrammarInitListener var2) throws RemoteException;

   void startSpeechGrammar(ISpeechGrammarListener var1) throws RemoteException;

   void stopSpeechGrammar() throws RemoteException;

   void stopSpeechAndEnterIdleMode() throws RemoteException;

   void setRecognizedLanguage(String var1) throws RemoteException;

   void setVoiceName(String var1) throws RemoteException;

   void onEnglishUnderstand(IAlphaEnglishUnderstandListener var1) throws RemoteException;

   void setEnglishOfflineListener(IAlphaEnglishOfflineUnderstandListener var1) throws RemoteException;

   void setSelfInterrupt(boolean var1) throws RemoteException;

   public abstract static class Stub extends Binder implements ISpeechInterface {
      private static final String DESCRIPTOR = "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface";
      static final int TRANSACTION_registerSpeechCallBackListener = 1;
      static final int TRANSACTION_unRegisterSpeechCallBackListener = 2;
      static final int TRANSACTION_onSpeech = 3;
      static final int TRANSACTION_onStopSpeech = 4;
      static final int TRANSACTION_onPlay = 5;
      static final int TRANSACTION_onPlayHigh = 6;
      static final int TRANSACTION_onStopPlay = 7;
      static final int TRANSACTION_setWakeState = 8;
      static final int TRANSACTION_onTextUnderstand = 9;
      static final int TRANSACTION_initSpeechGrammar = 10;
      static final int TRANSACTION_startSpeechGrammar = 11;
      static final int TRANSACTION_stopSpeechGrammar = 12;
      static final int TRANSACTION_stopSpeechAndEnterIdleMode = 13;
      static final int TRANSACTION_setRecognizedLanguage = 14;
      static final int TRANSACTION_setVoiceName = 15;
      static final int TRANSACTION_onEnglishUnderstand = 16;
      static final int TRANSACTION_setEnglishOfflineListener = 17;
      static final int TRANSACTION_setSelfInterrupt = 18;

      public Stub() {
         this.attachInterface(this, "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
      }

      public static ISpeechInterface asInterface(IBinder obj) {
         if (obj == null) {
            return null;
         } else {
            IInterface iin = obj.queryLocalInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            return (ISpeechInterface)(iin != null && iin instanceof ISpeechInterface ? (ISpeechInterface)iin : new ISpeechInterface.Stub.Proxy(obj));
         }
      }

      public IBinder asBinder() {
         return this;
      }

      public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
         switch(code) {
         case 1:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechCallBackListener _arg0 = ISpeechCallBackListener.Stub.asInterface(data.readStrongBinder());
            int _result = this.registerSpeechCallBackListener(_arg0);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
         }
         case 2:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechCallBackListener _arg0 = ISpeechCallBackListener.Stub.asInterface(data.readStrongBinder());
            int _result = this.unRegisterSpeechCallBackListener(_arg0);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
         }
         case 3:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechCallBackListener _arg0 = ISpeechCallBackListener.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            this.onSpeech(_arg0, _arg1);
            reply.writeNoException();
            return true;
         }
         case 4:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechCallBackListener _arg0 = ISpeechCallBackListener.Stub.asInterface(data.readStrongBinder());
            this.onStopSpeech(_arg0);
            reply.writeNoException();
            return true;
         }
         case 5:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechCallBackListener _arg0 = ISpeechCallBackListener.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            String _arg2 = data.readString();
            String _arg3 = data.readString();
            this.onPlay(_arg0, _arg1, _arg2, _arg3);
            reply.writeNoException();
            return true;
         }
         case 6:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechCallBackListener _arg0 = ISpeechCallBackListener.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            String _arg2 = data.readString();
            String _arg3 = data.readString();
            this.onPlayHigh(_arg0, _arg1, _arg2, _arg3);
            reply.writeNoException();
            return true;
         }
         case 7:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechCallBackListener _arg0 = ISpeechCallBackListener.Stub.asInterface(data.readStrongBinder());
            this.onStopPlay(_arg0);
            reply.writeNoException();
            return true;
         }
         case 8:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            boolean _arg0 = 0 != data.readInt();
            this.setWakeState(_arg0);
            reply.writeNoException();
            return true;
         }
         case 9:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            String _arg0 = data.readString();
            IAlphaTextUnderstandListener _arg1 = IAlphaTextUnderstandListener.Stub.asInterface(data.readStrongBinder());
            this.onTextUnderstand(_arg0, _arg1);
            reply.writeNoException();
            return true;
         }
         case 10:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            String _arg0 = data.readString();
            ISpeechGrammarInitListener _arg1 = ISpeechGrammarInitListener.Stub.asInterface(data.readStrongBinder());
            this.initSpeechGrammar(_arg0, _arg1);
            reply.writeNoException();
            return true;
         }
         case 11:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            ISpeechGrammarListener _arg0 = ISpeechGrammarListener.Stub.asInterface(data.readStrongBinder());
            this.startSpeechGrammar(_arg0);
            reply.writeNoException();
            return true;
         }
         case 12:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            this.stopSpeechGrammar();
            reply.writeNoException();
            return true;
         case 13:
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            this.stopSpeechAndEnterIdleMode();
            reply.writeNoException();
            return true;
         case 14:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            String _arg0 = data.readString();
            this.setRecognizedLanguage(_arg0);
            reply.writeNoException();
            return true;
         }
         case 15:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            String _arg0 = data.readString();
            this.setVoiceName(_arg0);
            reply.writeNoException();
            return true;
         }
         case 16:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            IAlphaEnglishUnderstandListener _arg0 = IAlphaEnglishUnderstandListener.Stub.asInterface(data.readStrongBinder());
            this.onEnglishUnderstand(_arg0);
            reply.writeNoException();
            return true;
         }
         case 17:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            IAlphaEnglishOfflineUnderstandListener _arg0 = IAlphaEnglishOfflineUnderstandListener.Stub.asInterface(data.readStrongBinder());
            this.setEnglishOfflineListener(_arg0);
            reply.writeNoException();
            return true;
         }
         case 18:
         {
            data.enforceInterface("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            boolean _arg0 = 0 != data.readInt();
            this.setSelfInterrupt(_arg0);
            reply.writeNoException();
            return true;
         }
         case 1598968902:
            reply.writeString("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
            return true;
         default:
            return super.onTransact(code, data, reply, flags);
         }
      }

      private static class Proxy implements ISpeechInterface {
         private IBinder mRemote;

         Proxy(IBinder remote) {
            this.mRemote = remote;
         }

         public IBinder asBinder() {
            return this.mRemote;
         }

         public String getInterfaceDescriptor() {
            return "com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface";
         }

         public int registerSpeechCallBackListener(ISpeechCallBackListener callBack) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            int _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
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

         public int unRegisterSpeechCallBackListener(ISpeechCallBackListener callBack) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            int _result;
            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
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

         public void onSpeech(ISpeechCallBackListener listener, String text) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               _data.writeString(text);
               this.mRemote.transact(3, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onStopSpeech(ISpeechCallBackListener listener) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               this.mRemote.transact(4, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onPlay(ISpeechCallBackListener listener, String text, String strVoiceName, String language) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               _data.writeString(text);
               _data.writeString(strVoiceName);
               _data.writeString(language);
               this.mRemote.transact(5, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onPlayHigh(ISpeechCallBackListener listener, String text, String strVoiceName, String language) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               _data.writeString(text);
               _data.writeString(strVoiceName);
               _data.writeString(language);
               this.mRemote.transact(6, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onStopPlay(ISpeechCallBackListener listener) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               this.mRemote.transact(7, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void setWakeState(boolean onWake) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeInt(onWake ? 1 : 0);
               this.mRemote.transact(8, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onTextUnderstand(String strText, IAlphaTextUnderstandListener listener) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeString(strText);
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               this.mRemote.transact(9, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void initSpeechGrammar(String strGrammar, ISpeechGrammarInitListener listener) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeString(strGrammar);
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               this.mRemote.transact(10, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void startSpeechGrammar(ISpeechGrammarListener listern) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listern != null ? listern.asBinder() : null);
               this.mRemote.transact(11, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void stopSpeechGrammar() throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               this.mRemote.transact(12, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void stopSpeechAndEnterIdleMode() throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               this.mRemote.transact(13, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void setRecognizedLanguage(String strLanguage) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeString(strLanguage);
               this.mRemote.transact(14, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void setVoiceName(String strVoiceName) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeString(strVoiceName);
               this.mRemote.transact(15, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void onEnglishUnderstand(IAlphaEnglishUnderstandListener listener) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               this.mRemote.transact(16, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void setEnglishOfflineListener(IAlphaEnglishOfflineUnderstandListener listener) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
               this.mRemote.transact(17, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }

         public void setSelfInterrupt(boolean isInterrupt) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            try {
               _data.writeInterfaceToken("com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechInterface");
               _data.writeInt(isInterrupt ? 1 : 0);
               this.mRemote.transact(18, _data, _reply, 0);
               _reply.readException();
            } finally {
               _reply.recycle();
               _data.recycle();
            }

         }
      }
   }
}
