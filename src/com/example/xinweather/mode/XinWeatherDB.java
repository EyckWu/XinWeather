package com.example.xinweather.mode;

import java.util.ArrayList;
import java.util.List;

import com.example.xinweather.db.XinWeatherOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class XinWeatherDB {

	/*
	 * �������ݿ���
	 */
	public static final String DB_NAME="xin_weather";
	/*
	 * �������ݿ�汾��
	 */
	public static final int DB_VERSION=1;
	public static XinWeatherDB xinWeatherDB;
	public SQLiteDatabase db;
	
	//˽�л����췽��
	private XinWeatherDB(Context context){
		XinWeatherOpenHelper xinWeatherOpenHelper=new 
				XinWeatherOpenHelper(context, DB_NAME, null, DB_VERSION);
		db=xinWeatherOpenHelper.getWritableDatabase();
	}
	//��ȡXinWeatherDBʵ��
	public synchronized static XinWeatherDB getInstance(Context context){
		if(xinWeatherDB==null){
			xinWeatherDB=new XinWeatherDB(context);
		}
		return xinWeatherDB;
	}
	/*
	 * ��Provinceʵ���洢�����ݿ���
	 */
	public void saveProvince(Province province){
		if(province!=null){
			ContentValues values=new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	/*
	 * �����ݿ��ȡȫ������ʡ�ݵ���Ϣ
	 */
	public List<Province> loadProvince(){
		List<Province> listProvince =new ArrayList<Province>();
		Cursor pCursor=db.
				query("Province", null, null, null, null, null,null);
		if(pCursor.moveToFirst()){
			while(pCursor.moveToNext()){
				Province province=new Province();
				province.setId(pCursor.getInt(pCursor.
						getColumnIndex("id")));
				province.setProvinceName(pCursor.getString(pCursor.
						getColumnIndex("province_name")));
				province.setProvinceCode(pCursor.getString(pCursor.
						getColumnIndex("province_code")));
				listProvince.add(province);
			}
		}
		return listProvince;
	}
	/*
	 * ��Cityʵ���洢�����ݿ���
	 */
	public void saveCity(City city){
		if(city!=null){
			ContentValues values=new ContentValues();
			values.put("city_name",city.getCityName());
			values.put("city_code",city.getCityCode());
			values.put("province_id",city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	/*
	 * �����ݿ��ȡȫʡ������Ϣ
	 */
	public List<City> loadCity(int provinceId){
		List<City> listCity=new ArrayList<City>();
		Cursor cCursor=db.query("City", null,  "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null, null);
		if(cCursor.moveToFirst()){
			while(cCursor.moveToNext()){
				City city=new City();
				city.setId(cCursor.getInt(cCursor.
						getColumnIndex("id")));
				city.setCityName(cCursor.getString(cCursor.
						getColumnIndex("city_name")));
				city.setCityCode(cCursor.getString(cCursor.
						getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				listCity.add(city);
			}
		}
		return listCity;
	}
	/*
	 * ��Countyʵ���洢�����ݿ���
	 */
	public void saveCounty(County county){
		if(county!=null){
			ContentValues values=new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}
	}
	/*
	 * �����ݿ��ȡĳ�������ص���Ϣ
	 */
	public List<County> loadCounty(int cityId){
		List<County>  listCounty=new ArrayList<County>();
		Cursor cCursor=db.query("County", null, "city_id = ?",
				new String[] { String.valueOf(cityId) }, null, null, null, null);
		if(cCursor.moveToFirst()){
			while(cCursor.moveToNext()){
				County county=new County();
				county.setId(cCursor.getInt(cCursor.
						getColumnIndex("id")));
				county.setCountyName(cCursor.getString(cCursor.
						getColumnIndex("county_name")));
				county.setCountyCode(cCursor.getString(cCursor.
						getColumnIndex("county_code")));
				county.setCityId(cityId);
				listCounty.add(county);
			}
		}
		return listCounty;
	}
}
