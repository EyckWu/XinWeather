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
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/**
	 * ���б�
	 */
	private List<City> cityList;
	/**
	 * ���б�
	 */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectCity;
	/**
	 * ѡ�еļ���
	 */
	private int currentLever;
	
	private boolean isFromWeatherActivity;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        /**
         * �ж�currentLever��״̬�����Ϊcounty_selected��ֱ�ӵ�ת��������ʾҳ��
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
	* ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
			titleText.setText("�й�");
			currentLever=LEVER_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
	}
	

	/**
	 * ��ѯѡ��ʡ�����еĳ��У����ȴ����ݿ���ң����û���ٵ���������ѯ	
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
	 *��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ 
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
					//ͨ��runOnUiThread()�����ص����̴߳����߼�
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
				// ͨ��runOnUiThread()�����ص����߳�
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, 
								"����ʧ��", Toast.LENGTH_SHORT).show();
					}
					
				});
			}
		});
	}
	
	
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog .setMessage("���ڼ���");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/*
	 * �رս�����
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/**
	 * ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳���
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
