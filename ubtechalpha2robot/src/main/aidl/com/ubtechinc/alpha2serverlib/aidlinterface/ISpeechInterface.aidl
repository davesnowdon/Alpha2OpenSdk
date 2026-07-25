// AIDL for the robot's speech service (com.ubtechinc.services.SpeechServices):
// TTS playback, dictation, grammar recognition and semantic understanding.
// Method declaration order defines the Binder transaction ids and must match the
// on-robot service exactly - do not reorder, rename or change signatures.
package com.ubtechinc.alpha2serverlib.aidlinterface;

import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishOfflineUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener;

interface ISpeechInterface {
    int registerSpeechCallBackListener(ISpeechCallBackListener callBack);
    int unRegisterSpeechCallBackListener(ISpeechCallBackListener callBack);
    void onSpeech(ISpeechCallBackListener listener, String text);
    void onStopSpeech(ISpeechCallBackListener listener);
    void onPlay(ISpeechCallBackListener listener, String text, String strVoiceName, String language);
    void onPlayHigh(ISpeechCallBackListener listener, String text, String strVoiceName, String language);
    void onStopPlay(ISpeechCallBackListener listener);
    void setWakeState(boolean onWake);
    void onTextUnderstand(String strText, IAlphaTextUnderstandListener listener);
    void initSpeechGrammar(String strGrammar, ISpeechGrammarInitListener listener);
    void startSpeechGrammar(ISpeechGrammarListener listern);
    void stopSpeechGrammar();
    void stopSpeechAndEnterIdleMode();
    void setRecognizedLanguage(String strLanguage);
    void setVoiceName(String strVoiceName);
    void onEnglishUnderstand(IAlphaEnglishUnderstandListener listener);
    void setEnglishOfflineListener(IAlphaEnglishOfflineUnderstandListener listener);
    void setSelfInterrupt(boolean isInterrupt);
}
