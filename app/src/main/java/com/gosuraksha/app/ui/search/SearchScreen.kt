package com.gosuraksha.app.ui.search

// =============================================================================
// SearchScreen.kt — Global search hub with live backend results
//
// Features:
//   • Search bar with 300ms debounce → GET /search?q=
//   • Filter chips: All | Messages | Images | Emails | Passwords | QR
//   • Live results list with risk badge
//   • Recent searches (session-scoped, rememberSaveable)
//   • Loading / no-results / error states
//   • Quick access categories when idle
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.network.SearchResultItem
import kotlinx.coroutines.delay
import retrofit2.HttpException

// ── Filter chip data ──────────────────────────────────────────────────────────
private enum class SearchFilter(val label: String, val icon: ImageVector, val apiValue: String?) {
    ALL("All",       Icons.Outlined.Search,         null),
    MESSAGES("Messages",  Icons.Outlined.Message,   "SMS"),
    IMAGES("Images",      Icons.Outlined.Image,     "IMAGE"),
    EMAILS("Emails",      Icons.Outlined.Email,     "EMAIL"),
    PASSWORDS("Passwords",Icons.Outlined.Lock,      "PASSWORD"),
    QR("QR Scans",        Icons.Outlined.QrCodeScanner, "QR"),
    THREAT("Threats",     Icons.Outlined.Security,  "THREAT"),
}

// ── Quick-access categories ───────────────────────────────────────────────────
private data class SearchCategory(
    val label: String,
    val icon:  ImageVector,
    val color: Color,
)

private val searchCategories = listOf(
    SearchCategory("Scan History",  Icons.Outlined.History,      Color(0xFF2563EB)),
    SearchCategory("Threat Scans",  Icons.Outlined.Security,     Color(0xFFDC2626)),
    SearchCategory("Image Scans",   Icons.Outlined.Image,        Color(0xFF7C3AED)),
    SearchCategory("Email Checks",  Icons.Outlined.Email,        Color(0xFF059669)),
    SearchCategory("Scam Alerts",   Icons.Outlined.Warning,      Color(0xFFD97706)),
    SearchCategory("QR Scans",      Icons.Outlined.QrCodeScanner,Color(0xFF10B981)),
)

// ── Risk colour helpers ───────────────────────────────────────────────────────
@Composable
private fun riskBadgeColors(risk: String): Pair<Color, Color> = when (risk.uppercase()) {
    "HIGH"   -> Color(0xFFDC2626).copy(alpha = 0.12f) to Color(0xFFDC2626)
    "MEDIUM" -> Color(0xFFF59E0B).copy(alpha = 0.14f) to Color(0xFFD97706)
    "LOW"    -> Color(0xFF10B981).copy(alpha = 0.12f) to Color(0xFF059669)
    else     -> Color(0xFF6B7280).copy(alpha = 0.12f) to Color(0xFF6B7280)
}

private fun iconForType(type: String): ImageVector = when (type.uppercase()) {
    "EMAIL"    -> Icons.Outlined.Email
    "PASSWORD" -> Icons.Outlined.Lock
    "THREAT"   -> Icons.Outlined.Link
    "IMAGE"    -> Icons.Outlined.Image
    "QR"       -> Icons.Outlined.QrCodeScanner
    "SMS","TEXT" -> Icons.Outlined.Message
    else       -> Icons.Outlined.Search
}

private fun iconColorForType(type: String): Color = when (type.uppercase()) {
    "EMAIL"    -> Color(0xFF059669)
    "PASSWORD" -> Color(0xFF7C3AED)
    "THREAT"   -> Color(0xFFDC2626)
    "IMAGE"    -> Color(0xFF2563EB)
    "QR"       -> Color(0xFF10B981)
    else       -> Color(0xFF6B7280)
}

// ── Sealed state ─────────────────────────────────────────────────────────────
private sealed interface SearchState {
    object Idle : SearchState
    object Loading : SearchState
    data class Success(val items: List<SearchResultItem>) : SearchState
    data class Error(val message: String) : SearchState
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun SearchScreen(onBack: () -> Unit = {}) {
    val isDark     = ColorTokens.LocalAppDarkMode.current
    val bg         = if (isDark) Color(0xFF0F0F1A) else Color(0xFFF4F5F8)
    val cardBg     = if (isDark) Color(0xFF1A1A2E) else Color.White
    val cardBorder = if (isDark) Color(0xFF2A2A3E) else Color(0xFFEEEEF5)
    val textPri    = if (isDark) Color(0xFFEEEEFF) else Color(0xFF111111)
    val textSec    = if (isDark) Color(0xFF8888A8) else Color(0xFF666680)
    val inputBg    = if (isDark) Color(0xFF1A1A2E) else Color.White
    val accentBlue = Color(0xFF2563EB)

    var query          by rememberSaveable { mutableStateOf("") }
    var activeFilter   by rememberSaveable { mutableStateOf(SearchFilter.ALL) }
    var recentSearches by rememberSaveable { mutableStateOf(listOf<String>()) }
    var searchState    by remember { mutableStateOf<SearchState>(SearchState.Idle) }
    val focusManager   = LocalFocusManager.current

    // ── 300ms debounced search ────────────────────────────────────────────────
    LaunchedEffect(query, activeFilter) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            searchState = SearchState.Idle
            return@LaunchedEffect
        }
        delay(300L)
        searchState = SearchState.Loading
        searchState = try {
            val response = ApiClient.searchApi.search(
                query  = trimmed,
                filter = activeFilter.apiValue,
                limit  = 30,
            )
            val items = response.data?.results ?: emptyList()
            if (items.isEmpty()) {
                SearchState.Success(emptyList())  // show "No results" not error
            } else {
                SearchState.Success(items)
            }
        } catch (e: java.net.UnknownHostException) {
            SearchState.Error("Check your connection and try again.")
        } catch (e: java.net.SocketTimeoutException) {
            SearchState.Error("Check your connection and try again.")
        } catch (e: java.io.IOException) {
            SearchState.Error("Check your connection and try again.")
        } catch (e: HttpException) {
            val code = e.code()
            when {
                code == 404 -> SearchState.Success(emptyList())  // endpoint missing → show no results
                code in 500..599 -> SearchState.Error("Server error. Try again in a moment.")
                else -> SearchState.Error("Something went wrong. Please try again.")
            }
        } catch (e: Exception) {
            SearchState.Error("Something went wrong. Please try again.")
        }
    }

    fun submitSearch() {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty() && !recentSearches.contains(trimmed)) {
            recentSearches = (listOf(trimmed) + recentSearches).take(8)
        }
        focusManager.clearFocus()
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(bg),
        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // ── Header ─────────────────────────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text          = "SEARCH",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = accentBlue,
                    letterSpacing = 1.sp,
                )
                Text(
                    text       = "Find anything",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textPri,
                )
                Text(
                    text     = "Search your scan history, alerts, and contacts.",
                    fontSize = 13.sp,
                    color    = textSec,
                )
            }
        }

        // ── Search bar ─────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(inputBg)
                    .border(
                        1.dp,
                        if (query.isNotBlank()) accentBlue.copy(alpha = 0.5f) else cardBorder,
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (searchState is SearchState.Loading) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(18.dp),
                        color     = accentBlue,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Outlined.Search,
                        contentDescription = null,
                        tint               = if (query.isNotBlank()) accentBlue else textSec,
                        modifier           = Modifier.size(20.dp),
                    )
                }
                BasicTextField(
                    value         = query,
                    onValueChange = { query = it },
                    modifier      = Modifier.weight(1f),
                    textStyle     = TextStyle(
                        fontSize   = 15.sp,
                        color      = textPri,
                        fontWeight = FontWeight.Normal,
                    ),
                    cursorBrush     = SolidColor(accentBlue),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                    decorationBox   = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text     = "Search scans, alerts, contacts…",
                                fontSize = 15.sp,
                                color    = textSec,
                            )
                        }
                        inner()
                    },
                )
                if (query.isNotBlank()) {
                    Icon(
                        imageVector        = Icons.Outlined.Close,
                        contentDescription = "Clear",
                        tint               = textSec,
                        modifier           = Modifier
                            .size(18.dp)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { query = ""; searchState = SearchState.Idle },
                    )
                }
            }
        }

        // ── Filter chips ───────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SearchFilter.entries.forEach { filter ->
                    val isActive = activeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isActive) accentBlue else cardBg)
                            .border(1.dp, if (isActive) accentBlue else cardBorder, RoundedCornerShape(20.dp))
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { activeFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector        = filter.icon,
                                contentDescription = null,
                                tint               = if (isActive) Color.White else textSec,
                                modifier           = Modifier.size(14.dp),
                            )
                            Text(
                                text       = filter.label,
                                fontSize   = 12.sp,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (isActive) Color.White else textSec,
                            )
                        }
                    }
                }
            }
        }

        // ── Content area ───────────────────────────────────────────────────
        when (val state = searchState) {

            SearchState.Idle -> {
                // Recent searches
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text          = "RECENT SEARCHES",
                                fontSize      = 11.sp,
                                fontWeight    = FontWeight.SemiBold,
                                color         = textSec,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text     = "Clear all",
                                fontSize = 12.sp,
                                color    = accentBlue,
                                modifier = Modifier.clickable(
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { recentSearches = emptyList() },
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                        ) {
                            recentSearches.forEachIndexed { idx, term ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication        = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) { query = term }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        imageVector        = Icons.Outlined.History,
                                        contentDescription = null,
                                        tint               = textSec,
                                        modifier           = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text     = term,
                                        fontSize = 14.sp,
                                        color    = textPri,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Icon(
                                        imageVector        = Icons.Outlined.Close,
                                        contentDescription = "Remove",
                                        tint               = textSec,
                                        modifier           = Modifier
                                            .size(14.dp)
                                            .clickable(
                                                indication        = null,
                                                interactionSource = remember { MutableInteractionSource() },
                                            ) { recentSearches = recentSearches.filter { it != term } },
                                    )
                                }
                                if (idx < recentSearches.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .padding(horizontal = 16.dp)
                                            .background(cardBorder)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick access categories
                item {
                    Text(
                        text          = "QUICK ACCESS",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = textSec,
                        letterSpacing = 1.sp,
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(20.dp)),
                    ) {
                        searchCategories.forEachIndexed { idx, cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication        = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { /* Navigate */ }
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(cat.color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector        = cat.icon,
                                        contentDescription = null,
                                        tint               = cat.color,
                                        modifier           = Modifier.size(18.dp),
                                    )
                                }
                                Text(
                                    text       = cat.label,
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = textPri,
                                    modifier   = Modifier.weight(1f),
                                )
                            }
                            if (idx < searchCategories.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .padding(horizontal = 18.dp)
                                        .background(cardBorder)
                                )
                            }
                        }
                    }
                }
            }

            SearchState.Loading -> {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = accentBlue, strokeWidth = 2.5.dp)
                    }
                }
            }

            is SearchState.Success -> {
                if (state.items.isEmpty()) {
                    item { NoResultsState(query = query, textPri = textPri, textSec = textSec) }
                } else {
                    item {
                        Text(
                            text          = "${state.items.size} RESULT${if (state.items.size != 1) "S" else ""}",
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.SemiBold,
                            color         = textSec,
                            letterSpacing = 1.sp,
                        )
                    }
                    items(state.items) { result ->
                        SearchResultCard(
                            item       = result,
                            cardBg     = cardBg,
                            cardBorder = cardBorder,
                            textPri    = textPri,
                            textSec    = textSec,
                            onClick    = {
                                val trimmed = query.trim()
                                if (trimmed.isNotEmpty() && !recentSearches.contains(trimmed)) {
                                    recentSearches = (listOf(trimmed) + recentSearches).take(8)
                                }
                            },
                        )
                    }
                }
            }

            is SearchState.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFDC2626).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Text(
                            text      = state.message,
                            fontSize  = 14.sp,
                            color     = Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ── Search result card ────────────────────────────────────────────────────────
@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    cardBg: Color,
    cardBorder: Color,
    textPri: Color,
    textSec: Color,
    onClick: () -> Unit,
) {
    val typeColor    = iconColorForType(item.type)
    val typeIcon     = iconForType(item.type)
    val (badgeBg, badgeFg) = riskBadgeColors(item.risk)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick,
            )
            .padding(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(typeColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = typeIcon,
                contentDescription = null,
                tint               = typeColor,
                modifier           = Modifier.size(20.dp),
            )
        }

        // Title + subtitle
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = item.title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = textPri,
                maxLines   = 1,
            )
            Text(
                text     = item.subtitle,
                fontSize = 12.sp,
                color    = textSec,
                maxLines = 2,
            )
        }

        // Risk badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = item.risk,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                color      = badgeFg,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

// ── No results state ──────────────────────────────────────────────────────────
@Composable
private fun NoResultsState(query: String, textPri: Color, textSec: Color) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector        = Icons.Outlined.Search,
            contentDescription = null,
            tint               = textSec,
            modifier           = Modifier.size(44.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "No results for \"$query\"",
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = textPri,
        )
        Text(
            text     = "Try a different keyword or browse the categories above.",
            fontSize = 13.sp,
            color    = textSec,
        )
    }
}
