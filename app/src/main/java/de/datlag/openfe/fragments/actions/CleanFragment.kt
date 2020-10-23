package de.datlag.openfe.fragments.actions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import de.datlag.openfe.R
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.databinding.FragmentCleanActionBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.recycler.adapter.BackupRecyclerAdapter
import de.datlag.openfe.viewmodel.BackupViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class CleanFragment : AdvancedFragment(R.layout.fragment_clean_action), FragmentBackPressed {

    private val backupViewModel: BackupViewModel by viewModels()

    private lateinit var backupAdapter: BackupRecyclerAdapter
    private val binding: FragmentCleanActionBinding by viewBinding()

    private val navigationListener = View.OnClickListener {
        findNavController().navigate(R.id.action_CleanActionFragment_to_OverviewFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateToggle(false, getColor(R.color.defaultNavigationColor), navigationListener)

        initRecycler()

        backupViewModel.backups.observe(viewLifecycleOwner) { list ->
            backupAdapter.submitList(list)
        }
    }

    private fun initRecycler() = with(binding) {
        backupAdapter = BackupRecyclerAdapter()

        cleanBackupRecycler.layoutManager = LinearLayoutManager(safeContext)
        cleanBackupRecycler.adapter = backupAdapter
    }

    override fun onBackPressed(): Boolean = true
}
