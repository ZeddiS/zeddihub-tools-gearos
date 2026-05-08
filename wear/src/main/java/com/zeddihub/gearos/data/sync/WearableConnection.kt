package com.zeddihub.gearos.data.sync

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stav spojení s párovaným telefonem (Wearable Data Layer).
 * M1 doplní [observe] s `CapabilityClient` listenerem.
 */
@Singleton
class WearableConnection @Inject constructor(
    private val ctx: Context,
) {
    enum class State { CONNECTED, DISCONNECTED, UNKNOWN }

    suspend fun current(): State {
        val nodes = Wearable.getNodeClient(ctx).connectedNodes.await()
        return if (nodes.any { it.isNearby }) State.CONNECTED else State.DISCONNECTED
    }

    fun observe(): Flow<State> = flow {
        emit(current())
        // M1: napojit CapabilityClient.OnCapabilityChangedListener
    }.flowOn(Dispatchers.IO)
}
