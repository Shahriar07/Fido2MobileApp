package com.konasl.fido2mobileapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konasl.fido2mobileapp.ui.theme.AccentPink
import com.konasl.fido2mobileapp.ui.theme.PrimaryPurple
import com.konasl.fido2mobileapp.ui.theme.TextSecondary

@Composable
fun CircularProgressWithMessage(
    modifier: Modifier = Modifier,
    message: String,
    color: Color = AccentPink
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Circular progress indicator
        CircularProgressAnimation(color = color)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun CircularProgressAnimation(
    modifier: Modifier = Modifier,
    color: Color = PrimaryPurple,
    strokeWidth: Float = 8f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circle_progress_anim")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "angle_animation"
    )
    
    Canvas(modifier = modifier.size(80.dp)) {
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 240f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            for (i in 0 until totalSteps) {
                val stepColor = if (i < currentStep) PrimaryPurple else Color.LightGray
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .padding(horizontal = 2.dp)
                        .background(color = stepColor, shape = MaterialTheme.shapes.small)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Step labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            for (i in 0 until totalSteps) {
                val isCurrentStep = i == currentStep - 1
                val textColor = when {
                    isCurrentStep -> PrimaryPurple
                    i < currentStep - 1 -> TextSecondary
                    else -> Color.LightGray.copy(alpha = 0.7f)
                }
                
                Text(
                    text = stepLabels[i],
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = if (isCurrentStep) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                )
            }
        }
    }
}
