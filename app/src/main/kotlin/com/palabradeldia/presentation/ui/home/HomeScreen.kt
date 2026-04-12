package com.palabradeldia.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.palabradeldia.R
import com.palabradeldia.domain.model.DailyWord
import com.palabradeldia.domain.model.Definition
import com.palabradeldia.domain.model.Word
import com.palabradeldia.presentation.viewmodel.HomeUiState
import com.palabradeldia.presentation.viewmodel.HomeViewModel
import java.util.Locale
import java.time.format.TextStyle as JTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Outlined.StarBorder, contentDescription = stringResource(R.string.nav_favourites))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.nav_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        val state = uiState
        when {
            state is HomeUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            state is HomeUiState.Error ->
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
                }
            state is HomeUiState.Success ->
                WordContent(dailyWord = state.dailyWord, isFavourite = state.isFavourite,
                    onToggleFav = viewModel::onToggleFavourite,
                    modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun WordContent(dailyWord: DailyWord, isFavourite: Boolean, onToggleFav: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val dateLabel = remember(dailyWord.date) {
            val locale = Locale.getDefault()
            val day = dailyWord.date.dayOfWeek.getDisplayName(JTextStyle.FULL, locale)
                .replaceFirstChar { it.uppercase() }
            val num = dailyWord.date.dayOfMonth
            val month = dailyWord.date.month.getDisplayName(JTextStyle.FULL, locale)
            "$day, $num de $month"
        }
        Text(dateLabel, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.alpha(0.75f))
        WordCard(word = dailyWord.word, isFavourite = isFavourite, onToggleFav = onToggleFav)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun WordCard(word: Word, isFavourite: Boolean, onToggleFav: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(word.word,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Light, fontSize = 40.sp),
                        color = MaterialTheme.colorScheme.primary)
                    val posLabel = buildString {
                        append(word.pos)
                        if (!word.gender.isNullOrBlank()) append("  ·  ${word.gender}")
                    }
                    Text(posLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalIconToggleButton(checked = isFavourite, onCheckedChange = { onToggleFav() }) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(
                            if (isFavourite) R.string.remove_from_favourites else R.string.add_to_favourites)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            word.definitions.forEachIndexed { idx, def ->
                DefinitionItem(definition = def, showNumber = word.definitions.size > 1)
                if (idx < word.definitions.lastIndex) Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DefinitionItem(definition: Definition, showNumber: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showNumber) {
                Text("${definition.number}.", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Text(definition.text, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface)
        }
        if (!definition.example.isNullOrBlank()) {
            Text("« ${definition.example} »",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = if (showNumber) 20.dp else 0.dp))
        }
    }
}
