package com.littlebuddy.pet.ui.screens

import androidx.annotation.DrawableRes
import com.littlebuddy.pet.R
import com.littlebuddy.pet.pet.BowlFood

/**
 * Ánh xạ tài nguyên Drawable cho BowlFood ở tầng UI.
 * Tách biệt hoàn toàn tầng Domain / State khỏi Android UI Resources (Clean Architecture).
 */
@get:DrawableRes
val BowlFood.drawableRes: Int
    get() = when (this) {
        BowlFood.EMPTY -> R.drawable.bat_do_an
        BowlFood.DRY_FOOD -> R.drawable.bat_hat
        BowlFood.PATE -> R.drawable.bat_pate
        BowlFood.FISH -> R.drawable.bat_ca_kho
    }
