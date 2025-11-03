package com.example.finalproject;

import java.util.List;

public class Forecast {
    public String cod;
    public int message;
    public int cnt;
    public List<ForecastItem> list;
    public City city;

    public static class ForecastItem {
        public long dt;
        public Main main;
        public List<Weather> weather;
        public float pop; // Khả năng mưa
        public String dt_txt;
    }

    public static class Main {
        public float temp;
        public float temp_min;
        public float temp_max;
        public int humidity;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class City {
        public String name;
        public String country;
    }
}