package com.example.xinweather.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.xinweather.mode.City;
import com.example.xinweather.mode.County;
import com.example.xinweather.mode.Province;
import com.example.xinweather.mode.XinWeatherDB;

public class Utility {
	/**
	 * �����ʹ�����������ص�ʡ������
	 */
	public synchronized static boolean handleProvincesResponse(XinWeatherDB 
			xinWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0){
				for(String p:allProvinces){
					String[] array=p.split("\\|");
					Province province=new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					//���������������ݴ洢��Province��
					xinWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * �����ʹ�����������ص��м�����
	 */
	public synchronized static boolean handleCitiesResponse(XinWeatherDB 
			xinWeatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities=response.split(",");
			if(allCities!=null&&allCities.length>0){
				for(String c:allCities){
					String[] array=c.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//���������������ݴ洢��City��
					xinWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * �����ʹ�����������ص��ؼ�����
	 */
	public synchronized static boolean handleCountiesResponse(XinWeatherDB 
			xinWeatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties=response.split(",");
			if(allCounties!=null&&allCounties.length>0){
				for(String c:allCounties){
					String[] array=c.split("\\|");
					County county=new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					//���������������ݴ洢��City��
					xinWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * �������������ص�JSON���ݣ��������������ݴ洢������
	 */
	public static void handleWeatherResponse(Context context,String response){
		try{
			Log.d("Tag","handleWeatherResponse1");
			JSONObject jsonObject=new JSONObject(response);
			Log.d("Tag","handleWeatherResponse2");
			JSONObject weatherInfo=jsonObject.getJSONObject("weatherinfo");
			Log.d("Tag","handleWeatherResponse3");
			String countyName=weatherInfo.getString("city");
			Log.d("Tag","handleWeatherResponse4+countyName="+countyName);
			String weatherCode=weatherInfo.getString("cityid");
			Log.d("Tag","handleWeatherResponse5"+weatherCode);
			String temp1=weatherInfo.getString("temp1");
			Log.d("Tag","handleWeatherResponse6"+temp1);
			String temp2=weatherInfo.getString("temp2");
			Log.d("Tag","handleWeatherResponse7"+temp2);
			String weatherDesp=weatherInfo.getString("weather");
			Log.d("Tag","handleWeatherResponse8"+weatherDesp);
			String publishTime=weatherInfo.getString("ptime");
			Log.d("Tag","handleWeatherResponse9"+publishTime);
			saveWeatherInfo(context,countyName,weatherCode,temp1,temp2,
					weatherDesp,publishTime);
			Log.d("Tag","handleWeatherResponse10");
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	private static void saveWeatherInfo(Context context, String countyName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
		SharedPreferences.Editor editor=PreferenceManager.
				getDefaultSharedPreferences(context).edit();
		editor.putBoolean("county_seleted", true);
		editor.putString("county_name",countyName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
		Log.d("Tag","saveWeatherInfo succeed");
	}
}
