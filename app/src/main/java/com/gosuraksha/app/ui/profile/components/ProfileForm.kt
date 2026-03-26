package com.gosuraksha.app.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gosuraksha.app.R

@Composable
fun ProfileFormSection(
    isDark: Boolean,
    name: String,
    phone: String,
    imageUri: String?,
    remoteImageUrl: String?,
    message: String?,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onEditPhoto: () -> Unit
) {
    ProfileSectionCard(header = "PROFILE INFORMATION", isDark = isDark) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val avatarModel = imageUri ?: remoteImageUrl
                val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
                    .joinToString("") { it.first().uppercase() }.ifEmpty { "M" }
                val context = LocalContext.current

                Box(
                    modifier = Modifier.size(54.dp).clip(CircleShape).background(if (isDark) PC.iconBgSlate(isDark) else Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarModel.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarModel)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(initials, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PC.Green)
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        name.ifBlank { "Your name" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PC.onSurf(isDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(phone.ifBlank { "Phone number" }, fontSize = 11.sp, color = PC.subText(isDark))
                }

                OutlinedButton(
                    onClick = onEditPhoto,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, if (isDark) PC.DarkBorder else PC.LightBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PC.subText(isDark))
                ) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit photo", fontSize = 10.sp)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))

            ProfileTextField(name, onNameChange, stringResource(R.string.profile_field_full_name), Icons.Outlined.Person, isDark)
            ProfileTextField(phone, onPhoneChange, stringResource(R.string.profile_field_phone), Icons.Outlined.Phone, isDark, enabled = false)

            // Save button — PC.Green (vibrant #22C55E) with elevation so it feels
            // clickable and premium. Previously used PC.GreenDeep (#101311 = near-black)
            // which made the button look invisible and flat.
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PC.Green,
                    contentColor   = Color(0xFF051209)    // near-black text on bright green
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation  = 3.dp,
                    pressedElevation  = 0.dp,
                    focusedElevation  = 3.dp,
                    hoveredElevation  = 4.dp
                )
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.profile_btn_save), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            message?.let { ProfileSuccessBanner(it, isDark) }
        }
    }
}

@Composable
fun ProfileSectionCard(
    header: String,
    isDark: Boolean,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) PC.DarkCard else PC.LightCard)
            .border(0.5.dp, if (isDark) PC.DarkBorder else PC.LightBorder, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = header,
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 8.dp, end = 14.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = PC.secLbl(isDark),
            letterSpacing = 1.sp
        )
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(if (isDark) PC.DarkBorder else PC.LightBorder))
        content()
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isDark: Boolean,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = {
            Icon(leadingIcon, null, tint = PC.subText(isDark), modifier = Modifier.size(18.dp))
        },
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PC.Green,
            unfocusedBorderColor = if (isDark) PC.DarkBorder else PC.LightBorder,
            focusedLabelColor = PC.Green,
            unfocusedLabelColor = PC.subText(isDark),
            focusedTextColor = PC.onSurf(isDark),
            unfocusedTextColor = PC.onSurf(isDark),
            disabledTextColor = PC.subText(isDark),
            disabledBorderColor = if (isDark) PC.DarkBorder else PC.LightBorder,
            disabledLabelColor = PC.subText(isDark),
            disabledLeadingIconColor = PC.subText(isDark),
            cursorColor = PC.Green,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ProfileSuccessBanner(text: String, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PC.Green.copy(alpha = if (isDark) 0.10f else 0.08f))
            .border(0.5.dp, PC.Green.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(Icons.Filled.CheckCircle, null, tint = PC.Green, modifier = Modifier.size(16.dp))
        Text(text, color = PC.Green, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
