package no.realitylab.arface.presentation.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
//import kotlinx.android.synthetic.main.activity_home.bottomNavigationView
//import kotlinx.android.synthetic.main.activity_home.fabRv
import no.realitylab.arface.R
import no.realitylab.arface.presentation.fragments.home.HomeFragment
import no.realitylab.arface.presentation.fragments.home.ProfileFragment
import no.realitylab.arface.data.models.UserData
import no.realitylab.arface.presentation.viewmodels.UserViewModel
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import no.realitylab.arface.utilities.callbacks.ActivityCallback
import no.realitylab.arface.presentation.fragments.home.ModelsListFragment


class HomeActivity : AppCompatActivity(),
    ActivityCallback {


    private lateinit var fragContainer: FrameLayout
    private lateinit var currentUser : UserData
    private lateinit var bottomNavigationView: BottomNavigationView

    private val userVideModel : UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        /**
         * Obtenemos los datos del usuario, luego de iniciar sesion.
         */
        val extras = intent.extras
        if (extras != null ) {
            currentUser = UserData(
                extras.getString("userId"),
                extras.getString("userName"),
                extras.getString("profilePictureUrl"),
                extras.getString("userEmail")
            )
        }

        // guardar dotos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("userEmail", currentUser.userEmail)
        prefs.putString("userName", currentUser.userName)
        prefs.putString("profilePictureUrl", currentUser.profilePictureUrl)
        prefs.putString("userId", currentUser.userId)
        prefs.apply()


        Log.d("APP", "-- ${currentUser.profilePictureUrl}")

        userVideModel.updateUserModel(currentUser)

        initUI()
    }

    private fun initUI() {
        bottomNavigationView = findViewById(R.id.nav_view_items)
        fragContainer = findViewById(R.id.fragmentContainer)
        replaceFragment(HomeFragment())


        bottomNavigationView.setOnNavigationItemSelectedListener {
            when ( it.itemId ) {
                R.id.home_menu -> {
                    replaceFragment(HomeFragment())
                }
                R.id.profile_menu-> {
                    replaceFragment(ProfileFragment())
                }
            }
            true
        }

    }

    private fun pushBackFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(fragContainer.id, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
    override fun onLaunchFragmentFromFragment(sender: String, msg: Int) {
        if ( sender == HomeFragment.CHANGE_TO_MODELS ) {
            val fragment = ModelsListFragment()
            val bundle = Bundle()
            bundle.putInt(ModelsListFragment.ARG_MODEL_ID, msg)
            fragment.arguments = bundle
            pushBackFragment(fragment)
        }
    }
}