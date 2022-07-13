package com.zzoin.controller

import com.zzoin.model.ManagerDTO
import com.zzoin.repository.ManagerRepository
import kotlinx.coroutines.Deferred

class ManagerControllerImpl(private val managerRepository: ManagerRepository) : ManagerController{
    override fun getManagerByUid(uid: String): Deferred<ManagerDTO?> = managerRepository.findManagerByUidAsync(uid)
}