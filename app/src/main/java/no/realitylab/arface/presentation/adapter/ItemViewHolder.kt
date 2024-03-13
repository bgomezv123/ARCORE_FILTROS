package no.realitylab.arface.presentation.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import no.realitylab.arface.R
import no.realitylab.arface.data.models.Photo

class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val userPhoto = view.findViewById<RoundedImageView>(R.id.photo_post)

    fun bind(photo: Photo) {
        Picasso.get().load(photo.photoUrl).into(userPhoto)
    }
}