package com.example.handgestureapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handgestureapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var adapter: ContactSettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configurar contactos"

        adapter = ContactSettingsAdapter { contact ->
            viewModel.saveContact(contact)
        }

        binding.recyclerContacts.layoutManager = LinearLayoutManager(this)
        binding.recyclerContacts.adapter = adapter

        viewModel.contacts.observe(this) { contacts ->
            val items = (0..5).map { gestureId ->
                contacts.find { it.gestureId == gestureId }
                    ?: Contact(
                        gestureId = gestureId,
                        name = "",
                        phoneNumber = "",
                        message = "Hola :)"
                    )
            }
            adapter.submitList(items)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
