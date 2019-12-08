package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")

    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
    @SerializedName("fl")
    public String fl_1;
    @SerializedName("wind_dir")
    public String wind_dir_1;
    @SerializedName("wind_sc")
    public String wind_sc_1;
    @SerializedName("hum")
    public String hum_1;
    @SerializedName("pres")
    public String pres_1;
}
