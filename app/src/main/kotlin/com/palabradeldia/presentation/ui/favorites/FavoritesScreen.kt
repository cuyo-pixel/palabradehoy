package com.palabradeldia.presentation.ui.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palabradeldia.R
import com.palabradeldia.domain.model.Word
import com.palabradeldia.presentation.viewmodel.FavoritesUiState
import com.palabradeldia.presentation.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favourites_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is FavoritesUiState.Empty -> EmptyState(Modifier.padding(innerPadding))
            is FavoritesUiState.List  -> FavoritesList(
                words    = state.words,
                onRemove = viewModel::onRemove,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            SadCat(modifier = Modifier.size(88.dp))
            Text(
                stringResource(R.string.favourites_empty),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SadCat(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = w * 0.045f

        val stroke = Paint().apply {
            this.color = color
            style       = PaintingStyle.Stroke
            strokeWidth = sw
            strokeCap   = StrokeCap.Round
            strokeJoin  = StrokeJoin.Round
        }
        val fill = Paint().apply {
            this.color = color
            style       = PaintingStyle.Fill
        }

        drawContext.canvas.apply {
            // Head
            drawCircle(Offset(w * 0.50f, h * 0.57f), w * 0.33f, stroke)

            // Left ear — drooping outward and downward
            drawPath(Path().apply {
                moveTo(w * 0.26f, h * 0.34f)
                lineTo(w * 0.10f, h * 0.26f)
                lineTo(w * 0.22f, h * 0.46f)
                close()
            }, stroke)

            // Right ear — drooping outward and downward
            drawPath(Path().apply {
                moveTo(w * 0.74f, h * 0.34f)
                lineTo(w * 0.90f, h * 0.26f)
                lineTo(w * 0.78f, h * 0.46f)
                close()
            }, stroke)

            // Left eye — sad arc curving downward
            drawPath(Path().apply {
                moveTo(w * 0.36f, h * 0.51f)
                quadraticTo(w * 0.41f, h * 0.57f, w * 0.46f, h * 0.51f)
            }, stroke)

            // Right eye — sad arc curving downward
            drawPath(Path().apply {
                moveTo(w * 0.54f, h * 0.51f)
                quadraticTo(w * 0.59f, h * 0.57f, w * 0.64f, h * 0.51f)
            }, stroke)

            // Mouth — small sad curve
            drawPath(Path().apply {
                moveTo(w * 0.41f, h * 0.71f)
                quadraticTo(w * 0.50f, h * 0.66f, w * 0.59f, h * 0.71f)
            }, stroke)

            // Nose
            drawCircle(Offset(w * 0.50f, h * 0.62f), w * 0.028f, fill)

            // Left whiskers
            drawLine(color, Offset(w * 0.17f, h * 0.63f), Offset(w * 0.38f, h * 0.64f), sw * 0.7f)
            drawLine(color, Offset(w * 0.15f, h * 0.69f), Offset(w * 0.38f, h * 0.68f), sw * 0.7f)

            // Right whiskers
            drawLine(color, Offset(w * 0.83f, h * 0.63f), Offset(w * 0.62f, h * 0.64f), sw * 0.7f)
            drawLine(color, Offset(w * 0.85f, h * 0.69f), Offset(w * 0.62f, h * 0.68f), sw * 0.7f)
        }
    }
}

@Composable
private fun FavoritesList(
    words: List<Word>,
    onRemove: (Word) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(words, key = { it.id }) { word ->
            AnimatedVisibility(
                visible = true,
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                FavoriteItem(word = word, onRemove = { onRemove(word) })
            }
        }
    }
}

@Composable
private fun FavoriteItem(word: Word, onRemove: () -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    word.word,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    word.pos,
                    style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                word.definitions.firstOrNull()?.let { def ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        def.text,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.favourites_delete),
                    tint               = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
