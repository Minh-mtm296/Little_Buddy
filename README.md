# Little Buddy — Virtual Pet (Ứng Dụng Thú Cưng Ảo)

Ứng dụng Android nuôi và tương tác với thú cưng ảo chibi đáng yêu, được xây dựng hoàn toàn bằng **Kotlin**, **Jetpack Compose**, **Compose Canvas** và **Compose Animation APIs**.

---

## 1. Giới thiệu tổng quan

- **Tên dự án**: Little Buddy — Virtual Pet
- **Ý tưởng**: Mô phỏng việc chăm sóc chú mèo cưng trong căn phòng mùa thu ấm áp (_Cozy Autumn Room_). Toàn bộ nhân vật mèo, tủ đồ phụ kiện và bối cảnh căn phòng được vẽ **100% bằng Compose Canvas** (thuần vector đồ họa toán học, không sử dụng ảnh bitmap tĩnh toàn thân cho nhân vật).
- **Điểm nổi bật**:
  1. **Đồ họa Canvas Vector đa tầng**: Mèo có đầy đủ cử chỉ tự nhiên: nhịp thở phập phồng, chớp mắt, quay đầu theo hướng đi, chân bước so le, vươn vai duỗi người khi thức giấc, cuộn tròn khi ngủ say và híp mắt cười khi được vuốt ve.
  2. **Tối ưu Draw-Phase Animation (60 FPS)**: Đọc trạng thái hoạt ảnh trực tiếp ở pha Vẽ (Draw Phase) qua `rememberCatClock` và các lambda provider, bỏ qua hoàn toàn Recomposition và Layout Phase không cần thiết.
  3. **Hệ thống Tủ đồ & Cá nhân hóa (Wardrobe Studio)**: Đổi màu lông (Mèo Xiêm, Tam thể, Mướp gừng, Đen mun, Tuyết trắng) và phối phụ kiện đa dạng (mũ nơ, vòng cổ chuông, khăn quàng ấm, áo yếm). Hỗ trợ responsive layout cả xoay dọc lẫn xoay ngang (_Portrait & Landscape_).
  4. **State Machine & Gameplay Loop chặt chẽ**: Quản lý rành mạch các trạng thái hành động (`IDLE`, `WALKING`, `EATING`, `SLEEPING`, `WAKING_UP`, `STRETCHING`, `PLAYING`, `PETTING`).
  5. **Bền vững dữ liệu & Đồng bộ thời gian thực**: Lưu trữ qua `SharedPreferences`, tự động bù trừ chỉ số suy giảm khi app chạy ngầm (_Android Doze Mode_) thông qua vòng đời Activity.
  6. **Clean Code & Unit Testing**: Áp dụng nguyên tắc SOLID, phân tách rõ ràng Domain Model (`PetState`) và UI Controller (`PetViewModel`), bộ 27 Unit Test cases tự động đạt tỷ lệ pass 100%.

---

## 2. Bảng màu quy chuẩn (Design System)

Ứng dụng tuân thủ nghiêm ngặt hệ thống 6 gam màu pastel ấm áp:

| Màu sắc             | Mã Hex    | Vai trò trong giao diện                                           |
| :------------------ | :-------- | :---------------------------------------------------------------- |
| **Vanilla Cream**   | `#FFF7E6` | Màu nền chính toàn ứng dụng, tạo cảm giác êm dịu, ấm cúng         |
| **Blush Petal**     | `#F7C8D3` | Màu má hồng kawaii, nền thẻ thông tin và chi tiết trang trí       |
| **Rosewood**        | `#B46A72` | Màu nhấn chính cho nút hành động, viền nét và trạng thái active   |
| **Sage Leaf**       | `#A8B58A` | Đại diện cho chỉ số sức khỏe, độ vui vẻ và năng lượng tích cực    |
| **Misty Sky**       | `#A9B7C6` | Màu phụ cho thanh năng lượng và trạng thái giấc ngủ ban đêm       |
| **Midnight Lagoon** | `#2D3A47` | Màu chữ chính, tiêu đề, nét vẽ mắt mũi, đảm bảo độ tương phản cao |

---

## 3. Kiến trúc dự án (Architecture & Project Structure)

Dự án áp dụng kiến trúc MVVM kết hợp Clean Architecture tinh gọn:

```
app/src/
├── main/
│   ├── AndroidManifest.xml
│   ├── java/com/littlebuddy/pet/
│   │   ├── MainActivity.kt                  # Activity khởi chạy, Edge-to-Edge & Life-cycle hooks
│   │   ├── data/
│   │   │   ├── PetPreferences.kt            # Đọc/ghi SharedPreferences cục bộ
│   │   │   └── PetRepository.kt             # Cung cấp dữ liệu bất biến và StateFlow cho ViewModel
│   │   ├── pet/
│   │   │   ├── CatBody.kt                   # Canvas: Thân đứng chuông ú mập, tai, đuôi, chân trước/sau
│   │   │   ├── CatCalico.kt                 # Canvas: Đốm lông Calico hữu cơ (tam thể)
│   │   │   ├── CatColors.kt                 # Bảng màu sắc chi tiết cho lông mèo, tai, mắt và đệm
│   │   │   ├── CatEffects.kt                # Canvas: Trái tim bay bổng, ngôi sao lấp lánh khi vui vẻ
│   │   │   ├── CatFace.kt                   # Canvas: Mặt nạ màu sẫm (Seal Point), mắt anime, mũi miệng
│   │   │   ├── CatSleeping.kt               # Canvas: Mèo cuộn tròn ngủ, đệm ngủ, hiệu ứng Zzz
│   │   │   ├── CatWardrobeCanvas.kt         # Canvas: Phụ kiện mũ, vòng cổ, áo yếm (tư thế đứng & ngủ)
│   │   │   ├── PetAnimation.kt              # Đồng hồ CatClock, Pose interpolator (lerp, smoothstep)
│   │   │   ├── PetCanvas.kt                 # Composable Canvas kết nối toàn bộ các tầng vẽ của mèo
│   │   │   ├── PetMood.kt                   # Enum cảm xúc (HAPPY, NEUTRAL, HUNGRY, SLEEPY, PLAYFUL, EATING)
│   │   │   └── PetState.kt                  # Domain Model độc lập UI: quản lý chỉ số [0, 100], logic thăng cấp
│   │   ├── ui/
│   │   │   ├── components/
│   │   │   │   ├── ActionButtons.kt         # Hàng nút Feed, Pet, Play, Sleep với hiệu ứng chạm nảy
│   │   │   │   ├── CozyRoomBackground.kt    # Căn phòng mùa thu ấm cúng vẽ 100% bằng Compose Canvas
│   │   │   │   ├── FeedbackBubble.kt        # Bong bóng thoại Comic & đám mây giấc mơ bồng bềnh
│   │   │   │   ├── PetProfileDialog.kt      # Hộp thoại xem hồ sơ và đổi tên thú cưng
│   │   │   │   └── StatBar.kt               # Thanh chỉ số Hunger, Happiness, Energy (bo góc mềm mại)
│   │   │   ├── screens/
│   │   │   │   ├── FoodBowlUi.kt            # Ánh xạ tài nguyên Drawable cho thức ăn trong phòng
│   │   │   │   ├── FoodPickerDialog.kt      # Dialog chọn món (Hạt khô, Pate, Cá khô)
│   │   │   │   ├── GameScreen.kt            # Mini-games: Đuổi bắt cuộn len & Săn cần câu lông vũ
│   │   │   │   ├── PetHomeScreen.kt         # Màn hình chính Little Buddy (tương tác phòng & mèo)
│   │   │   │   └── WardrobeScreen.kt        # Màn hình Tủ đồ chuyên biệt với bục thời trang Live Preview
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt                 # 6 mã màu bắt buộc và màu hỗ trợ
│   │   │   │   ├── Theme.kt                 # LittleBuddyTheme tập trung toàn app
│   │   │   │   └── Type.kt                  # Cấu hình Typography Material 3
│   │   │   └── viewmodel/
│   │   │       └── PetViewModel.kt          # Quản lý State Machine vật lý, coroutines animation và sự kiện UI
│   │   └── wardrobe/
│   │       └── WardrobeModels.kt            # Data classes phụ kiện, phân loại & WardrobeRegistry
│   └── res/
│       ├── drawable/                        # 4 hình ảnh 2D bát thức ăn (bat_do_an, bat_hat, bat_pate, bat_ca_kho)
│       └── values/                          # Strings, Colors, Themes XML
└── test/
    └── java/com/littlebuddy/pet/
        ├── PetStateTest.kt                  # 21 test cases kiểm tra domain model, EXP, thời gian suy giảm
        └── WardrobeTest.kt                  # 6 test cases kiểm tra logic trang bị và cấp độ mở khóa
```

---

## 4. Tối ưu hóa hiệu năng Compose Canvas (60 FPS)

Jetpack Compose hoạt động theo chu trình 3 pha liên tiếp:
$$\text{Composition} \longrightarrow \text{Layout} \longrightarrow \text{Draw}$$

- **Vấn đề**: Nếu đọc trực tiếp các giá trị `State<Float>` thay đổi liên tục trong pha Composition, toàn bộ giao diện màn hình sẽ bị **Recompose 60 lần/giây**, gây tiêu tốn CPU và hao pin.
- **Giải pháp áp dụng**:
  1. **Đọc State ở Pha Vẽ (Draw-Phase State Reading)**: Sử dụng `rememberCatClock` và truyền các giá trị hoạt ảnh thông qua lambda provider `() -> Float` trực tiếp vào khối `Canvas { ... }`.
  2. **Bỏ qua Recomposition & Layout**: Compose nhận biết trạng thái chỉ ảnh hưởng đến màu sắc/tọa độ vẽ, do đó **chỉ có khối Canvas được vẽ lại**, cây Composable xung quanh hoàn toàn không bị kích hoạt Recomposition.
  3. **Tạm dừng animation khi không cần thiết**: Các hiệu ứng bước đi (`walkCycle`), gầm gừ (`purrWave`), nhịp thở ngủ sâu chỉ khởi chạy khi mèo ở đúng trạng thái tương ứng.

---

## 5. Quy tắc chỉ số & Vòng lặp Gameplay

### 5.1. Bảng quy chuẩn tương tác

| Tương tác            | Độ no (Hunger)                        | Niềm vui (Happiness)          | Năng lượng (Energy)     | Kinh nghiệm (EXP) | Thân thiết (Intimacy)            |
| :------------------- | :------------------------------------ | :---------------------------- | :---------------------- | :---------------- | :------------------------------- |
| **Cho ăn: Hạt khô**  | **+25%**                              | **+5%**                       | Giữ nguyên              | **+15 EXP**       | **+5 điểm**                      |
| **Cho ăn: Pate**     | **+35%**                              | **+12%**                      | Giữ nguyên              | **+15 EXP**       | **+5 điểm**                      |
| **Cho ăn: Cá khô**   | **+30%**                              | **+20%**                      | Giữ nguyên              | **+15 EXP**       | **+5 điểm**                      |
| **Vuốt ve (Pet)**    | Giữ nguyên                            | **+15%** _(hoặc +5% khi ngủ)_ | Giữ nguyên              | **+10 EXP**       | **+10 điểm** _(hoặc +2 khi ngủ)_ |
| **Mini-game (Play)** | **-12%**                              | **+25%**                      | **-18%**                | **+15 EXP**       | **+15 điểm**                     |
| **Uống nước**        | Giữ nguyên                            | **+6%**                       | **+10%**                | Giữ nguyên        | Giữ nguyên                       |
| **Vờn len sàn**      | Giữ nguyên                            | **+10%**                      | **-5%**                 | Giữ nguyên        | Giữ nguyên                       |
| **Cào móng**         | Giữ nguyên                            | **+8%**                       | Giữ nguyên              | Giữ nguyên        | Giữ nguyên                       |
| **Đi ngủ (Sleep)**   | Tiêu hao **-0.05f/phút** _(giảm 75%)_ | Giảm **-0.15f/phút**          | Hồi phục **+4.0f/phút** | Giữ nguyên        | Giữ nguyên                       |

### 5.2. Các quy tắc quan trọng

- **Giới hạn an toàn**: Mọi chỉ số luôn được giới hạn chặt chẽ trong khoảng `[0.0f, 100.0f]`. Điểm thân thiết tối đa là 200.
- **Lên cấp lũy tiến (`gainExp`)**: Mỗi cấp độ yêu cầu 100 EXP. Khi nhận lượng lớn EXP cùng lúc, hàm xử lý vòng lặp thăng nhiều cấp chuẩn xác mà không bị tràn số.
- **Trao đổi chất khi ngủ**: Tiêu hao đói giảm 75% so với lúc thức (`SLEEP_HUNGER_DECAY_PER_MINUTE = 0.05f`), giúp mèo không bị kiệt sức hoàn toàn sau giấc ngủ qua đêm.
- **Đồng bộ thời gian ngầm (Background Doze Sync)**: Khi Activity quay trở lại (`onResume`), hệ thống tính toán thời gian người dùng vắng mặt và áp dụng mức suy giảm chỉ số tương ứng thông qua `applyTimeElapsed`.
- **Chống spam tương tác**: Vuốt ve liên tục trong cùng chu kỳ chỉ gia hạn hiệu ứng rung rinh mà không cộng dồn chỉ số; uống nước và vờn len có thời gian hồi 8 giây.

---

## 6. Hệ thống Tủ đồ & Cá nhân hóa (Wardrobe Studio)

Màn hình Tủ đồ (`WardrobeScreen`) sở hữu bục thời trang xoay quanh nhân vật với chế độ thử đồ trực tiếp (_Live Preview_):

1. **Bộ lông (Fur Skin)**:
   - _Mèo Xiêm (Siamese - Mặc định)_: Bộ lông màu kem, mặt nạ, tai và 4 chân màu taupe sẫm đặc trưng.
   - _Mèo Tam thể (Calico)_: Bộ lông 3 màu trắng - cam nâu - xám đan xen tự nhiên.
   - _Mèo Mướp gừng (Ginger Tabby)_: Bộ lông cam ấm áp cùng các vệt sọc tinh nghịch.
   - _Mèo Đen mun (Midnight Black)_: Lông đen tuyền cùng đôi mắt hổ phách huyền bí.
   - _Mèo Tuyết trắng (Pure White)_: Bộ lông trắng muốt như bông tuyết.
2. **Mũ & Nơ (Headwear)**: Mũ nồi họa sĩ Beret, Nơ hoa cúc vàng, Mũ cói mùa hè.
3. **Vòng cổ & Khăn quàng (Neckwear)**: Vòng cổ chuông vàng may mắn, Khăn quàng len đỏ ấm áp, Nơ bướm xanh.
4. **Trang phục thân (Bodywear)**: Quần yếm denim vintage, Áo len dệt kim kẻ sọc mùa đông.
5. **Cơ chế cấp độ mở khóa**: Mỗi vật phẩm đều yêu cầu cấp độ tương ứng của thú cưng để mở khóa, tạo động lực chăm sóc và tương tác hàng ngày.

---

## 7. Kiểm thử tự động (Unit Tests)

Dự án sở hữu bộ **27 Unit Test cases** tự động bao phủ toàn diện các ràng buộc logic:

- `PetStateTest` (21 test cases):
  - Ràng buộc tăng/giảm chỉ số khi Cho ăn, Vuốt ve, Chơi mini-game.
  - Xử lý thăng cấp lũy tiến khi nhận lượng lớn EXP cùng lúc.
  - Quy tắc suy giảm chỉ số theo thời gian và hồi phục năng lượng khi ngủ.
  - Chuẩn hóa tên thú cưng và giới hạn trần điểm thân thiết (200 điểm).
- `WardrobeTest` (6 test cases):
  - Khởi tạo trang phục mặc định (Mèo Xiêm, không phụ kiện).
  - Thử đồ, tháo đồ theo danh mục và khôi phục mặc định trong Live Preview.
  - Kiểm tra điều kiện cấp độ yêu cầu khi mở khóa trang phục.

---

## 8. Hướng dẫn cài đặt & Chạy ứng dụng

### Yêu cầu môi trường

- **Android Studio**: Bản Ladybug / Hedgehog / Meerkat hoặc mới hơn.
- **JDK**: Java 17 trở lên.
- **Thiết bị chạy**: Android 7.0 (API 24) trở lên, khuyến nghị Android 12+.

### Các bước thực hiện

1. Clone mã nguồn về máy:
   ```bash
   git clone https://github.com/Minh-mtm296/Little_Buddy.git
   ```
2. Mở Android Studio, chọn **Open** và trỏ đến thư mục `Little_Buddy`.
3. Đợi Android Studio đồng bộ Gradle (**Sync Project with Gradle Files**).
4. Chạy toàn bộ Unit Tests kiểm tra:
   ```powershell
   ./gradlew testDebugUnitTest
   ```
5. Chọn thiết bị ảo (Emulator) hoặc cắm điện thoại Android (bật USB Debugging), nhấn nút **Run 'app'** (`Shift + F10`) để trải nghiệm!

---

## 9. Phân công nhiệm vụ nhóm

| Thành viên   | Vai trò              | Trách nhiệm chính                                                                                                                                    |
| :----------- | :------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Thu Minh** | Kiến trúc & Tích hợp | Điều phối kiến trúc Clean Code / MVVM, `MainActivity`, `PetViewModel`, `PetState`, đồng bộ thời gian thực, Unit Tests, Git.                          |
| **Ngọc Anh** | UI/UX & Components   | Design System 6 màu, Typography Material 3, `StatBar`, `ActionButtons`, `FeedbackBubble`, `PetProfileDialog`, `FoodPickerDialog`.                    |
| **Kiều Vy**  | Canvas & Wardrobe    | Toàn bộ hình vẽ Canvas nhân vật mèo (`CatBody`, `CatFace`, `CatSleeping`, `CatCalico`, `CatWardrobeCanvas`), `WardrobeScreen`, `CozyRoomBackground`. |

---

## 10. Bản quyền tài nguyên (Asset Attribution)

- **Đồ họa nhân vật & Phòng**: 100% được thiết kế và vẽ bằng **Compose Canvas Code** (thuần vector toán học, không sử dụng ảnh minh họa tải sẵn cho nhân vật).
- **Hình ảnh bát thức ăn**: 4 ảnh minh họa 2D phong cách chibi pastel (`bat_do_an.png`, `bat_hat.png`, `bat_pate.png`, `bat_ca_kho.png`) được tối ưu hóa độ phân giải gọn nhẹ (~60–120 KB) phục vụ mục đích học tập phi thương mại.
- **Biểu tượng**: Bộ biểu tượng Google Material Symbols Rounded mã nguồn mở.
