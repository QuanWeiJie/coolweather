package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0 ;
    public static final int LEVEL_CITY=1 ;
    public static final int LEVEL_COUNTY=2 ;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;

    @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.d("ChooseAreaFragment","onCreateView");
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView)view.findViewById(R.id.title_text);  //获取标题栏文本id
        backButton = (Button) view.findViewById(R.id.back_button);  //获取标题栏id
        listView = (ListView)view.findViewById(R.id.list_view);    //获取Item列表id
        //获取ArrayAdapter对象
        adapter =new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);//设置并初始化适配器
        return view;//将视图返回
    }
    /*点击事件集合*/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("ChooseAreaFragment","onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        //列表任意一栏被点击，则...
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ChooseAreaFragment","列表被点了的...");
                if (currentLevel == LEVEL_PROVINCE){   //当前选中的级别为省份时
                    selectedProvince = provinceList.get(position);  //当前点击为选中状态
                    queryCities();//查询市的方法
                }
                else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){  //如果当前级别是Country就启动WeatherActivity,并把当前选中的天气ID传递过去
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.refreshLayout.finishRefresh(1000);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }
                else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    /*全国所有的省，优先查询数据库，如果没有再去服务器查询*/
    private void queryProvinces() {
        titleText.setText("中国");
        Log.d("ChooseAreaFragment","查询省中...");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        else {
            Log.d("ChooseAreaFragment","服务器查询省中...");
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /*查询选中省内的所有的城市，优先查询数据库，如果没有再去服务器查询*/
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());  //设置市的标题内容
        backButton.setVisibility(View.VISIBLE);  //设置返回按钮可见
        //查询被选中的省份城市的市区
        cityList = LitePal.where("provinceId=?",String.valueOf(selectedProvince.
                getId())).find(City.class);
        Log.d("ChooseAreaFragment","市级");
        if (cityList.size()>0){ //如果省列表不为空，则...
            dataList.clear();
            for (City city:cityList){ //遍历每一份省的市级城市
                dataList.add(city.getCityName()); //添加到数据列表中
            }
            adapter.notifyDataSetChanged();//通知适配器数据更新了
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else{
            int provinceCode = selectedProvince.getProvinceCode();  //获取被选取省级代码
            String address = "http://guolin.tech/api/china/"+provinceCode;//获取被选取地区的网络地址
            Log.d("ChooseAreaFragment","准备在网络中获取地址信息");
            queryFromServer(address,"city");   // 在网络中查询
        }
    }
    /*查询选中市内所有的县，优先查询数据库，如果没有再去服务器查询*/
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /*根据传入的地址和类型从服务器查询省市县数据*/
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        //   发送一条网络请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            //请求加载失败
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread方法回到主线程逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("ChooseAreaFragment","加载地区信息...");
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ChooseAreaFragment","开启线程更新UI");
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }
                            else if ("city".equals(type)){
                                queryCities();
                            }
                            else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }




            }
        });
    }

    /*显示进度对话框*/
    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);

        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

 }
