package com.gosuraksha.app.data.mapper

import retrofit2.Response

fun safeString(value: String?, fallback: String = "UNKNOWN"): String =
    value?.trim()?.takeIf { it.isNotEmpty() } ?: fallback

fun safeInt(value: Int?, fallback: Int = 0): Int = (value ?: fallback).coerceAtLeast(0)

fun safeLong(value: Long?, fallback: Long = 0L): Long = (value ?: fallback).coerceAtLeast(0L)

fun <T> safeList(value: List<T>?, fallback: List<T> = emptyList()): List<T> = value ?: fallback

fun <K, V> safeMap(value: Map<K, V>?, fallback: Map<K, V> = emptyMap()): Map<K, V> = value ?: fallback

fun <T> Response<T>.requireData(message: String = "Response body missing"): T {
    return body() ?: throw IllegalStateException(message)
}
