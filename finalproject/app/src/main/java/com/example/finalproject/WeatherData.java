package com.example.finalproject;

public class WeatherData {
    private String date;
    private String weatherIcon;
    private String weatherDescription; // Thêm mô tả thời tiết
    private float rainChance; // Giữ lại để tương thích, có thể bỏ nếu không cần
    private float tempMax;
    private float tempMin;

    public WeatherData(String date, String weatherIcon, String weatherDescription, float rainChance, float tempMax, float tempMin) {
        this.date = date;
        this.weatherIcon = weatherIcon;
        this.weatherDescription = weatherDescription;
        this.rainChance = rainChance;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
    }

    // Getter
    public String getDate() { return date; }
    public String getWeatherIcon() { return weatherIcon; }
    public String getWeatherDescription() { return weatherDescription; } // Getter mới
    public float getRainChance() { return rainChance; } // Giữ lại getter
    public float getTempMax() { return tempMax; }
    public float getTempMin() { return tempMin; }
}