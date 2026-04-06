package com.example.handgestureapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository = ContactRepository(
        AppDatabase.getInstance(application).contactDao()
    )

    val contacts: LiveData<List<Contact>> = repository.getAllContacts().asLiveData()

    fun saveContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveContact(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteContact(contact)
        }
    }
}
