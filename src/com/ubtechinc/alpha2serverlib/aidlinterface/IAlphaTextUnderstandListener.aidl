package com.ubtechinc.alpha2serverlib.aidlinterface;
/**
 * [讯飞语音主服务 回调监听 ] 
 * 
 * @author zengdengyi
 * 
 **/
interface IAlphaTextUnderstandListener{   
/** 错误代码 **/
void onAlpha2UnderStandError(int nErrorCode);
/** 语义识别结果 **/
void onAlpha2UnderStandTextResult(String strResult);
}  