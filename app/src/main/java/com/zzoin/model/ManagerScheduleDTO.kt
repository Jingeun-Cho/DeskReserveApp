package com.zzoin.model

data class ManagerScheduleDTO(
    var schedule : List<DailySchedule> = listOf()
){
    data class DailySchedule(
        var dayOfWeek : String = "",
        var kor : String = "",
        var working : String = "",
        var workingStart : Long = 0,
        var workingFinish : Long = 0,
        var restStart : Long = 0,
        var restFinish : Long = 0

    )
}
