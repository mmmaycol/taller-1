package com.example.handgestureapp

import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val contactDao: ContactDao
) {
    fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()

    suspend fun getContactByGesture(gestureId: Int): Contact? = contactDao.getContactByGesture(gestureId)

    suspend fun saveContact(contact: Contact) {
        contactDao.insert(contact)
    }

    suspend fun deleteContact(contact: Contact) {
        contactDao.delete(contact)
    }
}
