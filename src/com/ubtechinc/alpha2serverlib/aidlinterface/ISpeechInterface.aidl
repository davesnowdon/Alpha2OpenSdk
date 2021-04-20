package com.ubtechinc.alpha2serverlib.aidlinterface;

import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechCallBackListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaTextUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaEnglishOfflineUnderstandListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarInitListener;
import com.ubtechinc.alpha2serverlib.aidlinterface.ISpeechGrammarListener;

/**
 * [讯飞语音主服务 AIDL ] 
 * 
 * @author zengdengyi
 * 
 **/
interface ISpeechInterface{   
int registerSpeechCallBackListener(ISpeechCallBackListener callBack);
int unRegisterSpeechCallBackListener(ISpeechCallBackListener callBack);

/** 进入到听写场景，得到结果时候通过listener 返回结果**/       
void onSpeech(ISpeechCallBackListener listener,String text); 
/** 停止听写  **/
void onStopSpeech(ISpeechCallBackListener listener);   
/** 进行TTS播报 低优先级 不可打断正在播报的TTS**/   
void onPlay(ISpeechCallBackListener listener,String text, 
	String strVoiceName,String language);
/** 进行TTS播报 高优先级可打断其他正在播报的TTS，**/
void onPlayHigh(ISpeechCallBackListener listener,String text, 
	String strVoiceName,String language);
/** 停止TTS播报 **/
void onStopPlay(ISpeechCallBackListener listener);
void setWakeState(boolean onWake);
/** 文本语义理解 **/
void onTextUnderstand(String strText, IAlphaTextUnderstandListener listener);

/** 初始化语法识别 **/
void initSpeechGrammar(String strGrammar, ISpeechGrammarInitListener listener);
/** 开始语法识别 **/
void startSpeechGrammar(ISpeechGrammarListener listern);
/** 停止语法识别 **/
void stopSpeechGrammar();
/** 停止所有功能，并进入等待唤醒的模式**/
void stopSpeechAndEnterIdleMode();
/** set recognized language **/
void setRecognizedLanguage(String strLanguage);
/** 设置发声人 **/
void setVoiceName(String strVoiceName);
/** Nuance英文语义理解 **/
void onEnglishUnderstand(IAlphaEnglishUnderstandListener listener);
/** Nuance离线英文语义理解 **/
void setEnglishOfflineListener(IAlphaEnglishOfflineUnderstandListener listener);
/** 是否开启自打断 **/
void setSelfInterrupt(boolean isInterrupt);
}  