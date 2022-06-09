package org.apphatchery.gatbreferenceguide.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactBinding
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactDetailsBinding
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewBinding
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.db.entities.PrivateContact
import org.apphatchery.gatbreferenceguide.ui.adapters.FAChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAChapterViewModel
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAContactViewModel


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ContactDetailsFragment : Fragment(R.layout.fragment_contact_details){

    private lateinit var binding: FragmentContactDetailsBinding
    private val viewModel: FAContactViewModel by viewModels()
    private val args: ContactDetailsFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentContactDetailsBinding.bind(view)
        val contact = args.contact!!

        binding.apply {
            fullNameTextView.text = contact.fullName
            additionalInfoTextView.text = contact.additionalInfo
            cellTextView.text = contact.contactCell
            emailTextView.text = contact.contactEmail
            additionalInfoTextView.text = contact.contactAddress
            officePhoneTextView.text = contact.officePhone
            officeFaxTextView.text = contact.officeFax
            notesTextView.text = contact.personalNote
        }


        binding.CopyToMyContactButton.setOnClickListener {
            viewModel.copyPublicToPrivateContact(PrivateContact(0,contact.fullName, contact.additionalInfo,
                contact.contactCell, contact.contactEmail, contact.contactAddress, contact.officePhone, contact.officeFax, contact.personalNote))
            Snackbar.make(
                view, "Contact copied successfully",
                Snackbar.LENGTH_SHORT
            ).show()
            view.findNavController().navigate(R.id.action_contactDetailsFragment_to_contactFragment)
        }


    }

}