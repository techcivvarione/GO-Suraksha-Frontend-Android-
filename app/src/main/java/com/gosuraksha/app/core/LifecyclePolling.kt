package com.gosuraksha.app.core

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

fun periodicTickFlow(
    periodMillis: Long,
    initialDelayMillis: Long = periodMillis
): Flow<Unit> = flow {
    if (initialDelayMillis <= 0L) {
        emit(Unit)
    } else {
        delay(initialDelayMillis)
    }
    while (currentCoroutineContext().isActive) {
        emit(Unit)
        delay(periodMillis)
    }
}
