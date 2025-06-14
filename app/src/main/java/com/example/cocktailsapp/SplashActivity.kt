package com.example.cocktailsapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cocktailsapp.ui.theme.CocktailsAppTheme
import kotlinx.coroutines.delay
import kotlin.times
import kotlin.unaryMinus

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CocktailsAppTheme {
                SplashScreen(onSplashFinished = {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    // Animacje rotacji dla efektu mieszania koktajlu
    val rotation by animateFloatAsState(
        targetValue = if (startAnimation) 15f else 0f,
        animationSpec = repeatable(
            iterations = 4,
            animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Animacja pulsowania ikony
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.5f,
        animationSpec = repeatable(
            iterations = 2,
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Animacja bąbelków
    val bubblesOpacity by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 1500),
        label = "bubblesOpacity"
    )

    // Animacja tekstu - najpierw przezroczystość
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 2000),
        label = "textAlpha"
    )

    // Animacja tekstu - przesunięcie od dołu
    val textOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 50f,
        animationSpec = tween(durationMillis = 800, delayMillis = 2000, easing = EaseOutBack),
        label = "textOffset"
    )

    // Animacja koloru tła
    val backgroundColor by animateColorAsState(
        targetValue = if (startAnimation)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(durationMillis = 3000),
        label = "backgroundColor"
    )

    // Efekt rozpoczynający animację i kończący splash screen
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(5000) // Wydłużony czas trwania ekranu
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Bąbelki w tle (animowane)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(bubblesOpacity)
        ) {
            repeat(12) { index ->
                val bubbleDelay = index * 200
                val bubbleSize = (15 + index % 4 * 10).dp

                Bubble(
                    delayMillis = bubbleDelay,
                    size = bubbleSize,
                    startOffsetX = -100f + (index * 50),
                    startOffsetY = 400f - (index * 30)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animowana ikona koktajlu z efektem cienia
            Box(contentAlignment = Alignment.Center) {
                // Efekt blasku/cienia
                Icon(
                    imageVector = Icons.Default.LocalBar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(130.dp)
                        .scale(if (startAnimation) 1.3f else 0.5f)
                        .alpha(0.5f)
                        .offset(x = 3.dp, y = 3.dp)
                )

                // Główna ikona
                Icon(
                    imageVector = Icons.Default.LocalBar,
                    contentDescription = "Cocktail icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .rotate(rotation)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animowany tekst - przesunięcie z dołu + fade in
            Text(
                text = "CocktailsApp🍹",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset.dp)
            )

            // Podtytuł
            Text(
                text = "Odkryj świat koktajli",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier
                    .alpha(textAlpha * 0.7f)
                    .offset(y = (textOffset * 1.2f).dp)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun Bubble(
    delayMillis: Int,
    size: Dp,
    startOffsetX: Float,
    startOffsetY: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")

    // Animacja pozycji Y (ruch do góry)
    val offsetY by infiniteTransition.animateFloat(
        initialValue = startOffsetY,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000 + delayMillis, delayMillis = delayMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbleOffsetY"
    )

    // Animacja pozycji X (lekki ruch na boki)
    val offsetX by infiniteTransition.animateFloat(
        initialValue = startOffsetX,
        targetValue = startOffsetX + 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000 + delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleOffsetX"
    )

    // Animacja przezroczystości
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000 + delayMillis, delayMillis = delayMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbleAlpha"
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(size)
            .alpha(alpha)
            .background(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                shape = CircleShape
            )
    )
}
@Composable
fun CocktailLoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cocktailLoading")

    // Animacja rotacji shakera
    val shakerRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakerRotation"
    )

    // Animacja pulsowania
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakerScale"
    )

    // Kolor bąbelków
    val bubbleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .size(size)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Bąbelki
        repeat(6) { index ->
            Bubble(
                delayMillis = index * 400,
                size = (5 + index % 3 * 3).dp,
                startOffsetX = (-20 + index * 8).toFloat(),
                startOffsetY = (10 + index * 10).toFloat(),
                bubbleColor = bubbleColor,
                containerSize = size
            )
        }

        // Ikona shakera/koktajlu
        Icon(
            imageVector = Icons.Default.LocalBar,
            contentDescription = "Ładowanie",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(size * 0.6f)
                .rotate(shakerRotation)
                .scale(scale)
        )
    }
}

@Composable
fun Bubble(
    delayMillis: Int,
    size: Dp,
    startOffsetX: Float,
    startOffsetY: Float,
    bubbleColor: Color,
    containerSize: Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble")

    val maxOffset = (containerSize.value * 0.8f)

    // Animacja pozycji Y (ruch do góry)
    val offsetY by infiniteTransition.animateFloat(
        initialValue = startOffsetY,
        targetValue = -maxOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(1500 + delayMillis, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbleY"
    )

    // Animacja pozycji X (lekki ruch na boki)
    val offsetX by infiniteTransition.animateFloat(
        initialValue = startOffsetX,
        targetValue = startOffsetX + 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleX"
    )

    // Animacja przezroczystości
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500 + delayMillis, delayMillis = delayMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbleAlpha"
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(size)
            .alpha(alpha)
            .background(
                color = bubbleColor,
                shape = CircleShape
            )
    )
}