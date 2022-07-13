package com.zzoin.controller

import com.zzoin.common.AppConfig
import com.zzoin.model.UserDTO
import com.zzoin.repository.MemberRepository
import kotlinx.coroutines.Deferred

class MemberControllerImpl(private val memberRepository : MemberRepository) : MemberController{
    override fun getMemberByPhone(phone: String): Deferred<Pair<UserDTO?, String>> = memberRepository.findMemberByPhoneAsync(phone)

    override fun getMemberByProUid(proUid: String): Deferred<ArrayList<UserDTO>> = memberRepository.findMemberByProUidAsync(proUid)
}