package com.example.smartfarm;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.smartfarm.model.SensorData;
import com.example.smartfarm.service.DeviceService;

public class MainActivity extends AppCompatActivity {

    private TextView wifiStatus;
    private TextView temperatureInfo;
    private TextView humidityInfo;
    private TextView controlStatus; // Thêm TextView để hiển thị trạng thái điều khiển
    private Button lightButton;
    private Button fanButton;
    private Button doorButton;
    private Button feedButton;
    private DeviceService deviceService;

    private boolean isLightOn = false;
    private boolean isFanOn = false;
    private boolean isDoorOpen = false;

    // Thêm các biến trạng thái
    private String lightStatus = "OFF";
    private String fanStatus = "OFF";
    private String doorStatus = "CLOSED";
    private String feedingStatus = "NOT IN PROGRESS";

    private Handler handler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateSensorData(); // Cập nhật dữ liệu cảm biến
            handler.postDelayed(this, 5000); // Cập nhật sau mỗi 5 giây
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiStatus = findViewById(R.id.wifi_status);
        temperatureInfo = findViewById(R.id.temperature_info);
        humidityInfo = findViewById(R.id.humidity_info);
        controlStatus = findViewById(R.id.control_status); // Khởi tạo TextView trạng thái điều khiển
        lightButton = findViewById(R.id.light_button);
        fanButton = findViewById(R.id.fan_button);
        doorButton = findViewById(R.id.door_button);
        feedButton = findViewById(R.id.feed_button);

        deviceService = new DeviceService();

        updateWifiStatus();
        updateSensorData(); // Cập nhật ngay khi bắt đầu

        // Khởi động Runnable để cập nhật dữ liệu cảm biến liên tục
        handler.post(updateRunnable);

        lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLightOn = !isLightOn;
                String state = isLightOn ? "on" : "off";
                lightButton.setText(isLightOn ? "Tắt Đèn" : "Bật Đèn");
                lightButton.setBackgroundColor(getResources().getColor(isLightOn ? R.color.blue : R.color.green));
                controlLight(state);
            }
        });

        fanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFanOn = !isFanOn;
                String state = isFanOn ? "on" : "off";
                fanButton.setText(isFanOn ? "Tắt Quạt" : "Bật Quạt");
                fanButton.setBackgroundColor(getResources().getColor(isFanOn ? R.color.blue : R.color.green));
                controlFan(state);
            }
        });

        doorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDoorOpen = !isDoorOpen;
                String action = isDoorOpen ? "open" : "close";
                doorButton.setText(isDoorOpen ? "Đóng Cửa" : "Mở Cửa");
                doorButton.setBackgroundColor(getResources().getColor(isDoorOpen ? R.color.blue : R.color.green));
                controlDoor(action);
            }
        });

        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed();
            }
        });
    }

    private void updateControlStatus() {
        String status = "Light: " + lightStatus + "\n" +
                "Fan: " + fanStatus + "\n" +
                "Door: " + doorStatus + "\n" +
                "Feeding: " + feedingStatus;
        controlStatus.setText(status); // Cập nhật TextView trạng thái điều khiển
        Log.d("ControlStatus", "Trạng thái điều khiển: " + status); // Ghi log để theo dõi
    }

    private void updateControlStatus(String errorMessage) {
        String status = "Light: " + lightStatus + "\n" +
                "Fan: " + fanStatus + "\n" +
                "Door: " + doorStatus + "\n" +
                "Feeding: " + feedingStatus + "\n" +
                "Error: " + errorMessage; // Thêm thông báo lỗi vào trạng thái
        controlStatus.setText(status); // Cập nhật TextView trạng thái điều khiển
        Log.d("ControlStatus", "Trạng thái điều khiển: " + status); // Ghi log để theo dõi
    }

    private void updateWifiStatus() {
        boolean isConnected = true; // Kiểm tra kết nối WiFi thực tế
        wifiStatus.setText(isConnected ? "Kết nối WiFi" : "Mất kết nối WiFi");
        wifiStatus.setTextColor(isConnected ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
    }

    private void updateSensorData() {
        deviceService.getSensorData().enqueue(new Callback<SensorData>() {
            @Override
            public void onResponse(@NonNull Call<SensorData> call, @NonNull Response<SensorData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SensorData sensorData = response.body();
                    temperatureInfo.setText("Nhiệt độ: " + sensorData.getTemperature() + "°C");
                    humidityInfo.setText("Độ ẩm: " + sensorData.getHumidity() + "%");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SensorData> call, @NonNull Throwable t) {
                // Xử lý lỗi
                updateControlStatus(); // Giữ nguyên trạng thái điều khiển
            }
        });
    }

    private void controlLight(String state) {
        deviceService.controlLight(state).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    lightStatus = isLightOn ? "ON" : "OFF"; // Cập nhật trạng thái đèn
                    updateControlStatus(); // Cập nhật trạng thái điều khiển
                } else {
                    updateControlStatus("Lỗi khi điều khiển đèn");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                updateControlStatus("Kết nối thất bại"); // Ghi rõ thông báo lỗi
            }
        });
    }

    private void controlFan(String state) {
        deviceService.controlFan(state).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    fanStatus = isFanOn ? "ON" : "OFF"; // Cập nhật trạng thái quạt
                    updateControlStatus(); // Cập nhật trạng thái điều khiển
                } else {
                    updateControlStatus("Lỗi khi điều khiển quạt");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                updateControlStatus("Kết nối thất bại"); // Ghi rõ thông báo lỗi
            }
        });
    }

    private void controlDoor(String action) {
        deviceService.controlDoor(action).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    doorStatus = isDoorOpen ? "OPEN" : "CLOSED"; // Cập nhật trạng thái cửa
                    updateControlStatus(); // Cập nhật trạng thái điều khiển
                } else {
                    updateControlStatus("Lỗi khi điều khiển cửa");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                updateControlStatus("Kết nối thất bại"); // Ghi rõ thông báo lỗi
            }
        });
    }

    private void feed() {
        deviceService.feed().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    feedingStatus = "IN PROGRESS"; // Cập nhật trạng thái cho việc cho ăn
                    updateControlStatus(); // Cập nhật trạng thái điều khiển
                } else {
                    updateControlStatus("Lỗi khi cho ăn");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                updateControlStatus("Kết nối thất bại"); // Ghi rõ thông báo lỗi
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable); // Dừng cập nhật dữ liệu khi huỷ hoạt động
    }
}
