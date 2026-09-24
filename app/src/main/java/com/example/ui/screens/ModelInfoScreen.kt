package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AssistiveSafetyNotice
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.DeepCockpit
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber

@Composable
fun ModelInfoScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpit)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MACHINE LEARNING SPECIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Model Architecture & Pipeline",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "OpenCV Image Processing + Keras Convolutional Classifier",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StatusBadge(
                        label = "SYSTEM VERIFIED",
                        statusColor = SafetyGreen,
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Model Key Attributes Table
        Text(
            text = "CORE MODEL ATTRIBUTES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ModelSpecRow(label = "Model Name", value = "MobileNetV2-FatigueNet / CNN Multi-Task Classifier")
                ModelSpecRow(label = "Primary Purpose", value = "Real-time non-intrusive driver drowsiness and fatigue classification")
                ModelSpecRow(label = "Input Dimensions", value = "224 x 224 x 3 RGB / 68-Point Facial Landmark Vector")
                ModelSpecRow(label = "Output Classes", value = "Multi-class: [Alert / Open Eyes, Drowsy / Closed Eyes, Yawning]")
                ModelSpecRow(label = "Regression Outputs", value = "EAR (Eye Aspect Ratio), MAR (Mouth Aspect Ratio), Blink Frequency")
                ModelSpecRow(label = "Inference Latency", value = "18ms - 32ms per frame on mobile neural accelerator")
                ModelSpecRow(label = "Model Status", value = "Deployed / Active Hybrid Telemetry Inference Engine", isLast = true)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // OpenCV vs Keras Framework Roles
        Text(
            text = "FRAMEWORK RESPONSIBILITIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // OpenCV Role Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = TechCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "OpenCV (Computer Vision Role)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechCyan
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Camera Stream Acquisition: Captures raw video feed from the driver-facing cabin camera at 30 FPS.\n" +
                        "• Image Preprocessing: Grayscale conversion, CLAHE (Contrast Limited Adaptive Histogram Equalization) to stabilize low-light nighttime vehicle environments.\n" +
                        "• Facial ROI Detection: Uses Haar Cascade Classifiers & dlib shape predictor to crop face bounding boxes.\n" +
                        "• Geometric Metric Computation: Computes continuous Euclidian distance vectors for the Eye Aspect Ratio (EAR) and Mouth Aspect Ratio (MAR).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Keras/TensorFlow Role Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SafetyGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SafetyGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Keras & TensorFlow (Deep Learning Role)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyGreen
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Neural Architecture: MobileNetV2 with depthwise separable convolutions for high computational efficiency on edge devices.\n" +
                        "• Feature Extraction: Learns subtle micro-closure textures, eyelid drooping patterns, and spectacles glare invariance.\n" +
                        "• Dataset Training: Pre-trained and fine-tuned on academic datasets including MRL Eye Dataset (37,000+ eye crops) and YawDD (Yawning Detection Dataset).\n" +
                        "• Confidence Estimation: Generates softmax probability distributions to filter out noise, yaw movements, and driver speech.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mathematical Formulas (EAR & MAR)
        Text(
            text = "PHYSIOLOGICAL ALGORITHMIC FORMULAS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Eye Aspect Ratio (EAR) - Soukupová & Čech Algorithm:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = DeepCockpit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "EAR = ( ||p2 - p6|| + ||p3 - p5|| ) / ( 2 * ||p1 - p4|| )",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TechCyan,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Threshold: EAR > 0.28 (Open), EAR < 0.22 for ≥ 1.5s triggers Micro-Sleep Alarm.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "2. Mouth Aspect Ratio (MAR) - Yawning Geometric Metric:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = DeepCockpit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MAR = ( ||m2 - m8|| + ||m3 - m7|| + ||m4 - m6|| ) / ( 2 * ||m1 - m5|| )",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SafetyGreen,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Threshold: MAR > 0.65 sustained for > 2.5s classifies a biological yawn event.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        AssistiveSafetyNotice()
    }
}

@Composable
private fun ModelSpecRow(
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(130.dp)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceBorder.copy(alpha = 0.5f))
            )
        }
    }
}
