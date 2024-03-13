package no.realitylab.arface.presentation.fragments.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

import no.realitylab.arface.R
import no.realitylab.arface.databinding.FragmentLoginBinding
import no.realitylab.arface.presentation.activities.HomeActivity
import no.realitylab.arface.utilities.callbacks.ActivityCallback
import java.io.ByteArrayOutputStream

const val REQUEST_CODE_SIGN_IN = 0
class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding

    private lateinit var inflate: View
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var btnGoogleLogIn: Button
    private lateinit var btnLogIn: Button
    private lateinit var btnRegister: TextView
    private lateinit var callback: ActivityCallback
    private lateinit var auth : FirebaseAuth
    private lateinit var fireDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        inflate = inflater.inflate(R.layout.fragment_login, container, false)
        initServices()
        initUI()
        initListeners()
        return inflate
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if ( context is ActivityCallback) {
                callback = context
            }
        }
        catch (e: Exception) {
            Log.e("Error", e.printStackTrace().toString())
        }
    }

    private fun initUI() {
        tvEmail = inflate.findViewById(R.id.et_login_email)
        tvPassword = inflate.findViewById(R.id.et_login_password)
        btnGoogleLogIn = inflate.findViewById(R.id.btnGoogleSign)
        btnLogIn = inflate.findViewById(R.id.btnLogin)
        btnRegister = inflate.findViewById(R.id.tv_register_button)
    }

    private fun initServices() {
        auth = FirebaseAuth.getInstance()
        fireDatabase = FirebaseDatabase
            .getInstance("https://styleapp-50e33-default-rtdb.firebaseio.com/")
            .reference
    }

    private fun initListeners() {
        btnLogIn.setOnClickListener {
            val email = tvEmail.text
            val password = tvPassword.text
            if ( email.isNotEmpty() && password.isNotEmpty() ) {
                auth.signInWithEmailAndPassword(email.toString(), password.toString())
                    .addOnCompleteListener { task ->
                        if ( task.isSuccessful ) {
                            val currentUser = auth.currentUser
                            val userId = currentUser?.uid
                            if (userId != null ) {
                                val userRef = fireDatabase.child("users").child(userId)
                                userRef.addValueEventListener (object : ValueEventListener {
                                    override fun onDataChange( dataSnapshot: DataSnapshot ) {
                                        val userName = dataSnapshot.child("userName").getValue(String::class.java)
                                        val userEmail = dataSnapshot.child("userEmail").getValue(String::class.java)
                                        val userPhotoUri = dataSnapshot.child("profilePictureUrl").getValue(String::class.java)

                                        Log.d("APP", "userphoto $userPhotoUri, $userName, $userEmail")

                                        tvEmail.text = ""
                                        tvPassword.text = ""

                                        showHomeActivity(
                                            userId = userId,
                                            userName = userName?:"",
                                            userEmail = userEmail?:"",
                                            userPhoto = userPhotoUri ?:""
                                        )
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Toast.makeText(requireContext(), "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    }
            }
            else {
                Toast.makeText(requireContext(), "Complete los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoogleLogIn.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclient_id))
                .requestEmail()
                .build()

            val signInClient = GoogleSignIn.getClient(requireActivity(), options)
            startActivityForResult(signInClient.signInIntent, REQUEST_CODE_SIGN_IN)
        }

        btnRegister.setOnClickListener {
            callback.onLaunchFragmentFromFragment(CHANGE_TO_SIGN_UP, 1)
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credentials).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid
                val userName = user?.displayName ?: ""
                val userEmail = user?.email ?: ""
                val userPhoto = user?.photoUrl

                if (userId != null) {
                    val userRef = fireDatabase.child("users").child(userId)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val userNameSnap = snapshot.child("userName").getValue(String::class.java) ?: ""
                                val userEmailSnap = snapshot.child("userEmail").getValue(String::class.java) ?: ""
                                val userPhotoUriSnap = snapshot.child("profilePictureUrl").getValue(String::class.java) ?: ""
                                showHomeActivity(userId, userNameSnap, userPhotoUriSnap, userEmailSnap)
                            }
                            else {
                                downloadAndSaveUserImage(userPhoto, userRef, userId, userName, userEmail)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            showAlert("Se ha cancelado el proceso de registro")
                        }
                    })
                }
            }
            else {
                showAlert("Se ha producido un error en el proceso de autenticación")
            }
        }
    }

    private fun downloadAndSaveUserImage(
        userPhoto: Uri?,
        userRef: DatabaseReference,
        userId: String,
        userName: String,
        userEmail: String
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("user_photo/$userId.jpg")

        Glide.with(this@LoginFragment)
            .asBitmap()
            .load(userPhoto)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val baos = ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.JPEG,80, baos)
                    val data = baos.toByteArray()
                    val urlTask = imageRef.putBytes(data)
                    urlTask.addOnSuccessListener { task ->
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUri = uri.toString()
                            val userMap = HashMap<String, String>()
                            userMap["userName"] = userName
                            userMap["userEmail"] = userEmail
                            userMap["profilePictureUrl"] = downloadUri

                            userRef.setValue(userMap).addOnCompleteListener { saveTask ->
                                if (saveTask.isSuccessful) {
                                    showHomeActivity(userId, userName, downloadUri, userEmail)
                                } else {
                                    showAlert("Error al registrar al usuario")
                                }
                            }
                        }
                    }
                }

                override fun onLoadCleared(p0: Drawable?) {
                    showAlert("Se limpio los datos")
                }
            })
    }

    private fun showAlert(text: String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Error")
        builder.setMessage(text)
        builder.setPositiveButton("Aceptar", null)
        val dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( requestCode == REQUEST_CODE_SIGN_IN ) {
            if ( data != null && resultCode == Activity.RESULT_OK) {
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
                    account?.let {
                        googleAuthForFirebase(it)
                    }
                }
                catch (e: Exception) {
                    showAlert("Excepción esperando el registro")
                }
            }
        }
    }

    private fun showHomeActivity(userId: String, userName: String, userPhoto: String, userEmail: String) {
        val intent = Intent(activity, HomeActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("userName",userName)
            putExtra("profilePictureUrl", userPhoto)
            putExtra("userEmail",userEmail)
        }
        startActivity(intent)
    }

    companion object {

        const val CHANGE_TO_SIGN_UP = "sign_up_model"
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle()
            }
    }


}