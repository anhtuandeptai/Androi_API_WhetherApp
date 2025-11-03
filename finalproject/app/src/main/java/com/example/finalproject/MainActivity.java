package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.finalproject.databinding.ActivityMainBinding;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WeatherAdapter adapter;
    private List<WeatherData> weatherList;
    private Retrofit retrofit;
    private static final int REQUEST_CODE_MAP = 1;
    private double lastSelectedLatitude = 0.0;
    private double lastSelectedLongitude = 0.0;
    private boolean hasSelectedLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Khởi tạo RecyclerView (từ code cải tiến)
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        weatherList = new ArrayList<>();
        adapter = new WeatherAdapter(weatherList);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setVisibility(View.GONE);

        // Khởi tạo Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Xử lý padding cho system bars (từ code gốc)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập tìm kiếm và nhấp vào biểu tượng vị trí
        SearchCity();
        setupLocationClick();

        // Kiểm tra quyền vị trí (từ code cải tiến)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            // Quyền đã được cấp, thực hiện hành động
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MAP);
        }
    }

    private void setupLocationClick() {
        binding.locationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            if (hasSelectedLocation) {
                intent.putExtra("selectedLatitude", lastSelectedLatitude);
                intent.putExtra("selectedLongitude", lastSelectedLongitude);
            }
            startActivityForResult(intent, REQUEST_CODE_MAP);
        });
    }

    private void changeTextColor(ViewGroup parent, int color) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof SearchView) continue;
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                changeTextColor((ViewGroup) child, color);
            }
        }
    }

    private void SearchCity() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    fetchWeatherData(query);
                    callApi(query); // Gọi API dự báo (từ code cải tiến)
                    NewWord(); // Giữ chức năng từ mới từ code gốc
                    hasSelectedLocation = false; // Reset tọa độ khi tìm kiếm bằng tên
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(binding.searchView.getWindowToken(), 0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);
            lastSelectedLatitude = latitude;
            lastSelectedLongitude = longitude;
            hasSelectedLocation = true;

            fetchWeatherDataFromCoordinates(latitude, longitude);
        } else if (!hasSelectedLocation) {
            fetchWeatherData("Hanoi");
            callApi("Hanoi");
        }
    }

    private void fetchWeatherData(String cityName) {
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<WeatherApp> call = apiInterface.getWeatherData(cityName, "33854e7d22e606a3bff25cc6364bc557", "metric");
        call.enqueue(new Callback<WeatherApp>() {
            @Override
            public void onResponse(Call<WeatherApp> call, Response<WeatherApp> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherApp weatherData = response.body();
                    updateWeatherUI(weatherData);
                    thaydoianhnentheothoitiet(weatherData.getWeather().get(0).getMain(), weatherData.getWeather().get(0).getIcon());
                    generateWeatherMessage(weatherData); // Tích hợp thông điệp từ code gốc
                } else {
                    Toast.makeText(MainActivity.this, "Weather API Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherApp> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Weather API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeatherDataFromCoordinates(double lat, double lon) {

        NewWord();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        apiInterface.getWeatherDataByCoordinates(lat, lon, "33854e7d22e606a3bff25cc6364bc557", "metric")
                .enqueue(new Callback<WeatherApp>() {
                    @Override
                    public void onResponse(Call<WeatherApp> call, Response<WeatherApp> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherApp weatherData = response.body();
                            updateWeatherUI(weatherData);
                            thaydoianhnentheothoitiet(weatherData.getWeather().get(0).getMain(), weatherData.getWeather().get(0).getIcon());
                            generateWeatherMessage(weatherData);
                        } else {
                            Toast.makeText(MainActivity.this, "Weather API Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherApp> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Weather API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        apiInterface.getForecastDataByCoordinates(lat, lon, "33854e7d22e606a3bff25cc6364bc557", "metric")
                .enqueue(new Callback<Forecast>() {
                    @Override
                    public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if (response.isSuccessful() && response.body() != null) {
                            processWeatherData(response.body());
                        } else {
                            Toast.makeText(MainActivity.this, "Forecast Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Forecast> call, Throwable t) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Forecast Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWeatherUI(WeatherApp weather) {
        try {
            binding.temp.setText(String.format("%d°C", (int) weather.getMain().getTemp()));
            binding.Humidity.setText(String.format("%d%%", (int) weather.getMain().getHumidity()));
            binding.windSpeed.setText(String.format("%.1fm/s", weather.getWind().getSpeed()));
            binding.sunRise.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                    .format(new Date(weather.getSys().getSunrise() * 1000)));
            binding.sunset.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                    .format(new Date(weather.getSys().getSunset() * 1000)));
            binding.sea.setText(String.format("%dhPa", (int) weather.getMain().getSeaLevel()));
            binding.tempMax.setText("Max: " + (int) weather.getMain().getTempMax() + "°C");
            binding.tempMin.setText("Min: " + (int) weather.getMain().getTempMin() + "°C");
            binding.condition.setText(weather.getWeather().get(0).getMain());
            binding.date.setText(new SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(new Date()));
            binding.day.setText(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date()));
            binding.cityName.setText(weather.getName() + ", " + weather.getSys().getCountry());
            binding.weather.setText(weather.getWeather().get(0).getDescription());

            String iconUrl = "https://openweathermap.org/img/w/" + weather.getWeather().get(0).getIcon() + ".png";

        } catch (Exception e) {
            Toast.makeText(this, "Data Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void callApi(String cityName) {
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        apiInterface.getForecastData(cityName, "33854e7d22e606a3bff25cc6364bc557", "metric")
                .enqueue(new Callback<Forecast>() {
                    @Override
                    public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if (response.isSuccessful() && response.body() != null) {
                            processWeatherData(response.body());
                        } else {
                            Toast.makeText(MainActivity.this, "Forecast Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Forecast> call, Throwable t) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Forecast Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processWeatherData(Forecast forecast) {
        weatherList.clear();
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fullDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat localDateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        localDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String lastDate = "";
        List<Forecast.ForecastItem> dailyItems = new ArrayList<>();
        for (Forecast.ForecastItem item : forecast.list) {
            try {
                Date date = fullDateFormat.parse(item.dt_txt);
                String formattedDate = localDateFormat.format(date);

                if (!formattedDate.equals(lastDate) && !dailyItems.isEmpty()) {
                    processDailyData(dailyItems, lastDate);
                    dailyItems.clear();
                }
                dailyItems.add(item);
                lastDate = formattedDate;

                if (weatherList.size() >= 5) break;
            } catch (Exception e) {
                Toast.makeText(this, "Date Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        if (!dailyItems.isEmpty()) processDailyData(dailyItems, lastDate);
        adapter.notifyDataSetChanged();
    }

    private void processDailyData(List<Forecast.ForecastItem> dailyItems, String date) {
        float maxTemp = Float.MIN_VALUE, minTemp = Float.MAX_VALUE, maxPop = 0f;
        String weatherIcon = "01d", weatherDescription = "Clear";

        for (Forecast.ForecastItem item : dailyItems) {
            float currentTemp = item.main.temp;
            maxTemp = Math.max(maxTemp, currentTemp);
            minTemp = Math.min(minTemp, currentTemp);
            maxPop = Math.max(maxPop, item.pop);
            weatherIcon = item.weather.get(0).icon;
            if (item.pop == maxPop) weatherDescription = item.weather.get(0).description;
        }

        weatherList.add(new WeatherData(date, weatherIcon, weatherDescription, maxPop, maxTemp, minTemp));
    }

    private void thaydoianhnentheothoitiet(String mainWeather, String icon) {
        boolean isNight = icon.endsWith("n");
        ViewGroup root = findViewById(android.R.id.content);
        int backgroundRes = isNight ? R.drawable.default_night_background : R.drawable.default_background;
        int animationRes = isNight ? R.raw.moon : R.raw.sun;

        switch (mainWeather.toLowerCase()) {
            case "clear":
            case "sunny":
                String temp = binding.temp.getText().toString();
                if (isNight) {
                    backgroundRes = R.drawable.clear_night_background;
                    animationRes = R.raw.moon;
                } else if (!temp.isEmpty() && Integer.parseInt(temp.substring(0, temp.length() - 2)) > 30) {
                    backgroundRes = R.drawable.sunny_background;
                    changeTextColor(root, Color.BLACK);
                    animationRes = R.raw.sun;
                } else {
                    backgroundRes = R.drawable.clear_sky;
                    animationRes = R.raw.sun;
                }
                break;
            case "partly cloudy":
                backgroundRes = isNight ? R.drawable.partly_cloudy_night : R.drawable.partly_cloudy_day;
                animationRes = R.raw.cloud;
                break;
            case "clouds":
                backgroundRes = isNight ? R.drawable.cloudy_night_background : R.drawable.cloud_background;
                animationRes = R.raw.cloud;
                break;
            case "rain":
            case "drizzle":
                backgroundRes = isNight ? R.drawable.rain_night_background : R.drawable.rain_background;
                animationRes = R.raw.rain;
                break;
            case "thunderstorm":
                backgroundRes = R.drawable.storm_background;
                animationRes = R.raw.storm;
                break;
            case "snow":
                backgroundRes = R.drawable.snow_background;
                animationRes = R.raw.snow;
                break;
            case "mist":
            case "fog":
            case "haze":
                backgroundRes = R.drawable.fog_background;
                animationRes = R.raw.fog;
                break;
        }
        binding.getRoot().setBackgroundResource(backgroundRes);
        binding.lottieAnimationView.setAnimation(animationRes);
        binding.lottieAnimationView.playAnimation();
    }

    private void generateWeatherMessage(WeatherApp weatherData) {
        GenerativeModel gm = new GenerativeModel("gemini-2.0-flash-001", "AIzaSyDtCBBs8Hv-JfOkAwEzy1rk-TQVEE0f59s");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        String description = weatherData.getWeather().get(0).getDescription();
        String visibility = String.valueOf(weatherData.getVisibility());
        String cityName = weatherData.getName();
        boolean isNight = weatherData.getWeather().get(0).getIcon().endsWith("n");
        String night = isNight ? "Đêm" : "Ngày";
        int temp = (int) weatherData.getMain().getTemp();
        int humidity = (int) weatherData.getMain().getHumidity();
        double windSpeed = weatherData.getWind().getSpeed();
        String sunrise = binding.sunRise.getText().toString();
        String sunset = binding.sunset.getText().toString();

        Content content = new Content.Builder()
                .addText("Hiện tại bây giờ đang là " + night + " và đây là dữ liệu của thành phố " + cityName + ". Dựa trên dữ liệu thời tiết hiện tại và thời gian trong ngày, và món ăn đặc sản của thành phố ấy bắt đầu bằng gạch đầu dòng  tạo một đoạn văn tối đa 30 từ, mang lại sự ấm áp, thực tế và hữu ích. Nếu trời lạnh, nhắc giữ ấm; nếu nóng, gợi ý giữ mát; nếu gió mạnh hoặc tầm nhìn kém, khuyên cẩn thận khi di chuyển. Kết thúc bằng emoji phù hợp.\n\n" +
                        "🌡️ Nhiệt độ: " + temp + "°C\n" +
                        "☁️ Thời tiết: " + description + " (" + weatherData.getWeather().get(0).getMain() + ")\n" +
                        "💧 Độ ẩm: " + humidity + "%\n" +
                        "🌬️ Gió: " + windSpeed + "m/s\n" +
                        "👀 Tầm nhìn: " + visibility + "m\n" +
                        "🌅 Mặt trời mọc: " + sunrise + " | 🌇 Lặn: " + sunset)
                .build();

        ListenableFuture<GenerateContentResponse> responseAPI = model.generateContent(content);
        Futures.addCallback(responseAPI, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                binding.ngao.setText(result.getText());
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, command -> command.run());
    }

    private void NewWord() {
        GenerativeModel gm = new GenerativeModel("gemini-2.0-flash-001", "AIzaSyDtCBBs8Hv-JfOkAwEzy1rk-TQVEE0f59s");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText("  Tạo một mục từ vựng tiếng Anh ( thuộc trình độ C1 ) ngẫu nhiên:\n\n"
                        + " kết quả trả vêf có dạng đúng như này , không thêm không bớt  \n\n"
                        + "- Từ mới: (Một từ ngẫu nhiên, thông dụng, dưới 10 ký tự)\n"
                        + "- Phát âm: (Phiên âm theo tiếng Anh-Mỹ, sử dụng từ điển Cambridge)\n"
                        + "- Nghĩa: (Giải thích ngắn gọn bằng tiếng Việt, tối đa 10 từ)\n"
                        + "- Loại từ: (danh từ , động từ , trạng từ , tính từ , bổ ngữ , đại từ , vân vân)\n"
                        + "- Ví dụ: (Một câu đơn giản minh họa cách dùng từ, tối đa 15 từ)\n\n"
                        + " kết quả trả vêf có dạng đúng như này , không thêm không bớt  \n\n"
                        +"món ăn đặc sản của thành phố "
                        + "Ví dụ:\n"
                        + "- Từ mới: Happy\n"
                        + "- Phát âm: /ˈhæpi/\n"
                        + "- Nghĩa: Vui vẻ, hạnh phúc\n"
                        + "- Loại từ: tính từ\n"
                        + "- Ví dụ: She looks very happy today.\n"
                        + "Nem Lụi "
                )


                .build();

        ListenableFuture<GenerateContentResponse> responseAPI = model.generateContent(content);
        Futures.addCallback(responseAPI, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                binding.txtnewword.setText(result.getText());
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, command -> command.run());
    }
}