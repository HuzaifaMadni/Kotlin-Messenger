package com.huzaifa.kotlinmessenger.onboarding

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.huzaifa.kotlinmessenger.Chat.LatestMessagesActivity
import com.huzaifa.kotlinmessenger.R
import com.huzaifa.kotlinmessenger.models.User
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class RegisterActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isStoragePermissionGranted()

        have_account.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        select_photo.setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        btnRegister.setOnClickListener {
            if (registerUser()) return@setOnClickListener
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            circleImage.setImageBitmap(bitmap)
            select_photo.alpha = 0f
        }
    }

    private fun registerUser(): Boolean {
        val email = email.text.toString().trim()
        val password = password.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) return true

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                else {
                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_LONG).show()
                    uploadImageToFirebaseStorage()
                }
            }.addOnFailureListener {
                it.printStackTrace()
            }
        return false
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Toast.makeText(this, "Photo Upload Successful", Toast.LENGTH_LONG).show()

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location: $it")
                    saveDataToFirebaseDB(it.toString())
                }
            }
    }

    private fun saveDataToFirebaseDB(fileUri: String) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid!!, name.text.toString(), fileUri)

        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Data Saved", Toast.LENGTH_LONG).show()
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }

    fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                Log.v("Success", "Permission is granted")
                true
            } else {
                Log.v("Failure", "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    1)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Success", "Permission is granted")
            true
        }
    }
}

