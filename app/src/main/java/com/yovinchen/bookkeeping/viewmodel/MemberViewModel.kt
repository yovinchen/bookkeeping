package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.Member
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MemberViewModel(application: Application) : AndroidViewModel(application) {
    private val memberDao = BookkeepingDatabase.getDatabase(application).memberDao()
    
    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()

    fun addMember(name: String, description: String = "") {
        viewModelScope.launch {
            val member = Member(name = name, description = description)
            memberDao.insertMember(member)
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            memberDao.updateMember(member)
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            memberDao.deleteMember(member)
        }
    }

    suspend fun getMemberCount(): Int {
        return memberDao.getMemberCount()
    }
}
