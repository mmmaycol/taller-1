package com.example.handgestureapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * FROM contacts WHERE gestureId = :gestureId LIMIT 1")
    suspend fun getContactByGesture(gestureId: Int): Contact?

    @Query("SELECT * FROM contacts ORDER BY gestureId ASC")
    fun getAllContacts(): Flow<List<Contact>>
}
