# Little Buddy — Virtual Pet (Ứng Dụng Thú Cưng Ảo)

Ứng dụng Android chăm sóc thú cưng ảo chibi/kawaii được xây dựng hoàn toàn bằng **Kotlin**, **Jetpack Compose**, **Compose Canvas** và **Compose Animation APIs**.

---

## 1. Giới thiệu tổng quan

- **Tên dự án**: Little Buddy — Virtual Pet
- **Ý tưởng**: Mô phỏng việc nuôi và chăm sóc thú cưng ảo chibi đáng yêu — chú mèo Xiêm (Siamese Cat / Seal Point) trong căn phòng mùa thu ấm áp (Cozy Autumn Room). Nhân vật thú cưng, tủ đồ phụ kiện và toàn bộ bối cảnh căn phòng được vẽ 100% bằng Compose Canvas, có các cử chỉ, nhịp thở, chớp mắt, biểu cảm thay đổi linh hoạt theo hành động của người dùng (cho ăn món ngon, vuốt ve âu yếm, cào móng, chơi cuộn len, cần câu lông vũ, đi ngủ cuộn tròn).
- **Mục tiêu**:
  1. Giao diện chuẩn Mobile phong cách Kawaii/Chibi ấm áp, đồng nhất theo bảng màu quy chuẩn pastel.
  2. Ứng dụng kỹ thuật **Compose Canvas** chuyên sâu để vẽ nhân vật thú cưng, trang phục đa tầng và bối cảnh phòng responsive, không phụ thuộc ảnh bitmap tĩnh cho nhân vật.
  3. Tích hợp hệ thống **Compose Animation APIs** (InfiniteTransition, Animatable, animateFloatAsState, LaunchedEffect) tối ưu theo tầng Draw (Draw-Phase State Reading) đạt chuẩn 60 FPS mượt mà.
  4. Hệ thống Tủ đồ thời trang (Wardrobe) đa dạng: Tùy biến màu lông (Xiêm, Tam thể, Mèo mướp gừng, Mèo đen mun, Tuyết trắng), mũ nơ, vòng cổ khăn quàng, và trang phục thân người.
  5. Quản lý trạng thái logic rõ ràng (StateFlow, ViewModel, MVVM, Clean Architecture tách biệt Domain và UI Resources).
  6. Lưu trữ trạng thái cục bộ bền vững (SharedPreferences Persistence bảo toàn tên, cấp độ, trang phục đang mặc khi khởi động lại).

---

## 2. Bảng màu quy chuẩn (Design System)

Ứng dụng tuân thủ nghiêm ngặt hệ thống 6 màu chủ đạo:
- **Vanilla Cream (`#FFF7E6`)**: Màu nền chính toàn ứng dụng, tạo cảm giác êm dịu, ấm áp.
- **Blush Petal (`#F7C8D3`)**: Màu phụ cho thẻ, má hồng kawaii, và chi tiết trang trí.
- **Rosewood (`#B46A72`)**: Màu nhấn chính cho các nút hành động, viền nét và trạng thái active.
- **Sage Leaf (`#A8B58A`)**: Màu đại diện cho sức khỏe, năng lượng tích cực và mức độ hạnh phúc.
- **Misty Sky (`#A9B7C6`)**: Màu phụ cho thanh năng lượng và chế độ giấc ngủ.
- **Midnight Lagoon (`#2D3A47`)**: Màu chữ chính, tiêu đề, nét vẽ mắt mũi đảm bảo độ tương phản cao, dễ đọc.

---

## 3. Kiến trúc dự án (Clean Architecture & Modular Codebase)

Dự án áp dụng mô hình kiến trúc sạch, phân tách rõ ràng giữa UI, Canvas, Animation, Wardrobe, Business Logic và Data:

```
app/src/main/java/com/littlebuddy/pet/
├── MainActivity.kt                      # Điểm khởi chạy Activity, Edge-to-Edge & LittleBuddyTheme
├── ui/
│   ├── theme/
│   │   ├── Color.kt                     # 6 mã màu bắt buộc và bảng màu hỗ trợ
│   │   ├── Type.kt                      # Cấu hình Typography Material 3
│   │   └── Theme.kt                     # LittleBuddyTheme tập trung toàn app
│   ├── components/
│   │   ├── StatBar.kt                   # Thanh chỉ số Hunger, Happiness, Energy (animateFloatAsState)
│   │   ├── ActionButtons.kt             # Hàng nút Feed, Pet, Play, Sleep với hiệu ứng chạm nảy
│   │   ├── FeedbackBubble.kt            # Bong bóng thoại Comic Canvas (đuôi nhọn trỏ đầu mèo & đám mây giấc mơ)
│   │   ├── PetProfileDialog.kt          # Hộp thoại xem thông tin và đổi tên thú cưng
│   │   └── CozyRoomBackground.kt        # Căn phòng mùa thu ấm cúng vẽ 100% bằng Compose Canvas
│   └── screens/
│       ├── PetHomeScreen.kt             # Màn hình chính Little Buddy (tương tác phòng & mèo)
│       ├── WardrobeScreen.kt            # Màn hình Tủ đồ chuyên biệt với sân khấu preview trực tiếp
│       ├── FoodPickerDialog.kt          # Dialog chọn thức ăn cho mèo (Hạt, Pate, Cá khô)
│       ├── FoodBowlUi.kt                # Ánh xạ tài nguyên Drawable cho BowlFood (UI Layer)
│       ├── GameScreen.kt                # Mini-games: Bảng chọn trò chơi, Đuổi bắt cuộn len & Săn lông vũ (Pounce Jump Canvas & Victory Card)
│       └── WardrobeScreen.kt            # Màn hình Tủ đồ chuyên biệt với sân khấu preview trực tiếp
├── pet/
│   ├── CatColors.kt                     # Bảng màu sắc chi tiết cho lông mèo, tai, mắt và đệm ngủ
│   ├── CatBody.kt                       # Vẽ Canvas: Thân đứng, tai tam giác, đuôi vẫy, chân trước/sau
│   ├── CatFace.kt                       # Vẽ Canvas: Mặt nạ màu sẫm (Seal point), mắt anime, mũi miệng
│   ├── CatWardrobeCanvas.kt             # Vẽ Canvas: Phụ kiện mũ, vòng cổ, áo yếm (cho cả đứng và ngủ)
│   ├── CatSleeping.kt                   # Vẽ Canvas: Mèo cuộn tròn ngủ, đệm nhung chần bông, Zzz bay
│   ├── CatEffects.kt                    # Vẽ Canvas: Trái tim bay bổng, ngôi sao lấp lánh, hạt thức ăn
│   ├── PetCanvas.kt                     # Composable chính ghép nối các tầng vẽ, tối ưu Draw Phase
│   ├── PetAnimation.kt                  # Procedural Animation Controller (lambda provider () -> Float)
│   ├── PetMood.kt                       # Enum cảm xúc (HAPPY, NEUTRAL, HUNGRY, SLEEPY, PLAYFUL, EATING) & PetType.CAT (Mèo Xiêm)
│   └── PetState.kt                      # Domain State độc lập UI, giới hạn chỉ số [0, 100], logic nghiệp vụ
├── wardrobe/
│   └── WardrobeModels.kt                # Data classes phụ kiện, danh mục, bảng màu & WardrobeRegistry kho trang bị
└── data/
    ├── PetPreferences.kt                # Đọc/ghi SharedPreferences cục bộ (tên, stats, wardrobe trang bị)
    └── PetRepository.kt                 # Cung cấp dữ liệu bất biến và StateFlow cho ViewModel
```

---

## 4. Tối ưu hóa hiệu năng Compose Canvas & Animation (Trọng tâm kỹ thuật)

Một điểm nhấn kỹ thuật quan trọng trong dự án là **Tối ưu hóa vòng đời Jetpack Compose**:

### 4.1. Vấn đề Recomposition trong Animation
Jetpack Compose hoạt động theo 3 pha liên tiếp:
$$\text{Composition} \longrightarrow \text{Layout} \longrightarrow \text{Draw}$$
Nếu truyền các giá trị `State<Float>` trực tiếp vào tham số của Composable cha hoặc đọc `State.value` trong quá trình Compose, toàn bộ cây giao diện (`PetHomeScreen`, phòng Cozy Room, hàng nút điều khiển, thanh chỉ số) sẽ bị **Recompose liên tục mỗi 16ms (60 FPS)** ngay cả khi chỉ có mỗi chiếc đuôi mèo đang vẫy. Điều này làm tăng mức tiêu thụ CPU và gây hao pin thiết bị.

### 4.2. Giải pháp: Đọc State ở Pha Vẽ (Draw-Phase State Reading)
1. **Truyền Lambda Provider `() -> Float`**: Thay vì truyền trực tiếp giá trị `Float` thô, `PetAnimationState` đóng gói các kênh hoạt ảnh dưới dạng các hàm cung cấp lambda `val bounceOffsetY: () -> Float`, `val tailWagAngle: () -> Float`, v.v.
2. **Khai thác `DrawScope` trong `Canvas { }`**: Bên trong `PetCanvas.kt`, các lambda `animState.tailWagAngle()` chỉ được thực thi bên trong phạm vi khối vẽ `Canvas(modifier) { ... }`.
3. **Bỏ qua Recomposition & Layout**: Compose nhận diện được việc đọc State diễn ra ở pha **Draw**, do đó **chỉ có duy nhất khối Canvas được vẽ lại**, hoàn toàn bỏ qua bước Recomposition của `PetHomeScreen` và Layout Phase.
4. **Vòng lặp có điều kiện (Conditional Transitions)**: Các animation rung rơn (`purrWave`), bước đi (`walkCycle`), nhịp thở ngủ sâu (`sleepingBreathScale`) chỉ khởi chạy khi nhân vật ở đúng trạng thái tương ứng, giúp giảm thiểu tối đa tài nguyên nền.

---

## 5. Chi tiết kỹ thuật Canvas (`pet/`)

Toàn bộ chú mèo và căn phòng được kiến tạo 100% bằng mã nguồn đồ họa toán học trên tọa độ tương đối (`size.width`, `size.height`):
- `drawOval`: Đệm nằm nhung tròn mềm, bóng đổ êm ái trên thảm len, thân mèo tư thế đứng, đệm chân hồng xinh xắn.
- `drawCircle`: Đầu mèo bầu bĩnh tròn trịa, mắt anime to tròn với 2 đốm sáng phản chiếu long lanh, cúc chần bông trên đệm ngủ, hạt thức ăn.
- `drawPath`: Mặt nạ màu sẫm đặc trưng của mèo Xiêm, tai nhọn bo tròn, thân mèo cuộn tròn khi ngủ (`CatSleeping`), đuôi mèo uốn lượn hình chữ S, mũ nồi họa sĩ Pháp, nơ cài hoa, khăn quàng đỏ mùa đông, quần yếm denim vintage, trái tim và ngôi sao 4 cánh lấp lánh.
- `drawArc`: Mắt cười tít `^ ^` khi hạnh phúc, mắt nhắm cong êm đềm khi ngủ, khóe miệng cười chữ `w` mèo con đặc trưng.
- `drawLine`: Ria mép mèo 2 bên má thanh mảnh, các đường viền may trang phục.
- **Bong bóng thoại Comic & Đám mây giấc mơ (`FeedbackBubble`)**:
  - `drawSpeechBubble`: Tạo hình bằng `Path` bo tròn góc kèm chiếc đuôi nhọn trỏ chính xác về miệng/đầu bé mèo, bóng đổ hoạt hình 2D ấm áp.
  - `drawDreamCloudBubble`: Đường viền đám mây gợn sóng mềm mại bằng Bézier curves kèm 3 hạt suy nghĩ tròn nhỏ `○` `o` dẫn xuống đầu mèo khi ngủ.
  - Tự động trôi theo vị trí X thực tế của chú mèo (`BiasAlignment`) và lơ lửng dập dềnh (`bubbleFloat` hover animation).
- **Tọa độ vùng chạm tương đối**: Cột cào móng (`postX = 0.88w, postY = 0.58h`) và chậu cây (`plantX = 0.05w, plantY = 0.74h`) được định vị thông qua `BoxWithConstraints`, tự động khớp chính xác vị trí hình vẽ trên mọi kích thước màn hình điện thoại và máy tính bảng.

---

## 6. Quy tắc chỉ số & Logic chăm sóc (`PetState.kt`)

| Chỉ số | Quy ước chuẩn | Cho ăn (Feed) | Vuốt ve (Pet) | Chơi đùa (Play) | Nghỉ ngơi (Sleep) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Độ no bụng (Hunger)** | 100: No nê<br>0: Đói bụng | **+30%** (No thêm) | Giữ nguyên | **-15%** (Tiêu hao) | Giữ nguyên |
| **Mức độ vui vẻ (Happiness)** | 0: Buồn bã<br>100: Cực vui | **+10%** | **+15%** (hoặc +5% khi ngủ) | **+25%** | Giữ nguyên |
| **Năng lượng (Energy)** | 0: Kiệt sức<br>100: Tràn đầy | Giữ nguyên | Giữ nguyên | **-20%** | **+10%** mỗi 3 giây |
| **Điểm thân thiết (Intimacy)** | Tích lũy (0..200) | **+5 điểm** | **+10 điểm** (hoặc +2 khi ngủ) | **+15 điểm** | Giữ nguyên |
| **Kinh nghiệm (EXP)** | Cần 100 EXP / cấp | **+15 EXP** | **+10 EXP** | **+25 EXP** | Giữ nguyên |

- **Cấp độ thân thiết (Intimacy Level)**: Cứ mỗi 50 điểm thân thiết, Little Buddy sẽ thăng 1 cấp (Cấp 1: Bạn mới quen $\rightarrow$ Cấp 2: Bạn thân thiết $\rightarrow$ Cấp 3: Tri kỷ ngọt ngào $\rightarrow$ Cấp 4+: Gia đình ấm áp).
- Mọi chỉ số được khóa chặt trong phạm vi an toàn `[0.0f, 100.0f]` bằng `.coerceIn(0f, 100f)`.
- Khi năng lượng dưới 15% hoặc độ đói dưới 15%, chú mèo từ chối chơi đùa và phát thông điệp nhắc nhở người dùng cho ăn hoặc nghỉ ngơi.
- Khi đang ngủ, mèo cuộn tròn êm ái trên đệm; nếu người chơi vuốt ve, mèo khẽ cựa quậy trong mơ và nhận thêm điểm thân thiết.

---

## 7. Hệ thống Tủ đồ & Cá nhân hóa (Wardrobe System)

Màn hình Tủ đồ (`WardrobeScreen`) được thiết kế trực quan với sân khấu xem trước thời gian thực (Live Preview Stage):
1. **Màu lông (Fur Skin)**:
   - *Mèo Xiêm nguyên bản (Seal Point Siamese)*: Mặt nạ sẫm, thân màu kem thanh lịch.
   - *Mèo Tam thể (Calico)*: Bộ lông 3 màu tài lộc, đốm cam nâu sinh động.
   - *Mèo Mướp gừng (Ginger Tabby)*: Màu vàng cam ấm áp với sọc mướp dễ thương.
   - *Mèo Đen mun (Midnight Black)*: Lông đen huyền bí, mắt vàng hổ phách sáng ngời.
   - *Mèo Tuyết trắng (Pure White)*: Bộ lông trắng muốt như bông tuyết.
2. **Mũ & Phụ kiện đầu (Headwear)**: Mũ nồi họa sĩ Beret, Nơ hoa cúc vàng rực rỡ.
3. **Vòng cổ & Khăn quàng (Neckwear)**: Khăn quàng len đỏ ấm áp, Vòng cổ chuông vàng may mắn.
4. **Trang phục thân (Bodywear)**: Quần yếm denim phong cách cổ điển, Áo len dệt kim kẻ sọc mùa đông.
- **Cơ chế trang bị**: Cho phép thử đồ tự do, xem trước trực tiếp trên nhân vật, tháo phụ kiện hoặc khôi phục mặc định. Khi bấm Xác nhận, bộ trang phục được lưu vào `SharedPreferences` và đồng bộ ra ngoài phòng chính. Khi mèo đi ngủ, trang phục thích nghi mượt mà sang tư thế cuộn tròn.

---

## 8. Bản quyền & Nguồn gốc tài nguyên (Asset Attribution / Credits)

- **Đồ họa nhân vật & Bối cảnh phòng**: 100% được thiết kế và lập trình bằng **Compose Canvas Code** (thuần vector đồ họa, không sử dụng hình ảnh tải sẵn cho nhân vật và phòng).
- **Hình ảnh bát thức ăn**:
  - `bat_do_an.png`, `bat_hat.png`, `bat_pate.png`, `bat_ca_kho.png`
  - Hình minh họa 2D phong cách chibi pastel, được tối ưu hóa độ phân giải gọn nhẹ (360x230 px, ~60–120 KB) phục vụ mục đích học tập phi thương mại.
- **Font & Icon hệ thống**: Bộ biểu tượng Google Material Symbols Rounded & Google Fonts mã nguồn mở.

---

## 9. Phân công nhiệm vụ nhóm 3 thành viên

| Thành viên | Vai trò | Phụ trách chính | Tiêu chí nghiệm thu (Checklist) |
| :--- | :--- | :--- | :--- |
| **Trưởng nhóm** (Minh) | Điều phối, Tích hợp & Kiến trúc | `MainActivity.kt`<br>`PetHomeScreen.kt`<br>`PetViewModel.kt`<br>`PetPreferences.kt`<br>`FoodBowlUi.kt` | - Tích hợp toàn bộ module, không lỗi biên dịch.<br>- Xử lý StateFlow, Coroutine vòng lặp ngủ và phục hồi năng lượng.<br>- Tách biệt kiến trúc Clean Architecture (Domain - UI Resources).<br>- Tối ưu hóa Draw-Phase Animation.<br>- Đảm bảo sản phẩm chạy ổn định trên thiết bị Android. |
| **Ngọc Anh** | UI/UX & Components | `ui/theme/*`<br>`ui/components/StatBar.kt`<br>`ui/components/ActionButtons.kt`<br>`ui/components/FeedbackBubble.kt`<br>`ui/components/PetProfileDialog.kt`<br>`ui/screens/FoodPickerDialog.kt` | - Đảm bảo chính xác 6 mã màu và hệ thống typography.<br>- Hiển thị thanh tiến trình êm mượt, bo góc đúng chuẩn Kawaii.<br>- Tích hợp trạm bát thức ăn & nước uống trong phòng.<br>- Hộp thoại thông tin bé mèo và đổi tên hoạt động mượt mà. |
| **Kiều Vy** | Canvas & Wardrobe | `pet/Cat*.kt`<br>`pet/PetCanvas.kt`<br>`pet/PetAnimation.kt`<br>`wardrobe/*`<br>`ui/screens/WardrobeScreen.kt`<br>`ui/components/CozyRoomBackground.kt` | - Hoàn thiện 100% hình vẽ Canvas cho Mèo Xiêm và các skin lông.<br>- Thiết kế hệ thống trang phục Tủ đồ (mũ, vòng cổ, yếm).<br>- Dựng tư thế mèo cuộn tròn ngủ trên đệm nhung chần bông.<br>- Vẽ bối cảnh căn phòng mùa thu ấm áp (Cozy Autumn Room).<br>- Tọa độ hoàn toàn tương đối và tối ưu hóa Draw phase. |

---

## 10. Kịch bản Video Demo (2 phút cuốn hút)

1. **0:00 - 0:20 (Mở đầu & Căn phòng Cozy Room)**: Khởi chạy app, giới thiệu Little Buddy với phong cách Kawaii pastel 6 màu chuẩn. Giới thiệu căn phòng mùa thu ấm cúng vẽ 100% bằng Canvas (cửa sổ vòm đón nắng, rèm ren, thảm oval, cây cảnh, cột cào móng).
2. **0:20 - 0:40 (Hoạt họa nhịp nhàng & Tối ưu Draw Phase)**: Trình diễn nhịp thở phập phồng, đuôi vẫy nhẹ nhàng, đôi mắt anime chớp tự nhiên. Giải thích ngắn gọn về việc đọc State trong pha Draw giúp giữ 60 FPS mượt mà.
3. **0:40 - 1:00 (Hành động Cho ăn & Trạm Bát Thức Ăn)**:
   - Chạm vào bát thức ăn trên sàn -> Mở bảng chọn món (Hạt dinh dưỡng, Pate cá hồi, Cá khô giòn).
   - Chọn món -> Mèo bước đến bên bát ăn, khuôn miệng nhai ngoàm ngoàm vui vẻ, thanh Độ no tăng lên.
4. **1:00 - 1:20 (Vuốt ve & Mini-game tương tác)**:
   - Chạm trực tiếp vào bé mèo hoặc bấm **Vuốt ve** -> Mèo nhảy tưng nảy lên, mắt cười híp mí `^ ^`, trái tim bay bổng.
   - Chạm vào cột cào móng hoặc cuộn len -> Kích hoạt mini-game tương tác sinh động.
5. **1:20 - 1:40 (Hệ thống Tủ đồ & Cá nhân hóa)**:
   - Nhấn icon **Tủ đồ** -> Mở màn hình Wardrobe với sân khấu preview trực tiếp.
   - Đổi màu lông (Mèo Mướp gừng, Tam thể, Mèo đen), đội Mũ nồi họa sĩ, đeo Yếm denim -> Nhấn Lưu -> Mèo ra ngoài phòng mang đúng bộ đồ mới.
6. **1:40 - 1:55 (Giấc ngủ cuộn tròn & Lưu trữ cục bộ)**:
   - Nhấn **Đi ngủ (Sleep)** -> Phòng phủ ánh đêm dịu nhẹ, mèo chuyển sang tư thế cuộn tròn ngủ say trên đệm chần bông, Zzz bay lơ lửng, năng lượng hồi phục dần.
   - Tắt ứng dụng hoàn toàn và mở lại -> Mọi dữ liệu về tên bé, cấp độ, trang phục đang mặc đều được khôi phục nguyên vẹn.
7. **1:55 - 2:00 (Kết luận)**: Tóm tắt điểm nổi bật về việc áp dụng thuần túy Compose Canvas, Animation và kiến trúc sạch.

---

## 11. Hướng dẫn mở và chạy dự án

1. Mở **Android Studio** (bản Ladybug / Hedgehog / Meerkat / Koala hoặc mới hơn).
2. Chọn **Open** và trỏ đến thư mục dự án `LITTLE BUDDY`.
3. Để Android Studio đồng bộ Gradle (**Sync Project with Gradle Files**).
4. Chọn thiết bị ảo (Emulator) hoặc cắm điện thoại Android (bật USB Debugging).
5. Nhấn nút **Run 'app'** (phím tắt `Shift + F10`) để cài đặt và trải nghiệm!
