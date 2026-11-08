package com.khepilabs.khepitube.ui.screens.settings.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.recommendation.KhepiPersona
import com.khepilabs.khepitube.ui.screens.settings.taste.PersonaEmblem

private val CardPadding = 20.dp
private val CardSpacing = 16.dp
private val EmblemBadgeSize = 64.dp
private val EmblemSize = 48.dp
private val ChevronBadgeSize = 36.dp

/** The way into Your taste: the persona the engine has settled on, styled as the KhepiTube Hero Card. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PersonaEntryCard(
    persona: KhepiPersona?,
    selected: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onOpen,
        selected = selected,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) colors.primaryContainer.copy(alpha = 0.3f) else colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(CardPadding),
            horizontalArrangement = Arrangement.spacedBy(CardSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(EmblemBadgeSize)
                        .clip(CircleShape)
                        .background(colors.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                if (persona != null) {
                    PersonaEmblem(persona, Modifier.size(EmblemSize))
                } else {
                    LoadingIndicator(Modifier.size(32.dp))
                }
            }

            Column(Modifier.weight(1f)) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colors.primaryContainer.copy(alpha = 0.5f),
                    contentColor = colors.primary,
                ) {
                    Text(
                        text = stringResource(R.string.taste_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = persona?.let { stringResource(it.titleRes) } ?: stringResource(R.string.taste_card_loading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = persona?.let { stringResource(it.descriptionRes) } ?: stringResource(R.string.taste_card_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                shape = CircleShape,
                color = colors.surfaceContainerHigh,
                modifier = Modifier.size(ChevronBadgeSize),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
