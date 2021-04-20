package com.ubtechinc.alpha2serverlib.aidlinterface;
/**
 * [讯飞语音主服务 回调监听 ] 
 * 
 * @author zengdengyi
 * 
 **/
interface ISpeechCallBackListener{   
/** 听写结束后客户端接口回调得到听写结果 **/
void onCallBack(int type, String text);
/** TTS播报结束后 客户端接口回调得到播放结束标志**/
void onPlayEnd(boolean isEnd);
}  