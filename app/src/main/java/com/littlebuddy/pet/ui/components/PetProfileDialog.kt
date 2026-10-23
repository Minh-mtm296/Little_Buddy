package com.littlebuddy.pet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.pet.PetType
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SoftWhite
import com.littlebuddy.pet.ui.theme.VanillaCream
import com.littlebuddy.pet.wardrobe.WardrobeRegistry
import kotlinx.coroutines.delay

/**
 * Hộp thoại tùy chỉnh: Đặt tên / Đổi tên thú cưng & Xem hồ sơ bé mèo
 */
@Composable
fun PetProfileDialog(
    petState: PetState,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    onSelectType: ((PetType) -> Unit)? = null,
    onResetPet: () -> Unit
) {
    var nameInput by remember { mutableStateOf(petState.petName) }
    var showResetConfirm by remember { mutableStateOf(false) }
    val sanitized = PetState.sanitizeName(nameInput)
    val isNameValid = sanitized != null
    val focusRequester = remember { FocusRequester() }

    fun save() {
        sanitized?.let { validName ->
            onSaveName(validName)
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        runCatching { focusRequester.requestFocus() }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = {
                Text(
                    text = "Khôi phục dữ liệu ban đầu?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MidnightLagoon
                )
            },
            text = {
                Text(
                    text = "Bé mèo sẽ được đặt lại tên, cấp độ và các chỉ số về mặc định ban đầu. Bạn có chắc chắn muốn khôi phục không?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MidnightLagoon
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetPet()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rosewood),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Khôi phục", color = SoftWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Hủy", color = MidnightLagoon)
                }
            },
            containerColor = VanillaCream,
            shape = RoundedCornerShape(20.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "✏️", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Đổi tên & Hồ sơ bé mèo",
                    style = MaterialTheme.typography.titleLarge,
                    color = MidnightLagoon
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        if (it.length <= PetState.MAX_NAME_LENGTH) {
                            nameInput = it
                        }
                    },
                    label = { Text("Đặt tên cho bé mèo của bạn") },
                    placeholder = { Text("Nhập tên mới (VD: Bông Gòn, Mimi...)") },
                    trailingIcon = {
                        if (nameInput.isNotEmpty()) {
                            IconButton(onClick = { nameInput = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Xóa tên",
                                    tint = Rosewood.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    supportingText = {
                        if (!isNameValid) {
                            Text(
                                text = "Tên không được để trống nha!",
                                color = Rosewood
                            )
                        } else {
                            Text(
                                text = "${nameInput.trim().length}/${PetState.MAX_NAME_LENGTH} ký tự (Tối đa ${PetState.MAX_NAME_LENGTH} ký tự)",
                                color = MidnightLagoon.copy(alpha = 0.6f)
                            )
                        }
                    },
                    isError = !isNameValid,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { save() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(14.dp)
                )

                Text(
                    text = "Nhân vật thú cưng:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MidnightLagoon
                )

                val activeFurSkin = WardrobeRegistry.getFurSkin(petState.wardrobe.furSkinId)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlushPetal.copy(alpha = 0.35f))
                        .border(1.2.dp, Rosewood.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "🐱", style = MaterialTheme.typography.titleLarge)
                    Column {
                        Text(
                            text = "${activeFurSkin.name} (${petState.petType.displayName})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MidnightLagoon
                        )
                        Text(
                            text = "Có thể đổi màu lông & trang phục trong Tủ đồ 👗",
                            style = MaterialTheme.typography.labelSmall,
                            color = MidnightLagoon.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Khôi phục trạng thái ban đầu",
                        color = Rosewood,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { save() },
                enabled = isNameValid,
                colors = ButtonDefaults.buttonColors(containerColor = Rosewood),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Lưu lại", color = SoftWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = MidnightLagoon)
            }
        },
        containerColor = VanillaCream,
        shape = RoundedCornerShape(24.dp)
    )
}
