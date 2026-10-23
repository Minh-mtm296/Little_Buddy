package com.littlebuddy.pet.pet

/**
 * Các trạng thái cảm xúc (Mood) của thú cưng
 */
enum class PetMood(val label: String, val description: String) {
    HAPPY("Vui vẻ", "Bé đang cảm thấy rất hạnh phúc và yêu đời!"),
    NEUTRAL("Bình yên", "Bé đang ngồi ngoan ngoãn ngắm nhìn bạn."),
    HUNGRY("Đói bụng", "Bụng bé đang sôi cồn cào, hãy cho bé ăn nhé!"),
    SLEEPY("Buồn ngủ", "Mắt bé lim dim rồi, bé muốn chợp mắt một lát..."),
    PLAYFUL("Hào hứng", "Bé đang muốn nhảy nhót và chơi đùa cùng bạn!"),
    EATING("Ngoàm ngoàm", "Món ăn ngon tuyệt cú mèo!")
}

/**
 * Loại thú cưng mà người dùng có thể chọn
 */
enum class PetType(val displayName: String) {
    CAT("Mèo Xiêm")
}
