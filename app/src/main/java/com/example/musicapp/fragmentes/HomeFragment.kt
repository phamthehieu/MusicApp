package com.example.musicapp.fragmentes

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.adapters.SongRowOneAdapters
import com.example.musicapp.databinding.FragmentHomeBinding
import com.example.musicapp.models.Check
import com.example.musicapp.viewModel.ListMusicHomeModel
import com.example.musicapp.viewModel.UserViewModel
import com.github.ybq.android.spinkit.SpinKitView

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fragmentContext: Context
    private lateinit var userViewModel: UserViewModel
    private lateinit var listMusicHomeModel: ListMusicHomeModel

    private var myArtistsList: List<String> = listOf()

    private lateinit var spinKitView: SpinKitView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        listMusicHomeModel = ViewModelProvider(requireActivity()).get(ListMusicHomeModel::class.java)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        loadArtists()

    }

    private fun setupUI() {
        spinKitView = binding.spinKit
        spinKitView.visibility = View.VISIBLE
        binding.textTitle1.visibility = View.GONE
        binding.textTitle2.visibility = View.GONE
        binding.textTitle3.visibility = View.GONE

        val userImage = arguments?.getString("userImage", "")
        if (!userImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(userImage)
                .placeholder(R.drawable.google)
                .into(binding.profileIv)
        } else {
            binding.profileIv.setImageResource(R.drawable.ic_person_gray)
        }
    }

    private fun setupObservers() {
        listMusicHomeModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.spinKit.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.textTitle1.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.textTitle2.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.textTitle3.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        listMusicHomeModel.tracksDataLive1.observe(viewLifecycleOwner) { tracks ->
            updateRecyclerView(binding.categoriesRv1, tracks)
        }
        listMusicHomeModel.tracksDataLive2.observe(viewLifecycleOwner) { tracks ->
            updateRecyclerView(binding.categoriesRv2, tracks)
        }
        listMusicHomeModel.tracksDataLive3.observe(viewLifecycleOwner) { tracks ->
            updateRecyclerView(binding.categoriesRv3, tracks)
        }

        userViewModel.listArtistsId.observe(viewLifecycleOwner) { listArtistsId ->
            myArtistsList = listArtistsId as List<String>
            loadArtists()
        }
    }


    private fun loadArtists() {
        if (myArtistsList.size >= 3) {
            listMusicHomeModel.loadArtistData(myArtistsList[0], 1)
            listMusicHomeModel.loadArtistData(myArtistsList[1], 2)
            listMusicHomeModel.loadArtistData(myArtistsList[2], 3)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView(recyclerView: RecyclerView, tracks: List<Check.DataSong>) {
        val adapter = SongRowOneAdapters(requireContext(), ArrayList(tracks))
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}