package com.ubtechinc.alpha2serverlib.aidlinterface;

import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2XmppCallBack;
interface IAlpha2XmppListener{   

/**注册XMPP消息回调接口**/
int registerXmppCallBackListener(String appID, IAlpha2XmppCallBack callBack);

/**注销XMPP消息回调接口**/
int unRegisterXmppCallBackListener(IAlpha2XmppCallBack callBack);

/**发送第三方APP的XMPP消息**/
void sendCustomXmppMessage(int type, String appID, String message);
}