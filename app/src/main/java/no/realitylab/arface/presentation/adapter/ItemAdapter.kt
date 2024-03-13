package no.realitylab.arface.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import no.realitylab.arface.R
import no.realitylab.arface.data.models.Photo

class ItemAdapter(
    private var modelsList : List<Photo> = emptyList()
    //private val changeToRV: (Int) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {


    fun updateData(newItemList: List<Photo>) {
        modelsList = newItemList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return modelsList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(modelsList[position])
        /*
        holder.itemView.setOnClickListener {
            //changeToRV(position)
            val intent = Intent(it.context, GlassesActivity::class.java)
            it.context.startActivity(intent)
        }
        */
    }
}