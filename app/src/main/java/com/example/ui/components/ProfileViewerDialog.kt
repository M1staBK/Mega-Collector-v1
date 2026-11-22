package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.AchievementCatalog
import com.example.model.BazaarCollectorProfile
import com.example.ui.theme.CollectorTheme
import com.example.ui.theme.RarityUncommon

@Composable
fun ProfileViewerDialog(
    profile: BazaarCollectorProfile,
    onDismiss: () -> Unit
) {
    val palette = CollectorTheme.colors
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
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = profile.alias,
                                tint = profile.avatarColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.alias,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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

                // Equipped Showcase Badges (up to 3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "EQUIPPED BADGES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.muted
                    )
                    val resolvedBadges = profile.badgeIds.mapNotNull { AchievementCatalog.byId(it) }
                    if (resolvedBadges.isEmpty()) {
                        Text(
                            text = "No achievement badges pinned yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            resolvedBadges.take(3).forEach { badge ->
                                Surface(
                                    color = badge.accentColor.copy(alpha = 0.13f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, badge.accentColor.copy(alpha = 0.55f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = BadgeIconVector(badge.iconStyle),
                                            contentDescription = badge.title,
                                            tint = badge.accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = badge.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
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
