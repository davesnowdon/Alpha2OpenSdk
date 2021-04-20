package com.ubtechinc.alpha2serverlib.aidlinterface;

/**
* 讯飞语法识别初始化回调函数
*/
interface ISpeechGrammarInitListener {
	/**讯飞初始化回调**/
	void speechGrammarInitCallback(String grammarID, int nErrorCode);
}