package com.zzoin.controller

import com.google.firebase.inject.Deferred
import com.zzoin.model.ManagerDTO


interface ManagerController {
    fun getManagerByUid(uid : String) : kotlinx.coroutines.Deferred<ManagerDTO?>
}