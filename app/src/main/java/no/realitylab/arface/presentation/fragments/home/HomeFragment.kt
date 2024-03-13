package no.realitylab.arface.presentation.fragments.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import no.realitylab.arface.R
import no.realitylab.arface.presentation.activities.GlassesActivity
import no.realitylab.arface.utilities.callbacks.ActivityCallback
import no.realitylab.arface.data.models.UserData
import no.realitylab.arface.presentation.viewmodels.UserViewModel


class HomeFragment : Fragment() {

    private val userViewModel : UserViewModel by activityViewModels()
    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var glassesCardView: CardView
    private lateinit var capCardView: CardView
    private lateinit var hairCardView: CardView
    private lateinit var inflate: View

    private lateinit var userId: String
    private lateinit var callback: ActivityCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userObserver = Observer<UserData> { userData ->
            userId = userData.userId?: ""
            Picasso.get().load(userData.profilePictureUrl).into(profileImage)
            userName.text = userData.userName
            userEmail.text = userData.userEmail
        }
        userViewModel.userData.observe(this, userObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        inflate = inflater.inflate(R.layout.fragment_home, container, false)
        initUI()
        initListeners()

        return inflate
    }

    private fun initUI() {
        profileImage = inflate.findViewById(R.id.image_view)
        userName = inflate.findViewById(R.id.tv_username)
        userEmail = inflate.findViewById(R.id.tv_useremail)

        glassesCardView = inflate.findViewById(R.id.card_view_glasses)
        hairCardView = inflate.findViewById(R.id.card_view_hair)
        capCardView = inflate.findViewById(R.id.card_view_cap)
    }

    private fun initListeners() {

        glassesCardView.setOnClickListener {
            //callback.onLaunchFragmentFromFragment(CHANGE_TO_MODELS, 1)

            val intent = Intent(activity, GlassesActivity::class.java)
            val arrayList = ArrayList<String>()
            arrayList.add("lente_color.sfb")
            arrayList.add("sunglasses.sfb")
            arrayList.add("yellow_sunglasses.sfb")
            arrayList.add("glass1.sfb")
            arrayList.add("glass2.sfb")
            arrayList.add("sunglass1.sfb")
            arrayList.add("sunglass2.sfb")
            arrayList.add("sunglass.sfb")
            intent.putStringArrayListExtra("models_list", arrayList)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        hairCardView.setOnClickListener {
            val intent = Intent(activity, GlassesActivity::class.java)
            val arrayList = ArrayList<String>()
            arrayList.add("hair1.sfb")
            arrayList.add("hair2.sfb")
            arrayList.add("hair3.sfb")
            arrayList.add("hair4.sfb")
            arrayList.add("hair5.sfb")
            intent.putStringArrayListExtra("models_list", arrayList)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        capCardView.setOnClickListener {
            val intent = Intent(activity, GlassesActivity::class.java)
            val arrayList = ArrayList<String>()
            arrayList.add("cap1.sfb")
            arrayList.add("cap2.sfb")
            arrayList.add("cap3.sfb")
            arrayList.add("cap4.sfb")
            intent.putStringArrayListExtra("models_list", arrayList)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if ( context is ActivityCallback) {
                callback = context
            }
        } catch ( e: Exception ) {
            Log.e("Error", e.printStackTrace().toString())
        }
    }

    companion object {
        const val CHANGE_TO_MODELS = "to_models"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle()
            }
    }
}