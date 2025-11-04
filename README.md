# Androi_API_WhetherApp
ứng dụng androi về dự báo thời tiết kết hợp API gemini để học từ mới tiếng anh mỗi ngày và đưa ra lời khuyên , lời chúc dựa vào dữ liệu nhận được từ API đến người dùng <br>
**một số hình ảnh**<br>
ở đây là các chức năng của app là , nhập tên thành phố để xem thời tiết và nhập một nơi trên bản đồ để xem thời tiết và thời tiếp 5 ngày tiếp theo , mỗi ngày một từ mới tiếng anh , đưa ra lời khuyên dựa trên thời tiết và địa điểm 

<img width="1201" height="1204" alt="Ảnh chụp màn hình 2025-11-04 010023" src="https://github.com/user-attachments/assets/3a59fcc5-1746-40b4-b154-8274cfa49152" />
<img width="644" height="1324" alt="Ảnh chụp màn hình 2025-11-04 010223" src="https://github.com/user-attachments/assets/96da0fe6-6ca1-4135-8cdb-e885f7e4b563" />
<img width="656" height="1361" alt="Ảnh chụp màn hình 2025-11-04 010137" src="https://github.com/user-attachments/assets/0e4aebc3-8c4d-4377-b30c-cebb0c02f8d9" />

<img width="658" height="1339" alt="Ảnh chụp màn hình 2025-11-04 010525" src="https://github.com/user-attachments/assets/f6961e22-20db-42d2-b320-188354e2297b" />
<img width="676" height="1345" alt="Ảnh chụp màn hình 2025-11-04 010305" src="https://github.com/user-attachments/assets/c5f2a6f2-67cd-4346-a0ad-7153cd1b30ad" />
<img width="552" height="532" alt="Ảnh chụp màn hình 2025-11-04 010601" src="https://github.com/user-attachments/assets/1918a8f3-ec4f-4dc3-8102-1d59b2a30df8" />

app này sử dụng api miễn phí mapbox và gemini để chạy được app bạn phải lấy được 2 api key và thay thế vào trong đoạn code <br>
**1 thay api key gemini vào đoạn 2 đoạn code trong file MainActivity.java như hình bên dưới**

<img width="1554" height="139" alt="image" src="https://github.com/user-attachments/assets/341a9248-b4e4-4663-888b-25403c99578b" />

<img width="1560" height="122" alt="image" src="https://github.com/user-attachments/assets/5bada7f4-1808-4320-8e12-21a81839262e" />

**2thay api mapbox vào các đoạn dưới đây**<br>
đầu tiên trong file gradle.properties thay api key mapbox
<img width="1284" height="91" alt="image" src="https://github.com/user-attachments/assets/23694fe5-063d-4060-af98-ab21e39af5f1" />

tiếp theo thay api mapbox trong file settings.gradle.kts

<img width="1323" height="850" alt="image" src="https://github.com/user-attachments/assets/5bc44fe7-1e8f-41d6-92ff-fd31a2f6327f" />

vậy là thành công 




