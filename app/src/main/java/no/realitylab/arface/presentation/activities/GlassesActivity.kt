package no.realitylab.arface.presentation.activities

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_glasses.*
import no.realitylab.arface.utilities.FaceArFragment
import no.realitylab.arface.R
import java.io.ByteArrayOutputStream
import java.util.ArrayList

class GlassesActivity : AppCompatActivity() {

    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private lateinit var arFragment: FaceArFragment
    private var glasses: ArrayList<ModelRenderable> = ArrayList()
    private var faceRegionsRenderable: ModelRenderable? = null

    private var faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()
    private var index: Int = 0
    private var changeModel: Boolean = false

    private lateinit var auth: FirebaseAuth
    private lateinit var fireDatabase: DatabaseReference
    private lateinit var storageRef : StorageReference

    private lateinit var listOfStrings: ArrayList<String>
    private lateinit var userId : String
    private lateinit var captureBitmap : Bitmap

    private lateinit var sceneView: ArSceneView
    /*
    * Declaracion de los elementos necesarios para la interfaz
    * de lentes.
    *
     */
    private lateinit var nextButton: ImageButton
    private lateinit var cameraButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }
        setContentView(R.layout.activity_glasses)

        listOfStrings = intent.getStringArrayListExtra("models_list") as ArrayList<String>
        userId = intent.getStringExtra("userId") as String

        arFragment = face_fragment as FaceArFragment



        initUI()
        initListeners()
        initServices()

        // load models
        for ( i in listOfStrings ) {
            buildModel(i)
        }

        sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        val scene = sceneView.scene

        scene.addOnUpdateListener {
            if (faceRegionsRenderable != null) {
                sceneView.session
                    ?.getAllTrackables(AugmentedFace::class.java)?.let {
                        for (f in it) {
                            if (!faceNodeMap.containsKey(f)) {
                                val faceNode = AugmentedFaceNode(f)
                                faceNode.setParent(scene)
                                faceNode.faceRegionsRenderable = faceRegionsRenderable
                                faceNodeMap[f] = faceNode
                            } else if (changeModel) {
                                faceNodeMap.getValue(f).faceRegionsRenderable = faceRegionsRenderable
                            }
                        }
                        changeModel = false
                        // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
                        val iter = faceNodeMap.entries.iterator()
                        while (iter.hasNext()) {
                            val entry = iter.next()
                            val face = entry.key
                            if (face.trackingState == TrackingState.STOPPED) {
                                val faceNode = entry.value
                                faceNode.setParent(null)
                                iter.remove()
                            }
                        }
                    }
            }
        }
    }

    private fun buildModel(uriString: String) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(uriString))
            .build()
            .thenAccept { modelRenderable ->
                glasses.add(modelRenderable)
                faceRegionsRenderable = modelRenderable
                modelRenderable.isShadowCaster = false
                modelRenderable.isShadowReceiver = false
            }
    }

    private fun initUI() {
        nextButton = findViewById(R.id.button_next)
        cameraButton = findViewById(R.id.button_camera)
    }

    private fun initServices() {
        auth = FirebaseAuth.getInstance()
        fireDatabase = FirebaseDatabase
            .getInstance("https://styleapp-50e33-default-rtdb.firebaseio.com/")
            .reference
        storageRef =  FirebaseStorage.getInstance().reference
    }

    private fun initListeners() {
        nextButton.setOnClickListener {
            changeModel = !changeModel
            index++
            if (index > glasses.size - 1) {
                index = 0
            }
            faceRegionsRenderable = glasses[index]
        }

        cameraButton.setOnClickListener {
            captureARModelsIsImage()
        }
    }

    private fun captureARModelsIsImage() {
        // obtener la vista de AR
        progressBar.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        cameraButton.visibility = View.GONE
        // take picture of the current view.
        val sceneView = arFragment.arSceneView as SurfaceView
        // crear un bitmap para capturar la imagen.
        val bitmap = Bitmap.createBitmap(sceneView.width, sceneView.height, Bitmap.Config.ARGB_8888)

        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()

        PixelCopy.request(sceneView, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS ) {
                uploadImageToFirebaseStorage(bitmap)
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap) {
        // create a unique reference to photos
        val imageName = "$userId-${System.currentTimeMillis()}.jpg"
        val imageRef = storageRef.child("user_pictures/${userId}/${imageName}")

        // Convertir el bitmap capturado a un bitarray.
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,40, stream)
        val byteArray = stream.toByteArray()

        // subir el bytearray a firebase storage.
        val uploadTask = imageRef.putBytes(byteArray)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri->
                val imageUrl = uri.toString()
                val userPicturesRef = fireDatabase.child("users").child(userId).child("photos")
                userPicturesRef.push().setValue(imageUrl).addOnCompleteListener {
                    var mensaje = ""
                    mensaje = if ( it.isSuccessful ) {
                        "Imagen guardada "
                    } else {
                        "No se guardo la imagen"
                    }
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        nextButton.visibility = View.VISIBLE
                        cameraButton.visibility = View.VISIBLE
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {exception ->
            Toast.makeText(this, "Error al subir la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkIsSupportedDeviceOrFinish() : Boolean {
        if (ArCoreApk.getInstance().checkAvailability(this) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Toast.makeText(this, "Augmented Faces requires ARCore", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val openGlVersionString =  (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.deviceConfigurationInfo
            ?.glEsVersion

        openGlVersionString?.let { s ->
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
                finish()
                return false
            }
        }
        return true
    }
}