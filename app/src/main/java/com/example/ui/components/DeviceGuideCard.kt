package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PilotSecondary

@Composable
fun DeviceGuideCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_guide_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Device Guide",
                    tint = PilotSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OPPO Reno5 / ColorOS 13 Setup Guide",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            GuideStep(
                step = "1",
                title = "تفعيل Accessibility Service في ColorOS",
                desc = "اضغط [Enable Accessibility] ثم ابحث عن 'DroidPilot' وقم بتفعيل الخيار والموافقة على الصلاحية."
            )

            GuideStep(
                step = "2",
                title = "تثبيت التطبيق في الخلفية (ColorOS Background)",
                desc = "في ColorOS: الإعدادات > التطبيقات > DroidPilot > استخدام البطارية > السماح بالنشاط في الخلفية (حتى لا يغلق النظام الخدمة)."
            )

            GuideStep(
                step = "3",
                title = "استخدام الـ Floating Controller فوق TikTok",
                desc = "فعّل خيار 'Floating Controller Overlay' في الأعلى. سيظهر زر عائم فوق شاشة TikTok يمكنك من الضغط على [Swipe Up] و [Read Screen] مباشرة أثناء مشاهدة الفيديو!"
            )

            GuideStep(
                step = "4",
                title = "التقاط Screenshot",
                desc = "اضغط [Take Screenshot] ووافق على طلب إذن مشاركة الشاشة مرة واحدة لتخزين صورة حية لشاشة TikTok داخل التطبيق."
            )
        }
    }
}

@Composable
private fun GuideStep(step: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF312E81)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
