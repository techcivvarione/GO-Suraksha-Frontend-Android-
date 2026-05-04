package com.gosuraksha.app.learn.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LearnArticle(
    @SerializedName(value = "id", alternate = ["_id", "article_id"])
    val id: String? = null,
    val title: String = "",
    val subtitle: String? = null,
    @SerializedName(value = "description", alternate = ["summary", "excerpt"])
    val description: String? = null,
    val content: String? = null,
    @SerializedName(value = "image_url", alternate = ["image", "thumbnail", "cover_image"])
    val imageUrl: String? = null,
    @SerializedName(value = "category", alternate = ["topic"])
    val category: String? = null,
    @SerializedName(value = "level", alternate = ["difficulty"])
    val level: String? = null,
    @SerializedName(value = "read_time", alternate = ["readTime", "minutes_to_read"])
    val readTime: String? = null,
    @SerializedName(value = "tags", alternate = ["labels"])
    val tags: List<String>? = null,
    @SerializedName(value = "link", alternate = ["url", "article_url"])
    val link: String? = null,
    @SerializedName(value = "is_trending", alternate = ["trending"])
    val isTrending: Boolean = false,
    @SerializedName(value = "is_recommended", alternate = ["recommended", "featured"])
    val isRecommended: Boolean = false
) : Serializable {
    val stableId: String
        get() = id ?: link ?: "$title|${category.orEmpty()}"

    val displaySubtitle: String?
        get() = subtitle?.takeIf { it.isNotBlank() } ?: description?.takeIf { it.isNotBlank() }

    val displayDescription: String?
        get() = description?.takeIf { it.isNotBlank() } ?: content?.takeIf { it.isNotBlank() }

    val displayCategory: String
        get() = category?.takeIf { it.isNotBlank() } ?: "General Safety"

    val displayLevel: String?
        get() = level?.takeIf { it.isNotBlank() }

    val displayReadTime: String
        get() = readTime?.takeIf { it.isNotBlank() } ?: "5 min read"
}

data class LearnTopic(
    val label: String,
    val apiValue: String? = label
)
