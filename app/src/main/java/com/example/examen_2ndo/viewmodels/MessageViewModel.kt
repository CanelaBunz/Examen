package com.example.examen_2ndo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.examen_2ndo.persistence.AppDatabase
import com.example.examen_2ndo.persistence.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val messageDao = database.messageDao()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _messages.value = withContext(Dispatchers.IO) {
                messageDao.getAllMessages()
            }
        }
    }

    fun addMessage(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            messageDao.insert(Message(content = content))
            loadMessages()
        }
    }

    fun deleteAllMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            messageDao.deleteAll()
            withContext(Dispatchers.Main) {
                _messages.value = emptyList()
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
                return MessageViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}