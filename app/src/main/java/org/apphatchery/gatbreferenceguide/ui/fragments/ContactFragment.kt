package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactBinding
import org.apphatchery.gatbreferenceguide.db.data.ViewPagerData
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactViewPagerAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAPrivateContactAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FASavedViewPagerAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAContactViewModel
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASavedViewModel

@AndroidEntryPoint
class ContactFragment : BaseFragment(R.layout.fragment_contact) {

    private lateinit var faContactAdapter: FAContactAdapter
    private lateinit var faPrivateContactAdapter: FAPrivateContactAdapter
    private val viewModel by viewModels<FAContactViewModel>()
    private lateinit var binding: FragmentContactBinding
    private lateinit var faContactViewPagerAdapter: FAContactViewPagerAdapter

    private val viewPagerHeadingTitle = arrayListOf("My Contacts", "All Contacts")
    private var onViewCreated = true


    enum class ContactType {
        MY_CONTACTS, ALL_CONTACTS,
    }


    data class ContactTypeData(
        val savedType: ContactType = ContactType.ALL_CONTACTS,
        val itemCount: Int = -1,
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentContactBinding.bind(view)


        faPrivateContactAdapter = FAPrivateContactAdapter().apply {
            viewModel.getPrivateContact.observe(viewLifecycleOwner){
                faPrivateContactAdapter.submitList(it)
            }
            itemClickCallback {
                val bundle = Bundle().apply {
                    putParcelable("contact", it)
                }

                findNavController().navigate(
                    R.id.action_contactFragment_to_myContactDetailsFragment, bundle
                )
            }

        }


        faContactAdapter = FAContactAdapter().apply {
            viewModel.getContacts.observe(viewLifecycleOwner){
                faContactAdapter.submitList(it)
            }
            itemClickCallback {
                val bundle = Bundle().apply {
                    putParcelable("contact", it)
                }

                findNavController().navigate(
                    R.id.action_contactFragment_to_contactDetailsFragment, bundle
                )
            }

        }



        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                when (position) {
                    0 -> setContactData(
                        ContactType.MY_CONTACTS, faContactAdapter.currentList.size
                    )
                    1 -> setContactData(
                        ContactType.ALL_CONTACTS, faContactAdapter.currentList.size
                    )


                }
            }
        })


        val viewPagerAdapter = arrayListOf(
            ViewPagerData(faPrivateContactAdapter),
            ViewPagerData(faContactAdapter),
        )


        faContactViewPagerAdapter = FAContactViewPagerAdapter(viewModel, viewLifecycleOwner).apply {
            submitList(viewPagerAdapter)
            binding.viewPager.adapter = this
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewPagerHeadingTitle[position]
        }.attach()


    }

    companion object {
        val fakeContact = listOf(
            Contact(fullName = "Atlanta Grady Hospital IP Department"),
            Contact(fullName = "Atlanta Penitentiary Fulton"),
            Contact(fullName = "Atlanta Health Department Office"),
            Contact(fullName = "Cherokee Health Department Office"),
            Contact(fullName = "Cherokee Children’s Hospital"),
            Contact(fullName = "Cherokee Piedmont Hospital"),
            Contact(fullName = "DeKalb Health Department Office"),
            Contact(fullName = "DeKalb Children’s Hospital"),
            Contact(fullName = "DeKalb Piedmont Hospital"),
        )
    }



    private fun setContactData(contactType: ContactType, itemCount: Int) =
        viewModel.setSavedItemCount(ContactTypeData(contactType, itemCount))
}