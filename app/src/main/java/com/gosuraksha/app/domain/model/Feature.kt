package com.gosuraksha.app.domain.model

// =============================================================================
// Feature.kt — Plan-to-feature mapping for Go Suraksha.
//
// WHY: Decouples feature gating from raw plan string comparisons.
//      Replace every `plan == "GO_PRO"` check with `hasFeature(Feature.X)`.
//      This single file is the source-of-truth for all feature entitlements.
// =============================================================================

/**
 * Every gated feature in the app.
 * Add new features here — update [User.hasFeature] accordingly.
 */
enum class Feature {
    /** Full cyber card (identity score card, history). Requires PRO or ULTRA. */
    CYBER_CARD,

    /** AI-powered scan explanations. Requires PRO or ULTRA. */
    AI_EXPLAIN,

    /** Unlimited scan quota (text + QR). Requires PRO or ULTRA. */
    UNLIMITED_SCANS,

    /** Unlimited image scan quota. Requires PRO or ULTRA. */
    UNLIMITED_IMAGE_SCANS,

    /** Real-time push alerts for new threats. ULTRA only. */
    REAL_TIME_ALERTS,

    /** Family member protection & shared safety dashboard. ULTRA only. */
    FAMILY_PROTECTION,

    /** Advanced PDF reports & data export. ULTRA only. */
    ADVANCED_REPORTS,

    /** Danger alerts feed access. Requires PRO or ULTRA. */
    DANGER_ALERTS,

    /** Dark web email monitoring. Requires PRO or ULTRA. */
    DARK_WEB_MONITOR,
}

/**
 * Returns true if this user's plan grants access to [feature].
 *
 * Usage:
 *   val user = SessionManager.user.value
 *   if (hasFeature(user, Feature.CYBER_CARD)) { ... }
 */
fun User.hasFeature(feature: Feature): Boolean = when (feature) {
    Feature.CYBER_CARD             -> plan == Plan.GO_PRO || plan == Plan.GO_ULTRA
    Feature.AI_EXPLAIN             -> plan == Plan.GO_PRO || plan == Plan.GO_ULTRA
    Feature.UNLIMITED_SCANS        -> plan == Plan.GO_PRO || plan == Plan.GO_ULTRA
    Feature.UNLIMITED_IMAGE_SCANS  -> plan == Plan.GO_PRO || plan == Plan.GO_ULTRA
    Feature.REAL_TIME_ALERTS       -> plan == Plan.GO_ULTRA
    Feature.FAMILY_PROTECTION      -> plan == Plan.GO_ULTRA
    Feature.ADVANCED_REPORTS       -> plan == Plan.GO_ULTRA
    Feature.DANGER_ALERTS          -> plan == Plan.GO_PRO || plan == Plan.GO_ULTRA
    Feature.DARK_WEB_MONITOR       -> plan == Plan.GO_PRO || plan == Plan.GO_ULTRA
}

/**
 * Null-safe convenience wrapper — returns false when user is null.
 * Use this in @Composable functions where the user may not be loaded yet.
 *
 * @JvmName avoids the JVM platform clash with [User.hasFeature]: both functions
 * would otherwise erase to the same bytecode signature
 * `hasFeature(User, Feature): Boolean` because Kotlin's nullable `User?` and
 * non-null `User` are identical on the JVM.
 */
@JvmName("hasFeatureNullable")
fun hasFeature(user: User?, feature: Feature): Boolean =
    user?.hasFeature(feature) ?: false
