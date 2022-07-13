package com.zzoin.deskreserveapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.zzoin.common.AppConfig.convertTimestampToDate
import com.zzoin.common.AppConfig.convertTimestampToSimpleDate
import com.zzoin.deskreserveapp.databinding.ActivityAddScheduleBinding
import com.zzoin.deskreserveapp.databinding.DialogAddScheduleBinding
import com.zzoin.deskreserveapp.databinding.DialogSelectMemberBinding
import com.zzoin.deskreserveapp.databinding.ItemMemberReserveBinding
import com.zzoin.model.LessonDTO
import com.zzoin.model.ManagerDTO
import com.zzoin.model.UserDTO
import kotlinx.coroutines.*

import java.util.*
import kotlin.collections.ArrayList

class AddScheduleActivity : Activity(){
    private lateinit var  binding : ActivityAddScheduleBinding
    private lateinit var confirmDialog : Dialog
    private lateinit var memberDialog : Dialog
    private lateinit var lessonTimeDialog : Dialog
    private lateinit var manager : ManagerDTO
    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var selectedMember : UserDTO? = null
    private var selectedMemberDocId : String = ""
    private var selectedDate : Long = 0
    private var selectTime : Long = 0
    private var startDateTime : Long = 0
    private val calendar = Calendar.getInstance(Locale("ko", "kr"))
    private var type : String? = ""
    private var lessonDiff = 0
    private lateinit var user : UserDTO
    private var writeType = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        writeType = intent?.getIntExtra("type", 0)!!
        selectedDate = intent?.getLongExtra("date", 0)!!

        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            finish()
        }
        initView()
        manager = intent?.getSerializableExtra("manager") as ManagerDTO
        if(writeType != 1)
            binding.textViewAddScheduleDate.text = selectedDate.convertTimestampToSimpleDate()
        else
            binding.textViewAddScheduleDate.text = selectedDate.convertTimestampToDate()
        //Choose Member Legacy
//        binding.btnSelectMember.setOnClickListener {
//            showSelectMemberDialog()
//        }
        binding.textTitle.text =  "메모 사항"

        //Choose Start time
        startDateTime = intent?.getLongExtra("date", 0)!! / 1000 * 1000
        Log.d("calendar", startDateTime.toString())



        binding.textTitle.text = "레슨 메모"
//                binding.btnSelectStartTime.visibility = View.GONE
//                binding.btnSelectStartTime.setOnClickListener {
//                    showSelectTime()
//                }

        selectedMember = intent?.getSerializableExtra("user") as UserDTO
        user = selectedMember!!
//                binding.btnSelectMember.visibility = View.GONE
        selectedMemberDocId = intent?.getStringExtra("userDocId")!!

        val period = if (user.lessonMembershipStart != 0L && user.lessonMembershipEnd != 0L){
            val startDate = user.lessonMembershipStart.convertTimestampToSimpleDate()
            val endDate = user.lessonMembershipEnd.convertTimestampToSimpleDate()
            "$startDate ~ $endDate"
        }
        else "진행중인 레슨이 없습니다."

//                binding.btnSelectMember.text = "${user.name}님"
        memberDialog.findViewById<RecyclerView>(R.id.select_member_listView).adapter = null
        memberDialog.dismiss()
        binding.memberInfoLayout.memberLessonNameTextView.text = "${user.name} 회원님의 정보"
        binding.memberInfoLayout.textMembershipPeriod.text = period
        binding.memberInfoLayout.proName.text = manager.name
        binding.memberInfoLayout.lessonTotal.text = "${user.lessonMembership}회"
        binding.memberInfoLayout.lessonRemain.text = "${user.lessonMembership - user.lessonMembershipUsed}회"
        binding.memberInfoLayout.lessonUsed.text = "${user.lessonMembershipUsed}회"
        lessonDiff = (user.lessonMembership - user.lessonMembershipUsed).toInt()

        //Legacy
        /*
        when(writeType){
            0 -> {
//                binding.btnSelectStartTime.setOnClickListener {
//                    showSelectTime()
//                }
            }
            1 -> {
                startDateTime = selectedDate
                binding.btnSelectStartTime.visibility = View.GONE
                binding.textTitle.text = "레슨 메모"
            }
            2 -> {
                binding.textTitle.text = "레슨 메모"
//                binding.btnSelectStartTime.visibility = View.GONE
//                binding.btnSelectStartTime.setOnClickListener {
//                    showSelectTime()
//                }

                selectedMember = intent?.getSerializableExtra("user") as UserDTO
                user = selectedMember!!
//                binding.btnSelectMember.visibility = View.GONE
                selectedMemberDocId = intent?.getStringExtra("userDocId")!!

                val period = if (user.lessonMembershipStart != 0L && user.lessonMembershipEnd != 0L){
                    val startDate = user.lessonMembershipStart.convertTimestampToSimpleDate()
                    val endDate = user.lessonMembershipEnd.convertTimestampToSimpleDate()
                    "$startDate ~ $endDate"
                }
                else "진행중인 레슨이 없습니다."

//                binding.btnSelectMember.text = "${user.name}님"
                memberDialog.findViewById<RecyclerView>(R.id.select_member_listView).adapter = null
                memberDialog.dismiss()
                binding.memberInfoLayout.memberLessonNameTextView.text = "${user.name} 회원님의 정보"
                binding.memberInfoLayout.textMembershipPeriod.text = period
                binding.memberInfoLayout.proName.text = manager.name
                binding.memberInfoLayout.lessonTotal.text = "${user.lessonMembership}회"
                binding.memberInfoLayout.lessonRemain.text = "${user.lessonMembership - user.lessonMembershipUsed}회"
                binding.memberInfoLayout.lessonUsed.text = "${user.lessonMembershipUsed}회"
                lessonDiff = (user.lessonMembership - user.lessonMembershipUsed).toInt()

            }
        }
        */

        //Choose Lesson time
            binding.btnSelectLessonTime.setOnClickListener {
                showLessonTime()
            }

//        ArrayAdapter.createFromResource(this, R.array.lesson_coupon_type, android.R.layout.simple_spinner_item).apply{
//            setDropDownViewResource(R.layout.drop_down_item)
//            binding.addScheduleUseType.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
//            binding.addScheduleUseType.adapter = this
//        }
//
//        binding.addScheduleUseType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                when (position){
//                    0 -> type = "레슨"
//                    1 -> type = "서비스"
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                parent!!.setSelection(0)
//            }
//        }
        // Commit Schedule
        binding.btnAddSchedule.setOnClickListener {
            Log.d("lesson", "$lessonDiff / ${selectTime / (15 * 60* 1000)}")
            if (type=="레슨" && lessonDiff <= (selectTime / (15 * 60* 1000)).toInt()) {
                Toast.makeText(this, "잔여 레슨이 없습니다.", Toast.LENGTH_SHORT).show()
            }
            else if( selectedMember!!.uid == "") Toast.makeText(this, "회원님께서 앱에 등록하지 않았습니다.", Toast.LENGTH_SHORT).show()
            else if(selectedMember == null && selectedMemberDocId == "") Toast.makeText(this, "회원님이 선택되지 않았습니다", Toast.LENGTH_SHORT).show()
            else if(selectedDate  == 0L) Toast.makeText(this, "날짜가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            else if(selectedMember != null && selectedDate != 0L && selectTime != 0L && selectedMember!!.uid != ""){
                val lesson = LessonDTO()
                lesson.coachUid = manager.uid
                lesson.uid = selectedMember!!.uid
                lesson.lessonDateTime = startDateTime
                lesson.lessontime = selectTime
                lesson.type = type
                lesson.lessonMemo = binding.lessonMemoEditText.text.toString()
                lesson.lessonNote = ""

                val documentId = "${manager.name}_${System.currentTimeMillis()}"
                firestore
                    .collection("lesson")
                    .document(documentId)
                    .set(lesson)
                    .addOnSuccessListener {
                        Toast.makeText(this, "레슨 일정을 등록 했습니다.", Toast.LENGTH_SHORT).show()
                        addScheduleDialog(selectedMember!!)
                    }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView(){
        confirmDialog = Dialog(this)

        memberDialog = Dialog(this)
        memberDialog.setContentView(R.layout.dialog_select_member)
        memberDialog.window!!.attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        memberDialog.window!!.attributes.height = WindowManager.LayoutParams.MATCH_PARENT

        lessonTimeDialog = Dialog(this)
        lessonTimeDialog.setContentView(R.layout.dialog_selecte_lesson_time)
        selectTime = 15 * 60 * 1000L
        binding.btnSelectLessonTime.text = "15분"
    }

    private fun showSelectMemberDialog(){
        val dialogBinding = DialogSelectMemberBinding.inflate(layoutInflater)
        memberDialog.setContentView(dialogBinding.root)
        memberDialog.show()
        dialogBinding.selectMemberListView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
            adapter = SelectMemberDialogRecyclerViewAdapter(null, manager)
        }
        dialogBinding.btnSearchMember.setOnClickListener {
            val search = memberDialog.findViewById<EditText>(R.id.edit_text_search_member).text.toString()
            memberDialog.findViewById<RecyclerView>(R.id.select_member_listView).apply {
                adapter = SelectMemberDialogRecyclerViewAdapter(search, manager)
            }
            it.visibility = View.GONE
            memberDialog.findViewById<Button>(R.id.btn_search_reset).visibility = View.VISIBLE
        }
        dialogBinding.btnSearchReset.setOnClickListener {
            memberDialog.findViewById<RecyclerView>(R.id.select_member_listView).apply {
                adapter = SelectMemberDialogRecyclerViewAdapter(null, manager)
            }
            memberDialog.findViewById<Button>(R.id.btn_search_member).visibility = View.VISIBLE
            it.visibility = View.GONE
        }
    }

    private fun showSelectTime(){
        val timePicker = TimePickerDialog(this,
            { _, hourOfDay, minute ->
                val time = selectedDate + hourOfDay*3600*1000 + minute* 60 * 1000
//                binding.btnSelectStartTime.text = time.convertTimestampToDate()
                startDateTime = time
                Log.d("date", startDateTime.toString())
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true )

        timePicker.setTitle("레슨 예약 시간")
        timePicker.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showLessonTime(){
        lessonTimeDialog.show()
        lessonTimeDialog.findViewById<RadioGroup>(R.id.select_lesson_time_radioGroup)
            .setOnCheckedChangeListener { _, item ->
                when(item){
                    R.id.minute_15 -> {
                        selectTime = 15 * 60 * 1000L
                        binding.btnSelectLessonTime.text = "15분"
                    }
                    R.id.minute_30 -> {
                        selectTime = 30 * 60 * 1000L
                        binding.btnSelectLessonTime.text = "30분"
                    }
                    R.id.minute_45 -> {
                        selectTime = 45 * 60 * 1000L
                        binding.btnSelectLessonTime.text = "45분"
                    }
                    R.id.minute_60 -> {
                        selectTime = 60 * 60 * 1000L
                        binding.btnSelectLessonTime.text = "60분"
                    }
                }
                lessonTimeDialog.dismiss()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class SelectMemberDialogRecyclerViewAdapter(name : String?, manager : ManagerDTO) : RecyclerView.Adapter<SelectMemberDialogRecyclerViewAdapter.SelectMemberCustomViewHolder>() {
        private val userList : ArrayList<UserDTO> = arrayListOf()
        private val userDocumentId : ArrayList<String> = arrayListOf()
        init {
            if(name == null){
                firestore
                    .collection("user")
                    .whereEqualTo("proUid", manager.uid)
                    .whereNotEqualTo("ableReservation", "false")
                    .addSnapshotListener { querySnapshot, error ->
                        if(querySnapshot == null || error != null ) return@addSnapshotListener
                        userList.clear()
                        userDocumentId.clear()
                        querySnapshot.documents.forEach { snapshot ->
                            val item = snapshot.toObject(UserDTO::class.java)!!
                                userList.add(item)
                                userDocumentId.add(snapshot.id)
                        }
                        notifyDataSetChanged()
                    }
            }
            else{
                firestore
                    .collection("user")
                    .whereEqualTo("name", name)
                    .addSnapshotListener { querySnapshot, error ->
                        if(querySnapshot == null || error != null) return@addSnapshotListener
                        userList.clear()
                        userDocumentId.clear()
                        querySnapshot.documents.forEach { snapshot ->
                            val item = snapshot.toObject(UserDTO::class.java)!!
                            if(item.proUid == manager.uid) {
                                userList.add(item)
                                userDocumentId.add(snapshot.id)
                            }
                        }
                        notifyDataSetChanged()
                    }
            }

        }

        inner class SelectMemberCustomViewHolder(val binding : ItemMemberReserveBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectMemberCustomViewHolder {
            val view = ItemMemberReserveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SelectMemberCustomViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: SelectMemberCustomViewHolder, position: Int) {
            val viewBinding = holder.binding
            val user = userList[position]
            val userDocumentId = userDocumentId[position]

            if((user.profileImg!!.isNotBlank())){
                Glide
                    .with(this@AddScheduleActivity)
                    .load(user.profileImg)
                    .circleCrop()
                    .into(viewBinding.imgMemberProfile)
            }
            viewBinding.memberNameTextView.text = user.name
            viewBinding.memberPhoneTextView.text = user.phone

            val period = if (user.lessonMembershipStart != 0L && user.lessonMembershipEnd != 0L){
                val startDate = user.lessonMembershipStart.convertTimestampToSimpleDate()
                val endDate = user.lessonMembershipEnd.convertTimestampToSimpleDate()
                "$startDate ~ $endDate"
            }
            else "진행중인 레슨이 없습니다."


            viewBinding.btnSelectMember.setOnClickListener {
                selectedMember = user.copy()
                selectedMemberDocId = userDocumentId
//                binding.btnSelectMember.text = "${user.name}님"
                memberDialog.findViewById<RecyclerView>(R.id.select_member_listView).adapter = null
                memberDialog.dismiss()
                binding.memberInfoLayout.memberLessonNameTextView.text = "${user.name} 회원님의 정보"
                binding.memberInfoLayout.textMembershipPeriod.text = period
                binding.memberInfoLayout.proName.text = manager.name
                binding.memberInfoLayout.lessonTotal.text = "${user.lessonMembership}회"
                binding.memberInfoLayout.lessonRemain.text = "${user.lessonMembership - user.lessonMembershipUsed}회"
                binding.memberInfoLayout.lessonUsed.text = "${user.lessonMembershipUsed}회"

                lessonDiff = (user.lessonMembership - user.lessonMembershipUsed).toInt()
            }
        }

        override fun getItemCount(): Int = userList.size


    }
    @SuppressLint("SetTextI18n")
    private fun addScheduleDialog(user : UserDTO){
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        confirmDialog.setContentView(dialogBinding.root)
        confirmDialog.show()
        val closeDialog = CoroutineScope(Dispatchers.Default).launch {
            repeat(5){
                confirmDialog.findViewById<Button>(R.id.btn_confirm).text = "확인 (${5 - it})"
                delay(1000L)
            }
            setResult(RESULT_OK)
            finish()
        }
        confirmDialog.setCancelable(false)
        confirmDialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogBinding.btnConfirm.setOnClickListener {
            confirmDialog.dismiss()
            closeDialog.cancel()
            setResult(RESULT_OK)
            finish()
        }
        dialogBinding.textAddScheduleDate.text = selectedDate.convertTimestampToSimpleDate()
        dialogBinding.memberInfoLayout.proName.text = user.pro
        dialogBinding.memberInfoLayout.textMembershipPeriod.text = "${(user.lessonMembershipEnd - user.lessonMembershipStart)/(1000*60*60*24)}일"
        dialogBinding.memberInfoLayout.lessonTotal.text = "${user.lessonMembership} 회"
        dialogBinding.memberInfoLayout.lessonUsed.text = "${user.lessonMembershipUsed}회"
        dialogBinding.memberInfoLayout.lessonRemain.text = "${user.lessonMembership - user.lessonMembershipUsed}회"
        dialogBinding.notificationTextView.text = "${user.name}회원님, ${startDateTime.convertTimestampToDate()}에 ${selectTime/(60*1000)}분간 레슨 일정이 등록되었습니다."
    }


    override fun onPause() {
        super.onPause()
        Log.d("fragment", "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("fragment", "onResume")
    }
}