package com.example.finalproject;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.example.finalproject.databinding.ActivityMapBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;
    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private boolean hasSelectedLocation = false;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    Toast.makeText(MapActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                    if (!hasSelectedLocation) {
                        enableLocationComponent();
                    }
                } else {
                    Toast.makeText(MapActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = v ->
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = point -> {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(17.0).build());
        getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector detector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector detector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector detector) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.mapView;
        floatingActionButton = binding.focusLocation;
        floatingActionButton.hide();

        // Nhận tọa độ từ Intent
        Intent intent = getIntent();
        if (intent.hasExtra("selectedLatitude") && intent.hasExtra("selectedLongitude")) {
            selectedLatitude = intent.getDoubleExtra("selectedLatitude", 0.0);
            selectedLongitude = intent.getDoubleExtra("selectedLongitude", 0.0);
            hasSelectedLocation = true;
        }

        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE, style -> {
            // Nếu có tọa độ được chọn, đặt vị trí bản đồ theo tọa độ đó
            if (hasSelectedLocation) {
                Point selectedPoint = Point.fromLngLat(selectedLongitude, selectedLatitude);
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .center(selectedPoint)
                        .zoom(17.0)
                        .build());
            }
            setupMapInteractions();
        });

        // Yêu cầu quyền vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            if (!hasSelectedLocation) {
                enableLocationComponent();
            }
        }
    }

    private void enableLocationComponent() {
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        locationComponentPlugin.setEnabled(true);
        LocationPuck2D locationPuck2D = new LocationPuck2D();
        locationPuck2D.setBearingImage(AppCompatResources.getDrawable(this, R.drawable.baseline_location_on_24));
        locationComponentPlugin.setLocationPuck(locationPuck2D);
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
        locationComponentPlugin.addOnIndicatorPositionChangedListener(point -> {
            // Cập nhật camera
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(17.0).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));

            // Lưu vị trí ban đầu nếu chưa có vị trí được chọn
            if (!hasSelectedLocation) {
                selectedLatitude = point.latitude();
                selectedLongitude = point.longitude();
                hasSelectedLocation = true;

                // Trả về vị trí ban đầu ngay lập tức
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLatitude);
                resultIntent.putExtra("longitude", selectedLongitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        getGestures(mapView).addOnMoveListener(onMoveListener);
    }

    private void setupMapInteractions() {
        floatingActionButton.setOnClickListener(v -> {
            enableLocationComponent();
            floatingActionButton.hide();
        });

        getGestures(mapView).addOnMapClickListener(point -> {
            double latitude = point.latitude();
            double longitude = point.longitude();
            String message = String.format("Vĩ độ: %.5f, Kinh độ: %.5f", latitude, longitude);
            Toast.makeText(MapActivity.this, message, Toast.LENGTH_LONG).show();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(17.0).build());

            // Trả tọa độ về MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", latitude);
            resultIntent.putExtra("longitude", longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
            return true;
        });
    }
}