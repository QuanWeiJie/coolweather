package com.coolweather.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static java.lang.System.in;
import static java.lang.System.load;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;  //气温

    private TextView weatherInfoText;  //天气概况
private  TextView BodyFeel;
private TextView Water;
private  TextView DirectionText;
private TextView DirectionDegree;;
private TextView Pressure;
    private  ImageView nowImage; //天气图片
    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private Button comfortText;

    private Button carWashText;

    private Button sportText;

    private ImageView bingPicImg;
private TextView Aqi_status;

    public RefreshLayout refreshLayout;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;
    String comfort,carWash,sport;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
       //初始化
        initView();
        refreshLayout = (RefreshLayout)findViewById(R.id.swipe_refresh);
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
         drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         navButton = (Button)findViewById(R.id.nav_btn);
        //定义缓存对象
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weather_id;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气信息
             mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(mWeatherId);
    }
       /* String bingPic = prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }*/
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestWeather(mWeatherId);
                //refreshLayout.finishRefresh(2000/*,false*/);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                Toast.makeText(WeatherActivity.this, "尽情期待",
                        Toast.LENGTH_SHORT).show();
                refreshLayout.finishLoadMore(1000);
                //refreshLayout.finishLoadMore(2000/*,false*/);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
         comfortText.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 AlertDialog.Builder dialog = new AlertDialog.Builder(WeatherActivity.this);
                 dialog.setTitle("生活指数");
                 dialog.setMessage(comfort);
                 dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {

                     }
                 });
                 dialog.show();
             }
         });
         carWashText.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 AlertDialog.Builder  dialog = new AlertDialog.Builder(WeatherActivity.this);
                 dialog.setTitle("洗车指数");
                 dialog.setMessage(carWash);
                 dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {

                     }
                 });
                 dialog.show();
             }
         });
         sportText.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 AlertDialog.Builder dialog1 = new AlertDialog.Builder(WeatherActivity.this);
                 dialog1.setTitle("运动指数");
                 dialog1.setMessage(sport);
                 dialog1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {

                     }
                 });
                 dialog1.show();
             }
         });




     }
    /*根据天气ID请求天气信息*/
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=a88abd8ee7924e9da1346cdd22ef760b";

         HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
             @Override
             public void onResponse(Call call, Response response) throws IOException {
                 final String responseText = response.body().string();
                 Log.d("a123",responseText);
                 final Weather weather = Utility.handleWeatherResponse(responseText);
                 //   Log.e("获取天气信息失败",responseText);
                 //将当前线程切换到主线程
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         if (weather != null && "ok".equals(weather.status)) {
                             SharedPreferences.Editor editor = PreferenceManager.
                                     getDefaultSharedPreferences(WeatherActivity.this).edit();
                             editor.putString("weather", responseText);
                             editor.apply();
                             showWeatherInfo(weather);
                         } else {
                             Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                     Toast.LENGTH_SHORT).show();
                         }
                         refreshLayout.finishRefresh(1000);
                     }
                 });
             }

             @Override
             public void onFailure(Call call, IOException e) {
                 e.printStackTrace();
                 runOnUiThread(new Runnable() {

                     @Override
                     public void run() {
                         Toast.makeText(WeatherActivity.this, "从网上获取天气信息失败",
                                 Toast.LENGTH_SHORT).show();
                         refreshLayout.finishRefresh();
                     }
                 });
             }
         });
        // loadBingPic();
    }

    //缓存数据下处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        Toast.makeText(WeatherActivity.this, "展示数据",
                Toast.LENGTH_SHORT).show();

      String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1]; //split：分解
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        String bodyfeel = weather.now.fl_1+"°C";
        String water = weather.now.hum_1+"%";
        String direction = weather.now.wind_dir_1;
        String directionDegree = weather.now.wind_sc_1+"级";
        String pressure = weather.now.pres_1+"hPa";
        DirectionText.setText(direction);
        DirectionDegree.setText(directionDegree);
        Water.setText(water);
        BodyFeel.setText(bodyfeel);
        Pressure.setText(pressure);
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
       weatherInfoText.setText(weatherInfo);
      switch (weatherInfo){
          case "晴": nowImage.setImageResource(R.drawable.sunny); break;
          case "多云":nowImage.setImageResource(R.drawable.cloud); break;
          case "阴":nowImage.setImageResource(R.drawable.cloudy); break;
          case "小雨":nowImage.setImageResource(R.drawable.littlerain); break;
          case "中雨":nowImage.setImageResource(R.drawable.rainy); break;
          case "阵雨":nowImage.setImageResource(R.drawable.heavyrain2); break;
          case "雷震雨":nowImage.setImageResource(R.drawable.heavyrain2); break;
          default: nowImage.setImageResource(R.drawable.sunny); break;
      }
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);//动态加载布局
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            ImageView imageView = (ImageView)view.findViewById(R.id.info_Image);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
         switch (forecast.more.info) {
                case "晴":
                    imageView.setImageResource(R.drawable.sunny);
                    break;
                case "多云":
                    imageView.setImageResource(R.drawable.cloud);
                    break;
                case "阴":
                    imageView.setImageResource(R.drawable.cloudy);
                    break;
                case "小雨":imageView.setImageResource(R.drawable.littlerain); break;
                case "中雨":imageView.setImageResource(R.drawable.rainy); break;
                case "阵雨":imageView.setImageResource(R.drawable.heavyrain); break;
                case "雷震雨":imageView.setImageResource(R.drawable.heavyrain); break;
                default: imageView.setImageResource(R.drawable.sunny); break;
            }
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            Aqi_status.setText(weather.aqi.city.qlty);
        }
        String comfortBref = weather.suggestion.comfort.status;
        String carWashBref = weather.suggestion.carWash.status;
        String sportBref = weather.suggestion.sport.status;
      comfort = "舒适度：" + weather.suggestion.comfort.info;
      carWash = " 洗车指数：" + weather.suggestion.carWash.info;
      sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfortBref);
        carWashText.setText(carWashBref);
        sportText.setText(sportBref);
        weatherLayout.setVisibility(View.VISIBLE);//将ScrollView重新变成可见
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    //初始化各个控件
    private void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        nowImage = (ImageView)findViewById(R.id.now_image);
        BodyFeel = (TextView) findViewById(R.id.TiGanText);
        Water = (TextView)findViewById(R.id.waterText);
        DirectionText = (TextView)findViewById(R.id.directionText);
DirectionDegree = (TextView) findViewById(R.id.directionDegree);
Pressure = (TextView)findViewById(R.id.pressureText);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (Button) findViewById(R.id.comfort_text);
        carWashText = (Button) findViewById(R.id.car_wash_text);
        sportText = (Button) findViewById(R.id.sport_text);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
Aqi_status = (TextView)findViewById(R.id.aqi_status);
    }

    @Override
    public void onClick(View view) {

    }

   /* private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }*/
}
