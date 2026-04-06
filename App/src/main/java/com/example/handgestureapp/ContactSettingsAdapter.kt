package com.example.handgestureapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.handgestureapp.databinding.ItemContactSettingBinding

class ContactSettingsAdapter(
    private val onSaveClicked: (Contact) -> Unit
) : ListAdapter<Contact, ContactSettingsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactSettingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemContactSettingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.tvGestureLabel.text = "Gesto ${contact.gestureId}"
            binding.editName.setText(contact.name)
            binding.editPhone.setText(contact.phoneNumber)
            binding.editMessage.setText(contact.message.ifEmpty { "Hola :)" })

            binding.btnSave.setOnClickListener {
                val updatedContact = contact.copy(
                    name = binding.editName.text?.toString().orEmpty(),
                    phoneNumber = binding.editPhone.text?.toString().orEmpty(),
                    message = binding.editMessage.text?.toString().ifNullOrBlank("Hola :)")
                )
                onSaveClicked(updatedContact)
            }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.gestureId == newItem.gestureId
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }

    private fun CharSequence?.ifNullOrBlank(defaultValue: String): String {
        return if (this.isNullOrBlank()) defaultValue else this.toString()
    }
}
