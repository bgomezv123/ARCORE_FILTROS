package no.realitylab.arface.presentation.fragments.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.amulyakhare.textdrawable.TextDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import no.realitylab.arface.R
import no.realitylab.arface.presentation.activities.HomeActivity
import no.realitylab.arface.utilities.callbacks.ActivityCallback
import java.io.ByteArrayOutputStream
import java.util.Objects
import kotlin.Exception


class SignupFragment : Fragment() {

    private lateinit var layout : View
    private lateinit var auth: FirebaseAuth
    private lateinit var fireDatabase: DatabaseReference
    private lateinit var tvUserName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var tvPasswordTest: TextView
    private lateinit var btnBack: Button
    private lateinit var btnRegister: Button

    private lateinit var callback: ActivityCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       layout = inflater.inflate(R.layout.fragment_signup, container, false)
        initServices()
        initUI()
        initListeners()
        return layout
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if (context is ActivityCallback) {
                callback = context
            }
        }
        catch (e: Exception) {
            Log.e("Error", e.printStackTrace().toString())
        }
    }

    private fun initListeners() {
        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnRegister.setOnClickListener {
            if (tvUserName.text.isNotEmpty() &&
                tvEmail.text.isNotEmpty() &&
                tvPassword.text.isNotEmpty() &&
                tvPasswordTest.text.isNotEmpty())
            {
                if (tvPassword.text.length >= 6 ) {
                    val email = tvEmail.text.toString()
                    val password = tvPassword.text.toString()
                    val name = tvUserName.text.toString()
                    registerUser(email, password, name)
                }
                else {
                    Toast.makeText(requireContext(), "Password mas de 6 caracteres", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerUser(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            val userId = auth.currentUser?.uid

            if ( it.isSuccessful ) {

                // Obtener letra inicial.
                val initialLetter = email.substring(0,1)
                // Crear imagen con letra inicial.
                val drawable = TextDrawable.builder().buildRect(initialLetter, Color.BLUE)

                val baos = ByteArrayOutputStream()
                val bitmap = drawable.toBitmap(80,80)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val data = baos.toByteArray()


                // guardar la imagen en firebase storage
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("user_photo/$userId.jpg")


                imageRef.putBytes(data)
                    .addOnFailureListener {
                        Log.d("APP", "PROBLEMAS ${it.printStackTrace()}")
                    }
                    .addOnSuccessListener { task ->
                        imageRef.downloadUrl.addOnSuccessListener {uri ->
                            val downloadUri = uri.toString()

                            val map = HashMap<String, String>()
                            map["userName"] = name
                            map["userEmail"] = email
                            map["profilePictureUrl"] = downloadUri

                            if (userId != null) {
                                fireDatabase.child(userId).setValue(map).addOnCompleteListener { task ->
                                    if ( task.isSuccessful ) {
                                        val intent = Intent(requireContext(), HomeActivity::class.java).apply {
                                            putExtra("userId", userId)
                                            putExtra("userName", name)
                                            putExtra("profilePictureUrl", downloadUri)
                                            putExtra("userEmail",email)
                                        }
                                        startActivity(intent)
                                        Toast.makeText(requireContext(), "Registrado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            else {
                Toast.makeText(requireContext(), "problemas al registrar usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initUI() {
        tvUserName = layout.findViewById(R.id.et_register_name)
        tvEmail = layout.findViewById(R.id.et_register_email)
        tvPassword = layout.findViewById(R.id.et_register_password)
        tvPasswordTest = layout.findViewById(R.id.et_register_password_2)
        btnBack = layout.findViewById(R.id.btn_register_back)
        btnRegister = layout.findViewById(R.id.btn_register_register)
    }

    private fun initServices() {
        auth = FirebaseAuth.getInstance()
        fireDatabase = FirebaseDatabase
            .getInstance("https://styleapp-50e33-default-rtdb.firebaseio.com/")
            .getReference("users")
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignupFragment().apply {
                arguments = Bundle()
            }
    }
}