package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactAddBinding
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAContactViewModel
import org.apphatchery.gatbreferenceguide.utils.toast

@AndroidEntryPoint
class ContactAddFragment : BaseFragment(R.layout.fragment_contact_add) {

    private val viewModel by viewModels<FAContactViewModel>()
    private lateinit var binding: FragmentContactAddBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentContactAddBinding.bind(view)


    }

}