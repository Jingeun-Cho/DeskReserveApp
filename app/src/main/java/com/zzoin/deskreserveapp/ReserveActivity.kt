package com.zzoin.deskreserveapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zzoin.common.AppConfig.convertTimestampToSimpleDate
import com.zzoin.deskreserveapp.databinding.ActivityReserveBinding
import com.zzoin.deskreserveapp.databinding.ItemDailyScheduleBinding
import com.zzoin.model.LessonDTO
import com.zzoin.model.ManagerDTO
import com.zzoin.model.ManagerScheduleDTO
import com.zzoin.model.UserDTO
import kotlinx.coroutines.*

import java.util.*
import kotlin.collections.ArrayList

class ReserveActivity : AppCompatActivity() {
    private lateinit var binding : ActivityReserveBinding
    private val firestore = Firebase.firestore
    private lateinit var manager : ManagerDTO
    private lateinit var user : UserDTO
    private lateinit var userDocId : String
    private val calendar = Calendar.getInstance()
    private var isFirst = true
    private var today = 0
    companion object{
        const val BACK_TO_HOME_DELAY = 30000L

    }
    private val reserveResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK){
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private var checkTouchEventTimer : Job? = CoroutineScope(Dispatchers.Main).launch {
        val displayTime = (BACK_TO_HOME_DELAY/1000).toInt()
        repeat(displayTime){
            binding.textReserve.text = ("예약 시스템 (${displayTime - it})")
            delay(1000L)
        }
        delay(BACK_TO_HOME_DELAY)
        finish()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReserveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = intent.getSerializableExtra("user") as UserDTO
        userDocId = intent.getStringExtra("userDocId")!!
        manager = intent.getSerializableExtra("manager") as ManagerDTO
        checkTouchEventTimer?.start()
        Glide
            .with(this)
            .load(manager.profileImg)
            .circleCrop()
            .into(binding.imagePro)

        Glide.with(this)
            .load(user.profileImg)
            .circleCrop()
            .into(binding.imageMember)

//        val userList = intent.getStringArrayListExtra("userList") as ArrayList<*>
        val lessonCount = user.lessonMembership - user.lessonMembershipUsed
        val lessonCountString = if(lessonCount < 10) "0$lessonCount" else lessonCount.toString()
        val lessonPeriod = (user.lessonMembershipEnd - user.lessonMembershipStart) / (24 * 60 * 60 * 1000)
        val lessonPeriodString = if(lessonPeriod < 10) "0$lessonPeriod" else lessonPeriod.toString()
        val lessonFinishDate = user.lessonMembershipEnd.convertTimestampToSimpleDate()

        binding.textValueRemain.text = "${lessonCountString}회"
        binding.textValuePeriod.text = "${lessonPeriodString}일"
        binding.textUserName.text = "반갑습니다. ${user.name} 회원님"

        binding.textMemberName.text = "${user.name} 회원님"
        binding.textValueFinishDate.text = lessonFinishDate
        binding.textProName.text = "${manager.name} 프로님"
        binding.textToday.text = calendar.timeInMillis.convertTimestampToSimpleDate()
        binding.textProWorkingFinish

        today = calendar.get(Calendar.DAY_OF_WEEK)
        Log.d("schedule", "before today : $today")
        today = if(today - 2 >= 0 ) today - 2 else 6
        Log.d("schedule", "today : $today")
        firestore
            .collection("manager_schedule")
            .document(manager.uid!!)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot == null){
                    binding.textProWorkingStart.text = ""
                    binding.textProWorkingFinish.text = ""
                }
                val schedule = querySnapshot.toObject(ManagerScheduleDTO::class.java)!!
                Log.d("schedule", schedule.schedule[today].toString())
                binding.textProWorkingStart.text = "${schedule.schedule[today].workingStart / (60*60*1000)}시 ${(schedule.schedule[today].workingStart % (60*60*1000)/(60 * 1000))}분"
                binding.textProWorkingFinish.text = "${schedule.schedule[today].workingFinish / (60*60*1000)}시 ${(schedule.schedule[today].workingFinish % (60*60*1000))/(60* 1000)}분"

                binding.recyclerScheduleList.apply {
                    layoutManager = LinearLayoutManager(this@ReserveActivity, LinearLayoutManager.VERTICAL, false)
                    adapter = ScheduleRecyclerViewAdapter(manager.uid!!, schedule)
                    setOnTouchListener { _, event ->
                        when(event.action){
                            MotionEvent.ACTION_DOWN -> {
                                Log.d("timer", "keyDown")
                                stopTimer()
                                return@setOnTouchListener false
                            }
                            MotionEvent.ACTION_UP ->{
                                Log.d("timer", "keyUP")
                                restartTimer()
                                return@setOnTouchListener  false
                            }
                            else -> return@setOnTouchListener false
                        }
                    }
                    addOnScrollListener(object : RecyclerView.OnScrollListener(){
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            Log.d("timer", "scrolled")
                            if(!isFirst){
                                stopTimer()
                            }
                            else isFirst = false

                        }

                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            Log.d("timer", "stateChange")
                            restartTimer()
                        }
                    })
                }
            }




//        checkTouchEventTimer?.start()


//        binding.root.setOnTouchListener { v, event ->
//            when(event.action){
//                MotionEvent.ACTION_DOWN -> {
//                    Log.d("timer", "keyDown")
//                    stopTimer()
//                    return@setOnTouchListener true
//                }
//                MotionEvent.ACTION_UP ->{
//                    Log.d("timer", "keyUP")
//                    restartTimer()
//
//                    return@setOnTouchListener  false
//                }
//                else -> return@setOnTouchListener false
//            }
//        }


    }

    @SuppressLint("NotifyDataSetChanged")
    inner class ScheduleRecyclerViewAdapter(managerUid : String, private val schedule : ManagerScheduleDTO) : RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleItemViewHolder>(){
        private val calendar = Calendar.getInstance()
        private val startHour = 6
        private val oneDay = 24 * 60 * 60 *1000L
        private val firestore = Firebase.firestore
        private val lessonList : ArrayList<LessonDTO> = arrayListOf()
        private val lessonIdList : ArrayList<String> = arrayListOf()
        private var day = 0L

        init {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, date, 0, 0, 0)
            day = (calendar.timeInMillis / 1000) * 1000
            Log.d("reserveActivity", "$year / $month / $date")

            firestore
                .collection("lesson")
                .whereEqualTo("coachUid", managerUid)
                .whereGreaterThanOrEqualTo("lessonDateTime", day)
                .whereLessThan("lessonDateTime", day + oneDay)
                .addSnapshotListener { querySnapshot, error ->
                    if(error != null || querySnapshot == null) {
                        Log.e("reserveActivity", error!!.message.toString())
                        return@addSnapshotListener
                    }
                    lessonList.clear()
                    lessonIdList.clear()
                    querySnapshot.forEach { snapshot ->
                        lessonList.add(snapshot.toObject(LessonDTO::class.java))
                        lessonIdList.add(snapshot.id)
                    }
                    Log.d("reserveActivity", lessonList.toString())
                    notifyDataSetChanged()
                }
        }


        inner class ScheduleItemViewHolder(val adapterBinding: ItemDailyScheduleBinding) : RecyclerView.ViewHolder(adapterBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleItemViewHolder {
            val item = ItemDailyScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ScheduleItemViewHolder(item)

        }

        @SuppressLint("SetTextI18n", "ResourceAsColor")
        override fun onBindViewHolder(holder: ScheduleItemViewHolder, position: Int) {
            val viewBinding = holder.adapterBinding
            val currentHour = if(startHour + (position / 4 ) < 10) "0${startHour + (position / 4 )}" else startHour + (position / 4 )
            val minute = if(position % 4 == 0) "00" else (position % 4) * 15
            val currentMilli =  ((startHour + (position / 4 )) * 60 * 60 * 1000L) + ((position % 4) * 15) * 60 * 1000L
            val rowTime = day + currentMilli
            val todaySchedule = schedule.schedule[today]
//            Log.d("currentMilli", currentMilli.toString())
//            Log.d("currentMilli", "time : "  +  (day + currentMilli).toString())
            viewBinding.textTime.text = "${currentHour}:${minute}"

            var selectedLesson : LessonDTO? = null
            var lessonCount = 0
//            var lessonId : String? = null
//            Log.d("recycler", "Current : ${currentMilli}, workingStart : ${schedule.schedule[today].restStart}, workingFinish :  ${schedule.schedule[today].workingFinish}")
            //
            if(manager.closeToday in day until (day + oneDay)){
//                Log.d("schedule", "onBindViewHolder: 진입1")
                viewBinding.textLessonName.apply{
                    isClickable = false
                    text = "예약 불가"
                    typeface = Typeface.DEFAULT
                    setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                    background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.disable_schedule)
                }
            }
            // New Code
            else{ // 오늘 레슨이 있음
//                Log.d("schedule", "onBindViewHolder: 진입2")
                for((i, lesson) in lessonList.withIndex()){
                    if(rowTime in lesson.lessonDateTime!! until  (lesson.lessonDateTime!! + lesson.lessontime!!)){
                        selectedLesson = lesson
//                        lessonId = lessonIdList[i]  //이건 불 필요 할지도?
                        break

                    }
                }
                if(selectedLesson == null){
                    if(currentMilli in todaySchedule.restStart .. todaySchedule.restFinish && (todaySchedule.restStart * todaySchedule.restFinish) != 0L) setDisabledReserveBtn(viewBinding.textLessonName)
                    else if(currentMilli < schedule.schedule[today].workingStart || currentMilli > schedule.schedule[today].workingFinish) setDisabledReserveBtn(viewBinding.textLessonName)
                    else setAbleReserveBtn(viewBinding.textLessonName, rowTime)
                }
                else{ // 오늘 레슨 있음
                    if(rowTime in selectedLesson.lessonDateTime!! until (selectedLesson.lessonDateTime!! + selectedLesson.lessontime!!)) setReservedBtn(viewBinding.textLessonName)
                    else setAbleReserveBtn(viewBinding.textLessonName, rowTime)
                }
            }
//            else{
////                Log.d("schedule", "onBindViewHolder: 진입3")
////                Log.d("schedule", "onBindViewHolder: $currentMilli")
////                Log.d("schedule", "onBindViewHolder: ${schedule.schedule[today].workingStart}")
////                Log.d("schedule", "onBindViewHolder: ${schedule.schedule[today].workingFinish}")
////                Log.d("schedule", "onBindViewHolder: ${currentMilli in schedule.schedule[today].restStart until schedule.schedule[today].restFinish}")
////                Log.d("schedule", "onBindViewHolder: ${currentMilli < schedule.schedule[today].workingStart && currentMilli > schedule.schedule[today].workingFinish}")
//                if(currentMilli in todaySchedule.restStart .. todaySchedule.restFinish && (todaySchedule.restStart * todaySchedule.restFinish) != 0L) setDisabledReserveBtn(viewBinding.textLessonName)
//                else if(currentMilli < schedule.schedule[today].workingStart || currentMilli > schedule.schedule[today].workingFinish) setDisabledReserveBtn(viewBinding.textLessonName)
//                else setAbleReserveBtn(viewBinding.textLessonName, rowTime)
//
//            }
            // New Code End
            /* Legacy
            else if(!lessonList.isNullOrEmpty()){
                for((i, lesson) in lessonList.withIndex()){
                    if(rowTime in lesson.lessonDateTime!! until  (lesson.lessonDateTime!! + lesson.lessontime!!)){
                        selectedLesson = lesson
                        lessonId = lessonIdList[i]  //이건 불 필요 할지도?
                        break
                    }
                }

                // 해당 시간에 레슨 없음
                if(selectedLesson == null){
                    if(currentMilli in schedule.schedule[today].restStart until schedule.schedule[today].restFinish){
                        viewBinding.textLessonName.apply{
                            isClickable = false
                            text = "휴식 시간"
                            typeface = Typeface.DEFAULT
                            setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                            background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.disable_schedule)
                        }
                    }
                    else if(currentMilli in schedule.schedule[today].restStart until schedule.schedule[today].restFinish
                        || (currentMilli <= schedule.schedule[today].workingStart && currentMilli > schedule.schedule[today].workingFinish)) {
                        viewBinding.textLessonName.apply {
                            isClickable = false
                            text = "예약 불가"
                            typeface = Typeface.DEFAULT
                            setTextColor(
                                ContextCompat.getColor(
                                    this@ReserveActivity,
                                    R.color.white
                                )
                            )
                            background = ContextCompat.getDrawable(
                                this@ReserveActivity,
                                R.drawable.disable_schedule
                            )
                        }
                    }
                    else{
                        viewBinding.textLessonName.apply{
                            Log.d("RecyclerView", "onBindViewHolder: 1")
                            isClickable = true
                            text = "예약 가능"
                            typeface = Typeface.DEFAULT
                            setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.black))
                            background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.text_schedule)
                            setOnClickListener {
                                val intent = Intent(this@ReserveActivity, AddScheduleActivity::class.java)
                                intent.putExtra("manager", manager)
                                intent.putExtra("user", user)
                                intent.putExtra("userDocId", userDocId)
                                intent.putExtra("date", rowTime)
                                intent.putExtra("type", 2)
                                reserveResult.launch(intent)
                                checkTouchEventTimer?.cancel()
                            }
                        }
                    }
                }
                // 해당 시간에 레슨 있음
               else if(currentMilli in schedule.schedule[today].restStart until schedule.schedule[today].restFinish
                    || (currentMilli <= schedule.schedule[today].workingStart && currentMilli > schedule.schedule[today].workingFinish
                    )){
                    viewBinding.textLessonName.apply{
                        isClickable = false
                        text = "예약 불가"
                        typeface = Typeface.DEFAULT
                        setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                        background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.disable_schedule)
                    }
                }
                else{
                    viewBinding.textLessonName.apply{
                        isClickable = false
                        text = "예약 완료"
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                        setBackgroundColor(ContextCompat.getColor(this@ReserveActivity, R.color.reserve_lesson))
                    }
                }
            }

            // 오늘 Lesson 없을 경우
            else{
                if(currentMilli in schedule.schedule[today].restStart until schedule.schedule[today].restFinish){
                    viewBinding.textLessonName.apply{
                        isClickable = false
                        text = "휴식 시간"
                        typeface = Typeface.DEFAULT
                        setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                        background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.disable_schedule)
                    }
                }
                else if((currentMilli <= schedule.schedule[today].workingStart || currentMilli > schedule.schedule[today].workingFinish )){
                    viewBinding.textLessonName.apply{
                        isClickable = false
                        text = "예약 불가"
                        typeface = Typeface.DEFAULT
                        setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                        background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.disable_schedule)
                    }
                }

                else{
                    viewBinding.textLessonName.apply {
                        Log.d("RecyclerView", "onBindViewHolder: 2")
                        text = "예약 가능"
                        typeface = Typeface.DEFAULT
                        setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.black))
                        background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.text_schedule)

                        setOnClickListener {
                            val intent = Intent(this@ReserveActivity, AddScheduleActivity::class.java)
                            intent.putExtra("manager", manager)
                            intent.putExtra("user", user)
                            intent.putExtra("userDocId", userDocId)
                            intent.putExtra("date", rowTime)
                            intent.putExtra("type", 2)
                            reserveResult.launch(intent)
                            checkTouchEventTimer?.cancel()

                        }
                    }
                }
            }
    */
        }

        override fun getItemCount(): Int = 18 * 4 + 1

        private fun openReserveActivity(rowTime : Long){
            val intent = Intent(this@ReserveActivity, AddScheduleActivity::class.java)
            intent.putExtra("manager", manager)
            intent.putExtra("user", user)
            intent.putExtra("userDocId", userDocId)
            intent.putExtra("date", rowTime)
            intent.putExtra("type", 2)
            reserveResult.launch(intent)
            checkTouchEventTimer?.cancel()
        }

        private fun setDisabledReserveBtn(button : TextView){
            button.apply {
                isClickable = false
                text = "예약 불가"
                typeface = Typeface.DEFAULT
                setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                background = ContextCompat.getDrawable(this@ReserveActivity, R.drawable.disable_schedule)
            }
        }

        private fun setAbleReserveBtn(button : TextView, rowTime : Long) {
            button.apply {
                isClickable = true
                text = "예약 가능"
                typeface = Typeface.DEFAULT
                setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.black))
                background =
                    ContextCompat.getDrawable(this@ReserveActivity, R.drawable.text_schedule)
                setOnClickListener {
                    openReserveActivity(rowTime)
                }
            }
        }
        private fun setReservedBtn(button : TextView){
            button.apply {
                isClickable = false
                text = "예약 마감"
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(this@ReserveActivity, R.color.white))
                setBackgroundColor(ContextCompat.getColor(this@ReserveActivity, R.color.reserve_lesson))
            }
        }
    } // End RecyclerView

    override fun onResume() {
        super.onResume()
        restartTimer()
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    @SuppressLint("SetTextI18n")
    private fun restartTimer(){
        if(checkTouchEventTimer == null){
            checkTouchEventTimer = CoroutineScope(Dispatchers.Main).launch {
                val displayTime = (BACK_TO_HOME_DELAY/1000).toInt()
                repeat(displayTime){
                    binding.textReserve.text = ("예약 시스템 (${displayTime - it})")
                    delay(1000L)
                }
                delay(BACK_TO_HOME_DELAY)
                finish()
            }
        }
    }
    private fun stopTimer(){
        checkTouchEventTimer?.cancel()
        checkTouchEventTimer = null
    }


}