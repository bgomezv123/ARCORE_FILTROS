package no.realitylab.arface.presentation.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import no.realitylab.arface.R
import no.realitylab.arface.presentation.adapter.ItemAdapter
import no.realitylab.arface.presentation.viewmodels.ItemViewModel


class ModelsListFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private val itemViewModel : ItemViewModel by activityViewModels()
    private lateinit var inflate: View
    private lateinit var adapter: ItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflate = inflater.inflate(R.layout.fragment_models_list, container, false)
        progressBar = inflate.findViewById(R.id.progressBarList)
        itemViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        })
        itemViewModel.getPhotosFromFirebase() // Obtener las fotos desde Firebase
        initRecyclerView()
        return inflate
    }


    private fun initRecyclerView(){
        recyclerView = inflate.findViewById(R.id.recycler_models)
        adapter = ItemAdapter(emptyList())
        recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = adapter
        itemViewModel.models.observe(viewLifecycleOwner, Observer { photoList ->
            adapter.updateData(photoList)
        })


    }


    companion object {

        const val ARG_MODEL_ID = "param1"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ModelsListFragment().apply {
                arguments = Bundle()
            }
    }
}