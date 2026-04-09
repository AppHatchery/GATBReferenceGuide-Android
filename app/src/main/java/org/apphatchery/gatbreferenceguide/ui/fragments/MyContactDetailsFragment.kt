// Fragment for viewing and editing a user's private (personal) TB contact entry.
// The user sees all contact fields pre-populated from their saved PrivateContact and can
// update the record or permanently delete it. Both actions return to ContactFragment.
//
// Data flow: receives a PrivateContact via SafeArgs (MyContactDetailsFragmentArgs.contact).
// "Update" validates that name and cell are non-empty, then calls viewModel.update(PrivateContact).
// "Delete" shows an AlertDialog confirmation before calling viewModel.deleteContact(contact).
// After either action, navigates back via action_myContactDetailsFragment_to_contactFragment.
//
// Related: FAContactViewModel, ContactFragment, ContactDetailsFragment, PrivateContact.
package org.apphatchery.gatbreferenceguide.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.*
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.db.entities.PrivateContact
import org.apphatchery.gatbreferenceguide.ui.adapters.FAChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAChapterViewModel
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAContactViewModel


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MyContactDetailsFragment : Fragment(R.layout.fragment_my_contact_details){
    private lateinit var binding: FragmentMyContactDetailsBinding
    private val viewModel: FAContactViewModel by viewModels()
    private val args: MyContactDetailsFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMyContactDetailsBinding.bind(view)
        val contact = args.contact!!

        binding.apply {
            fullNameTextView.setText(contact.fullName)
            additionalInfoTextView.setText(contact.additionalInfo)
            cellTextView.setText(contact.contactCell)
            emailTextView.setText(contact.contactEmail)
            additionalInfoTextView.setText(contact.contactAddress)
            officePhoneTextView.setText(contact.officePhone)
            officeFaxTextView.setText(contact.officeFax)
            notesTextView.setText(contact.personalNote)
        }

        binding.updateButton.setOnClickListener {
            val name = binding.fullNameTextView.text.toString().trim()
            val additionalInfo = binding.additionalInfoTextView.text.toString().trim()
            val cell = binding.cellTextView.text.toString().trim()
            val email = binding.emailTextView.text.toString().trim()
            val address = binding.addressTextView.text.toString().trim()
            val phone = binding.officePhoneTextView.text.toString().trim()
            val fax = binding.officeFaxTextView.text.toString().trim()
            val note = binding.notesTextView.text.toString().trim()

            if (name.isNotEmpty() && cell.isNotEmpty()) {
                val privateContact = PrivateContact(contact.id, name, additionalInfo,cell, email, address, phone, fax,note)
                viewModel.update(privateContact)
                Snackbar.make(
                    view, "Contact updated successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
                view.findNavController().navigate(R.id.action_myContactDetailsFragment_to_contactFragment)

            } else {
                Toast.makeText(context, "Please enter contact name and cellphone number", Toast.LENGTH_SHORT).show()
            }



        }

        binding.deleteButton.setOnClickListener {
            deleteContact(contact)
        }

    }

    /**
     * Shows a confirmation AlertDialog before permanently removing the given [contact]
     * from the local database. On confirmation, calls viewModel.deleteContact() and
     * navigates back to ContactFragment.
     */
    private fun deleteContact(contact:PrivateContact) {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Contact")
            setMessage("Are you sure you want to permanently delete this contact?")
            setPositiveButton("DELETE") { _, _ ->
                viewModel.deleteContact(contact)
                view?.findNavController()?.navigate(
                    R.id.action_myContactDetailsFragment_to_contactFragment
                )
            }
            setNegativeButton("CANCEL", null)
        }.create().show()

    }





}