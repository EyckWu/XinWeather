package com.example.xinweather.utils;

public interface HttpCallbackListener {
	void onFinish(String response);
	void onError(Exception e); 
	
}
