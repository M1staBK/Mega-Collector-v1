package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.AchievementBadgeDefinition
import com.example.model.AchievementCatalog
import com.example.model.CollectorPublicProfile
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.RarityUncommon

@Composable
fun ProfileViewerDialog(
    profile: CollectorPublicProfile,
    onDismiss: () -> Unit
) {
    val palette = CollectorTheme.colors
    var inspectedBadge by remember(profile.traderId) { mutableStateOf<AchievementBadgeDefinition?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = palette.cardSurface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 10.dp,
            border = BorderStroke(1.2.dp, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_viewer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Avatar & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = profile.avatarColor.copy(alpha = 0.18f),
                        shape = CircleShape,
                        border = BorderStroke(1.8.dp, profile.avatarColor),
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (profile.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = rememberUserAvatarRequest(profile.photoUrl),
                                contentDescription = profile.alias,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = profile.alias,
                                    tint = profile.avatarColor,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = profile.alias,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = profile.avatarColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, profile.avatarColor.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = profile.avatarColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = profile.rankTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = profile.avatarColor
                                    )
                                }
                            }
                            if (profile.mlbbIgn.isNotBlank()) {
                                Surface(
                                    color = palette.frostSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, palette.border)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = null,
                                            tint = palette.accentPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val serverPart = if (profile.mlbbServerId.isNotBlank()) " (${profile.mlbbServerId})" else ""
                                        Text(
                                            text = "${profile.mlbbIgn}$serverPart",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close profile",
                            tint = palette.muted
                        )
                    }
                }

                // Collector Motto
                if (profile.motto.isNotBlank()) {
                    Surface(
                        color = palette.frostSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "“${profile.motto}”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.ink,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Stats Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileMiniStat(
                        label = "Prestige",
                        value = "%,d".format(profile.prestigeScore),
                        accent = palette.gold,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMiniStat(
                        label = "Skins",
                        value = "${profile.uniqueSkinsCount}",
                        accent = palette.accentPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMiniStat(
                        label = "Trades",
                        value = "${profile.tradesDone}",
                        accent = RarityUncommon,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Equipped Showcase Badges (Icon-only medallions; tap or hover reveals badge text!)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EQUIPPED BADGES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.muted
                        )
                        Text(
                            text = "Tap / hover icon",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accentPrimary
                        )
                    }
                    val resolvedBadges = profile.badgeIds.mapNotNull { AchievementCatalog.byId(it) }
                    if (resolvedBadges.isEmpty()) {
                        Text(
                            text = "No achievement badges pinned yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            resolvedBadges.take(3).forEach { badge ->
                                val isSelected = inspectedBadge?.id == badge.id
                                Surface(
                                    color = badge.accentColor.copy(alpha = if (isSelected) 0.24f else 0.13f),
                                    shape = CircleShape,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.2.dp,
                                        color = badge.accentColor.copy(alpha = if (isSelected) 1f else 0.6f)
                                    ),
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .pointerInput(badge.id) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    when (event.type) {
                                                        PointerEventType.Enter -> inspectedBadge = badge
                                                        PointerEventType.Exit -> if (inspectedBadge?.id == badge.id) inspectedBadge = null
                                                    }
                                                }
                                            }
                                        }
                                        .clickable {
                                            inspectedBadge = if (isSelected) null else badge
                                        }
                                        .testTag("viewer_badge_icon_${badge.id}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = BadgeIconVector(badge.iconStyle),
                                            contentDescription = badge.title,
                                            tint = badge.accentColor,
                                            modifier = Modifier.size(25.dp)
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = inspectedBadge != null) {
                            inspectedBadge?.let { b ->
                                Surface(
                                    color = b.accentColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, b.accentColor.copy(alpha = 0.48f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("viewer_badge_tooltip")
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = b.title,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = palette.ink
                                        )
                                        Text(
                                            text = b.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.muted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Favorite Skins Showcase
                if (profile.showcaseSkins.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "FEATURED VAULT SHOWCASE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.muted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile.showcaseSkins.take(3).forEach { skin ->
                                Surface(
                                    color = palette.frostSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, skin.rarity.primaryColor.copy(alpha = 0.6f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (skin.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = rememberMlbbSquareImageRequest(skin.imageUrl),
                                                contentDescription = skin.skinName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(skin.rarity.primaryColor.copy(alpha = 0.25f))
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = skin.heroName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = skin.rarity.primaryColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = skin.skinName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = palette.ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMiniStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val palette = CollectorTheme.colors
    Surface(
        modifier = modifier,
        color = palette.frostSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = accent,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = palette.muted,
                maxLines = 1
            )
        }
    }
}
