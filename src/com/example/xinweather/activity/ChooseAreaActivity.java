package com.example.xinweather.activity;

import java.util.ArrayList;
import java.util.List;


import com.example.xinweather.R;
import com.example.xinweather.mode.City;
import com.example.xinweather.mode.County;
import com.example.xinweather.mode.Province;
import com.example.xinweather.mode.XinWeatherDB;
import com.example.xinweather.utils.HttpCallbackListener;
import com.example.xinweather.utils.HttpUtils;
import com.example.xinweather.utils.Utility;


import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{
	public static final int LEVER_PROVINCE=1;
	public static final int LEVER_CITY=2;
	public static final int LEVER_COUNTY=3;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private XinWeatherDB xinWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 县列表
	 */
	private List<County> countyList;
	/**
	 * 选中的省份
	 */
	private Province selectProvince;
	/**
	 * 选中的城市
	 */
	private City selectCity;
	/**
	 * 选中的级别
	 */
	private int currentLever;
	
	private boolean isFromWeatherActivity;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        /**
         * 判断currentLever的状态，如果为county_selected则直接调转到天气显示页面
         */
        isFromWeatherActivity = getIntent()
        		.getBooleanExtra("from_weather_activity", false);
        SharedPreferences prefs=PreferenceManager
        		.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("county_seleted", false)
        		&& !isFromWeatherActivity){
        	Intent intent=new Intent(this,WeatherActivity.class);
        	Log.d("Tag","message[8]+intent"+prefs.getBoolean("county_seleted", false));
        	startActivity(intent);
        	Log.d("Tag","message[8]+intents");
        	finish();
        	return;
        }
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this, 
        		android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        xinWeatherDB=XinWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0,
					View view, int index,
					long arg3) {
				if(currentLever==LEVER_PROVINCE){
					selectProvince=provinceList.get(index);
					queryCities();
				}else if(currentLever==LEVER_CITY){
					selectCity=cityList.get(index);
					queryCounties();
				}else if(currentLever==LEVER_COUNTY){
					String countyCode=countyList.get(index)
							.getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
				
			}
        	
        });
        queryProvinces();
    }
	/**
	* 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
	*/
	private void queryProvinces(){
		provinceList=xinWeatherDB.loadProvince();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLever=LEVER_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
	}
	

	/**
	 * 查询选中省份所有的城市，优先从数据库查找，如果没有再到服务器查询	
	 */
	private void queryCities(){
		cityList=xinWeatherDB.loadCity(selectProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectProvince.getProvinceName());
			currentLever=LEVER_CITY;
		}else{
			queryFromServer(selectProvince.getProvinceCode(),"city");
		}
	}
	/**
	 *查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询 
	 */
	private void queryCounties(){
		countyList=xinWeatherDB.loadCounty(selectCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectCity.getCityName());
			currentLever=LEVER_COUNTY;
		}else{
			queryFromServer(selectCity.getCityCode(),"county");
		}
	}
	
	private void queryFromServer(final String code,final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtils.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(xinWeatherDB, 
							response);
				}else if("city".equals(type)){
					result=Utility.handleCitiesResponse(xinWeatherDB, 
							response, selectProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountiesResponse(xinWeatherDB,
							response, selectCity.getId());
				}
				if(result){
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
							
						}

						
						
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, 
								"加载失败", Toast.LENGTH_SHORT).show();
					}
					
				});
			}
		});
	}
	
	
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog .setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/*
	 * 关闭进度条
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/**
	 * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
	 */
	public void onBackPressed(){
		if(currentLever==LEVER_COUNTY){
			queryCities();
		}else if(currentLever==LEVER_CITY){
			queryProvinces();
		}else{
			finish();
		}
	}
}
