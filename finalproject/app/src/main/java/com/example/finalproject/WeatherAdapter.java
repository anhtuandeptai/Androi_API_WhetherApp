package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private List<WeatherData> weatherList;

    public WeatherAdapter(List<WeatherData> weatherList) {
        this.weatherList = weatherList;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_item_layout, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        WeatherData weather = weatherList.get(position);
        holder.tvDate.setText(weather.getDate());
        holder.tvRainChance.setText(weather.getWeatherDescription()); // Thay pop bằng description
        holder.tvTempMax.setText(String.format("%.1f°C", weather.getTempMax()));
        holder.tvTempMin.setText(String.format("%.1f°C", weather.getTempMin()));

        String iconUrl = "http://openweathermap.org/img/wn/" + weather.getWeatherIcon() + ".png";
        Glide.with(holder.itemView.getContext())
                .load(iconUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivWeatherIcon);
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvRainChance, tvTempMax, tvTempMin;
        ImageView ivWeatherIcon;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            tvRainChance = itemView.findViewById(R.id.tvRainChance);
            tvTempMax = itemView.findViewById(R.id.tvTempMax);
            tvTempMin = itemView.findViewById(R.id.tvTempMin);
        }
    }
}