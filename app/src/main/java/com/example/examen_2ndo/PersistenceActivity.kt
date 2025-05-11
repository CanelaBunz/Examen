package com.example.examen_2ndo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.examen_2ndo.databinding.ActivityPersistenceBinding
import com.example.examen_2ndo.viewmodels.MessageViewModel

class PersistenceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersistenceBinding
    private val viewModel: MessageViewModel by viewModels {
        MessageViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersistenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.btnAdd.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.addMessage(message)
                binding.etMessage.text.clear()
            }
        }

        binding.btnClear.setOnClickListener {
            viewModel.deleteAllMessages()
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(this, Observer { messages ->
            val messagesText = messages.joinToString("\n\n") { msg ->
                "${msg.content}\n${java.util.Date(msg.timestamp)}"
            }
            binding.tvMessages.text = messagesText
        })
    }
}