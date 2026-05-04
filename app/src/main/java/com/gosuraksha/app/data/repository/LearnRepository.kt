package com.gosuraksha.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.gosuraksha.app.learn.model.LearnArticle
import com.gosuraksha.app.network.LearnApi

class LearnRepository(
    private val api: LearnApi,
    private val gson: Gson = Gson()
) {

    suspend fun getArticles(category: String? = null): List<LearnArticle> {
        return parseArticles(api.getArticles(category).data)
    }

    suspend fun getRecommended(): List<LearnArticle> {
        return parseArticles(api.getRecommended().data)
    }

    private fun parseArticles(element: JsonElement?): List<LearnArticle> {
        if (element == null || element.isJsonNull) return emptyList()

        if (element.isJsonArray) {
            return gson.fromJson(element, articleListType())
        }

        if (!element.isJsonObject) return emptyList()

        val obj = element.asJsonObject
        findArticleArray(obj)?.let { return gson.fromJson(it, articleListType()) }

        return runCatching { gson.fromJson(obj, LearnArticle::class.java) }
            .getOrNull()
            ?.takeIf { it.title.isNotBlank() }
            ?.let(::listOf)
            ?: emptyList()
    }

    private fun findArticleArray(obj: JsonObject): JsonElement? {
        val keys = listOf("articles", "items", "results", "recommended", "data")
        return keys.firstNotNullOfOrNull { key ->
            obj.get(key)?.takeIf { it.isJsonArray }
        }
    }

    private fun articleListType() = object : TypeToken<List<LearnArticle>>() {}.type
}
