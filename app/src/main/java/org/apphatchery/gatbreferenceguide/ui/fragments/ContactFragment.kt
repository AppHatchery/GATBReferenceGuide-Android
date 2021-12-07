package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactBinding
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAContactViewModel

@AndroidEntryPoint
class ContactFragment : BaseFragment(R.layout.fragment_contact) {

    private lateinit var faContactAdapter: FAContactAdapter
    private val viewModel by viewModels<FAContactViewModel>()

    private lateinit var binding: FragmentContactBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentContactBinding.bind(view)
        faContactAdapter = FAContactAdapter()

        viewModel.getContacts.observe(viewLifecycleOwner){
            faContactAdapter.submitList(it)
        }

        binding.recyclerView.adapter = faContactAdapter

        binding.addContactButton.setOnClickListener {
            findNavController().navigate(ContactFragmentDirections.actionContactFragmentToContactAddFragment())
        }
    }

    companion object {
        val fakeContact = listOf(
            Contact(firstName = "Atlanta Grady Hospital IP Department"),
            Contact(firstName = "Atlanta Penitentiary Fulton"),
            Contact(firstName = "Atlanta Health Department Office"),
            Contact(firstName = "Cherokee Health Department Office"),
            Contact(firstName = "Cherokee Children’s Hospital"),
            Contact(firstName = "Cherokee Piedmont Hospital"),
            Contact(firstName = "DeKalb Health Department Office"),
            Contact(firstName = "DeKalb Children’s Hospital"),
            Contact(firstName = "DeKalb Piedmont Hospital"),
        )
    }


}