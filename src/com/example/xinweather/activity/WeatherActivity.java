package com.example.xinweather.activity;

import com.example.xinweather.R;
import com.example.xinweather.server.AutoUpdateService;
import com.example.xinweather.utils.HttpCallbackListener;
import com.example.xinweather.utils.HttpUtils;
import com.example.xinweather.utils.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	
	private TextView countyName;
	
	private TextView publishText;
	
	private TextView weatherDespText;
	
	private TextView temp1;
	
	private TextView temp2;
	
	private TextView currentDateText;
	
	private Button refreshWeather;
	
	private Button switchCounty;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化控件
		init();
		
		String countyCode=getIntent().getStringExtra("county_code");
		Log.d("Tag","countyCode1="+countyCode);
		if(!TextUtils.isEmpty(countyCode)){
			//有县级代号就去查询天气
			publishText.setText("同步中...");
			Log.d("Tag","message[1]");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			Log.d("Tag","message[2]");
			countyName.setVisibility(View.INVISIBLE);
			Log.d("Tag","message[3]");
			queryWeatherCode(countyCode);
			Log.d("Tag","message[4]");
			//showWeather();
			Log.d("Tag","message[41]");
		}else{
			Log.d("Tag","message[5]");
			showWeather();
			Log.d("Tag","message[6]");
		}
		switchCounty.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fresh:
			Log.d("Tag","message[7]");
			publishText.setText("同步中...");
			SharedPreferences prefs=PreferenceManager.
					getDefaultSharedPreferences(this);
			String weatherCode=prefs.getString("weather_code", "");
			Log.d("Tag","message[71]");
			Log.d("Tag","weatherCode="+weatherCode);
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
				Log.d("Tag","message[72]");
			}
			break;
		
		case R.id.select_county:
			Log.d("Tag","message[8]");
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			Log.d("Tag","message[81]");
			startActivity(intent);
			Log.d("Tag","message[82]");
			finish();
			Log.d("Tag","message[83]");
			
			break;

		default:
			break;
		}
	}
	
	/**
	* 查询县级代号所对应的天气代号。
	*/
	private void queryWeatherCode(String countyCode) {
	String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
	queryFromServer(address, "countyCode");
	Log.d("Tag","message[11]");
	}
	
	/**
	* 查询天气代号所对应的天气。
	*/
	private void queryWeatherInfo(String weatherCode) {
	String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
	queryFromServer(address, "weatherCode");
	Log.d("Tag","message[12]weatherCode="+weatherCode);
	}
	
	private void queryFromServer(final String address, final String type) {
		// TODO Auto-generated method stub
		HttpUtils.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {
				// TODO Auto-generated method stub
				Log.d("Tag","message[13]");
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//从服务器返回的数据中解析出天气代号
						Log.d("Tag","message[14]");
						String[] array=response.split("\\|");
						Log.d("Tag","array[0]="+array[0]);
						Log.d("Tag","array[1]="+array[1]);
						if(array!=null&&array.length==2){
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
							Log.d("Tag","message[15]");
						}
					}
				}else if("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									showWeather();
									Log.d("Tag","message[16]");
								}
							});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishText.setText("同步失败");
					}
				});
			}
		});
	}



	private void showWeather() {
		// TODO Auto-generated method stub
		SharedPreferences prefs=PreferenceManager
				.getDefaultSharedPreferences(this);
		countyName.setText(prefs.getString("county_name", ""));
		temp1.setText(prefs.getString("temp1", ""));
		temp2.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time", "")
				+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		countyName.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this,AutoUpdateService.class);
		startService(intent);
	}
	
	/**
	 * 初始化控件
	 */
	private void init(){
		weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
		countyName=(TextView)findViewById(R.id.county_name);
		publishText=(TextView)findViewById(R.id.publish_text);
		weatherDespText=(TextView)findViewById(R.id.weather_desp);
		temp1=(TextView)findViewById(R.id.temp1);
		temp2=(TextView)findViewById(R.id.temp2);
		currentDateText=(TextView)findViewById(R.id.current_date);
		
		switchCounty=(Button)findViewById(R.id.select_county);
		refreshWeather=(Button)findViewById(R.id.fresh);
		

		
	}
	
	

}
