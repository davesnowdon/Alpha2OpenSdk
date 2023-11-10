package com.ubtechinc.alpha2serverlib.aidlinterface;
/**
 * [讯飞语音client 回调监听 ] 
 * 
 * @author zengdengyi
 * 
 **/
interface IAlpha2SpeechClientListener{   
/** 听写结束后客户端接口回调得到听写结果 **/
void onServerCallBack(String text);
/** TTS播报结束后 客户端接口回调得到播放结束标志**/
void onServerPlayEnd(boolean isEnd);
}  