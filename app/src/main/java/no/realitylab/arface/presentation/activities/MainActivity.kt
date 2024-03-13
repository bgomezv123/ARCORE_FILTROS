package no.realitylab.arface.presentation.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.mainLayout
import no.realitylab.arface.R
import no.realitylab.arface.utilities.callbacks.ActivityCallback
import no.realitylab.arface.presentation.fragments.main.LoginFragment
import no.realitylab.arface.presentation.fragments.main.SignupFragment


const val REQUEST_CODE_SIGN_IN = 0
class MainActivity : AppCompatActivity(), ActivityCallback {

    private lateinit var fragContainer: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        session()
    }

    override fun onStart() {
        super.onStart()
        mainLayout.visibility = View.VISIBLE
        replaceFragment(LoginFragment())
    }

    private fun initUI() {
        fragContainer = findViewById(R.id.fragment_container_main)
        replaceFragment(LoginFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(fragContainer.id, fragment)
        transaction.commit()
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", null)
        val username = prefs.getString("userName", null)
        val userid = prefs.getString("userId", null)
        val userPhotoUri = prefs.getString("profilePictureUrl", null)

        if (!email.isNullOrEmpty() && !userPhotoUri.isNullOrEmpty() && !username.isNullOrEmpty() && !userid.isNullOrEmpty()) {
            mainLayout.visibility = View.INVISIBLE
            showHomeActivity(
                userEmail = email,
                userName = username,
                userPhoto = userPhotoUri,
                userId = userid
            )
        }
    }

    private fun showHomeActivity(userId: String, userName: String, userPhoto: String, userEmail: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("userName",userName)
            putExtra("profilePictureUrl", userPhoto)
            putExtra("userEmail",userEmail)
        }
        startActivity(intent)
        finish()
    }

    private fun pushBackFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(fragContainer.id, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onLaunchFragmentFromFragment(sender: String, msg: Int) {
        if ( sender == LoginFragment.CHANGE_TO_SIGN_UP ) {
            val fragment = SignupFragment()
            pushBackFragment(fragment)
        }
    }

}
