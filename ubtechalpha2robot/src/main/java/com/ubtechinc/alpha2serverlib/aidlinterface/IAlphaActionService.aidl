package com.ubtechinc.alpha2serverlib.aidlinterface;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionClient;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlphaActionListListener;

interface IAlphaActionService {
	int registerActionClient(IAlphaActionClient client);
	void unRegisterActionClient(IAlphaActionClient client);
	/** 播放动作文件**/
	boolean playActionFile(String strActionFile);
	/** 播放动作名 **/
	boolean playActionName(String strActionName);
	void stopActionPlay();
	void onEventHandlerTrigger(int nEventType, in byte[] param);
	boolean isCompleted();
    void getActionList(IAlphaActionListListener listener);
}