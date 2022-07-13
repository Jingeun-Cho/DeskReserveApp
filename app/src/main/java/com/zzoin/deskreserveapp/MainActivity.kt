package com.zzoin.deskreserveapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.zzoin.common.AppConfig
import com.zzoin.controller.LessonController
import com.zzoin.controller.ManagerController
import com.zzoin.controller.MemberController
import com.zzoin.deskreserveapp.databinding.ActivityMainBinding
import com.zzoin.model.ManagerDTO
import com.zzoin.model.UserDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val memberController : MemberController = AppConfig.memberController
    private val managerController : ManagerController = AppConfig.managerController
    private val lessonController : LessonController = AppConfig.lessonController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.inputPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        binding.btn1.setOnClickListener {
            appendPhoneNumber(1)
        }
        binding.btn2.setOnClickListener {
            appendPhoneNumber(2)
        }
        binding.btn3.setOnClickListener {
            appendPhoneNumber(3)
        }
        binding.btn4.setOnClickListener {
            appendPhoneNumber(4)
        }
        binding.btn5.setOnClickListener {
            appendPhoneNumber(5)
        }
        binding.btn6.setOnClickListener {
            appendPhoneNumber(6)
        }
        binding.btn7.setOnClickListener {
            appendPhoneNumber(7)
        }
        binding.btn8.setOnClickListener {
            appendPhoneNumber(8)
        }
        binding.btn9.setOnClickListener {
            appendPhoneNumber(9)
        }
        binding.btn0.setOnClickListener {
            appendPhoneNumber(0)
        }
        binding.btnDel.setOnClickListener {
            val currentPhone = binding.inputPhone.text.toString()

            if(currentPhone.isNotBlank()){
                if(currentPhone.length == 5 || currentPhone.length == 10){
                    binding.inputPhone.text.delete(currentPhone.length - 2, currentPhone.length)
                    binding.inputPhone.setSelection(binding.inputPhone.text.length)
                }

                else{
                    binding.inputPhone.text.delete(currentPhone.length - 1, currentPhone.length)
                    binding.inputPhone.setSelection(binding.inputPhone.text.length)
                }
            }
        }
        binding.btnClear.setOnClickListener {
            binding.inputPhone.setText("")
        }

        binding.btnReserve.setOnClickListener {
            val phoneNumber = binding.inputPhone.text.toString()
            CoroutineScope(Dispatchers.Main).launch {
                val (user, userDocId) = memberController.getMemberByPhone(phoneNumber).await()
                val manager : ManagerDTO?
                val managerUserList : ArrayList<UserDTO>
                if(user != null){
                    val checkLesson = lessonController.countTodayLesson(user.uid!!).await()
                    if(checkLesson == 0){
                        manager = managerController.getManagerByUid(user.proUid).await()
                        managerUserList = memberController.getMemberByProUid(user.proUid).await()//getManageUserList(firestore, user.proUid)
                        val intent = Intent(this@MainActivity, ReserveActivity::class.java)
                        intent.putExtra("user", user)
                        intent.putExtra("userDocId", userDocId)
                        intent.putExtra("manager", manager)
                        intent.putExtra("userList", managerUserList)
                        startActivity(intent)
                        binding.inputPhone.setText("")
                    }
                    else{
                        Snackbar.make(binding.root, "오늘 레슨을 진행 한 회원입니다.", Snackbar.LENGTH_SHORT).show()
                    }

                }
                else{
                    Snackbar.make(binding.root, "존재하지 않는 회원입니다.", Snackbar.LENGTH_SHORT).show()
                }
            }
            //if
        }
    }

    private fun appendPhoneNumber(value : Int){
        if(binding.inputPhone.text.length == 3 || binding.inputPhone.text.length == 8)
            binding.inputPhone.text.append('-')
        binding.inputPhone.text.append(value.toString())
    }

}