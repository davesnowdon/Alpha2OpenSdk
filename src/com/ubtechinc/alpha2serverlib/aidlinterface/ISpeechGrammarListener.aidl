package com.ubtechinc.alpha2serverlib.aidlinterface;

/**
* 讯飞语法识别初始化回调函数
*/
interface ISpeechGrammarListener {
	/**语法识别结果返回 strResultType： 结果返回类型json, xml两种。 strResult：结果**/
	void onSpeechGrammarResult(String strResultType, String strResult);
	/** 语法识别错误返回码**/
	void onSpeechGrammarError(int nErrorCode);
}