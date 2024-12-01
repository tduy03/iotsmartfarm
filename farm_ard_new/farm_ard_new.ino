#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <Servo.h>
#include <DHT.h>

// Định nghĩa các chân kết nối
#define DHTPIN D1                // Chân cảm biến DHT11
#define DHTTYPE DHT11            // Loại cảm biến DHT
#define SERVO_FEED_PIN D2        // Chân điều khiển motor servo ống thức ăn
#define SERVO_DOOR_PIN D3        // Chân điều khiển motor servo cửa
#define BUZZER_PIN D4            // Chân còi báo động
#define RELAY_FAN_PIN D5         // Chân điều khiển quạt
#define RELAY_LIGHT_PIN D6       // Chân điều khiển đèn

// Thông tin Wi-Fi cho mạng bên ngoài (chế độ STA)
const char* ssid = "FPT Tuan Duy";    // Thay bằng SSID của bạn
const char* password = "11032003";    // Thay bằng mật khẩu Wi-Fi của bạn

// Thông tin cho Access Point (chế độ AP)
const char* ap_ssid = "ESP8266_AP";   // SSID cho AP
const char* ap_password = "123456789"; // Mật khẩu cho AP

// Đối tượng HTTP Server
ESP8266WebServer server(80);

DHT dht(DHTPIN, DHTTYPE);
Servo servoFeed;   // Servo ống thức ăn
Servo servoDoor;   // Servo cửa

// Biến trạng thái
bool isFanOn = false;
bool isLightOn = false;

// Biến theo dõi điều khiển thủ công quạt
bool isManualFanControl = false; 
unsigned long lastManualFanControlTime = 0; // Thời gian của lần điều khiển thủ công cuối cùng
unsigned long manualTimeoutDuration = 60 * 1000; // 1 phút (đơn vị: mili giây)

// Hàm kết nối Wi-Fi
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  // Cấu hình chế độ Wi-Fi thành AP và STA
  WiFi.mode(WIFI_AP_STA);

  // Kết nối tới Wi-Fi bên ngoài (STA)
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print("IP address (STA): ");
  Serial.println(WiFi.localIP());

  // Thiết lập Access Point (AP)
  WiFi.softAP(ap_ssid, ap_password);
  Serial.println("Access Point started");
  Serial.print("IP address (AP): ");
  Serial.println(WiFi.softAPIP());
}

// Hàm bật còi báo động
void beep() {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(500); // Giữ còi trong 500ms
  digitalWrite(BUZZER_PIN, LOW);
}

// Hàm xử lý yêu cầu bật/tắt đèn
void handleLight() {
  String state = server.arg("state");
  if (state == "on") {
    digitalWrite(RELAY_LIGHT_PIN, HIGH);
    isLightOn = true;
    Serial.println("Light ON");
    beep(); // Bật còi
  } else if (state == "off") {
    digitalWrite(RELAY_LIGHT_PIN, LOW);
    isLightOn = false;
    Serial.println("Light OFF");
    beep(); // Bật còi
  }
  server.send(200, "text/plain", "Light state changed to " + state);
}

// Hàm xử lý yêu cầu bật/tắt quạt thủ công
void handleFan() {
  String state = server.arg("state");
  if (state == "on") {
    digitalWrite(RELAY_FAN_PIN, HIGH);
    isFanOn = true;
    isManualFanControl = true; // Chuyển sang điều khiển thủ công
    lastManualFanControlTime = millis(); // Cập nhật thời gian điều khiển thủ công
    Serial.println("Fan ON (Manual)");
    beep(); // Bật còi
  } else if (state == "off") {
    digitalWrite(RELAY_FAN_PIN, LOW);
    isFanOn = false;
    isManualFanControl = true; // Chuyển sang điều khiển thủ công
    lastManualFanControlTime = millis(); // Cập nhật thời gian điều khiển thủ công
    Serial.println("Fan OFF (Manual)");
    beep(); // Bật còi
  }
  server.send(200, "text/plain", "Fan state changed to " + state);
}

// Hàm xử lý yêu cầu cho servo ống thức ăn
void handleServoFeed() {
  servoFeed.write(90);
  beep();   // Quay servo 90 độ để thả thức ăn
  delay(2000);          // Đợi 2 giây
  servoFeed.write(0);   // Quay về vị trí ban đầu
  beep();               // Bật còi
  server.send(200, "text/plain", "Feeding Done");
  Serial.println("Feeding Done");
}

// Hàm xử lý yêu cầu mở/đóng cửa
void handleServoDoor() {
  String action = server.arg("action");
  if (action == "open") {
    servoDoor.write(100);  // Mở cửa
    beep();                // Bật còi
    server.send(200, "text/plain", "Door Opened");
    Serial.println("Door Opened");
  } else if (action == "close") {
    servoDoor.write(0);    // Đóng cửa
    beep();                // Bật còi
    server.send(200, "text/plain", "Door Closed");
    Serial.println("Door Closed");
  }
}

// Hàm xử lý yêu cầu lấy dữ liệu cảm biến
void handleGetSensorData() {
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();
  
  if (!isnan(temperature) && !isnan(humidity)) {
    String sensorData = "{";
    sensorData += "\"temperature\":" + String(temperature) + ",";
    sensorData += "\"humidity\":" + String(humidity);
    sensorData += "}";
    
    server.send(200, "application/json", sensorData);
  } else {
    server.send(500, "text/plain", "Failed to read from DHT sensor!");
  }
}

void setup() {
  Serial.begin(9600);       // Khởi tạo Serial Monitor
  
  // Khởi tạo các chân đầu ra
  pinMode(RELAY_LIGHT_PIN, OUTPUT);
  pinMode(RELAY_FAN_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  
  // Khởi tạo servo và cảm biến DHT
  servoFeed.attach(SERVO_FEED_PIN);
  servoDoor.attach(SERVO_DOOR_PIN);
  dht.begin();
  
  // Kết nối Wi-Fi
  setup_wifi();
  
  // Khởi động mDNS
  if (MDNS.begin("esp8266")) {
    Serial.println("mDNS responder started");
  } else {
    Serial.println("Error starting mDNS");
  }

  // Thiết lập các route cho HTTP server với phương thức POST
  server.on("/light", HTTP_POST, handleLight);
  server.on("/fan", HTTP_POST, handleFan);
  server.on("/servo/feed", HTTP_POST, handleServoFeed);
  server.on("/servo/door", HTTP_POST, handleServoDoor);
  server.on("/sensor", HTTP_GET, handleGetSensorData) ;
  
  // Bắt đầu HTTP server
  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient(); // Xử lý các yêu cầu từ client
  
  // Đọc nhiệt độ và độ ẩm từ cảm biến DHT
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();

  // Kiểm tra nếu đã hết thời gian chờ (không có thao tác thủ công mới)
  if (isManualFanControl && (millis() - lastManualFanControlTime > manualTimeoutDuration)) {
    isManualFanControl = false; // Quay lại chế độ tự động
    Serial.println("Timeout expired, switching back to automatic control.");
  }

  // Tự động điều khiển quạt dựa trên nhiệt độ nếu không có điều khiển thủ công
  if (!isManualFanControl && !isnan(temperature) && !isnan(humidity)) {
    if (temperature > 36.0 && !isFanOn) {
      digitalWrite(RELAY_FAN_PIN, HIGH);  // Bật quạt
      isFanOn = true;
      Serial.println("Fan turned ON due to high temperature.");
      beep(); // Bật còi
    } else if (temperature <= 34.0 && isFanOn) {
      digitalWrite(RELAY_FAN_PIN, LOW);   // Tắt quạt
      isFanOn = false;
      Serial.println("Fan turned OFF as temperature is normal.");
      beep(); // Bật còi
    }
  }
}
