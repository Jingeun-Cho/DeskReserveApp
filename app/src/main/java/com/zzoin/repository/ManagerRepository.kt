package com.zzoin.repository

import com.zzoin.model.ManagerDTO
import kotlinx.coroutines.Deferred

interface ManagerRepository {
    fun findManagerByUidAsync(uid : String): Deferred<ManagerDTO?>
}