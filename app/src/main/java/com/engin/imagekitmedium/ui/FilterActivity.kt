package com.engin.imagekitmedium.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.engin.imagekitmedium.R
import com.engin.imagekitmedium.base.IKMBaseActivity
import com.engin.imagekitmedium.utils.Constants
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVision.VisionCallBack
import com.huawei.hms.image.vision.ImageVisionImpl
import com.huawei.hms.image.vision.crop.CropLayoutView
import kotlinx.android.synthetic.main.activity_filter.*
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FilterActivity : IKMBaseActivity() {
    var imageVisionAPI: ImageVisionImpl? = null
    private var bitmap: Bitmap? = null
    private var initCode = -1
    private var executorService: ExecutorService = Executors.newFixedThreadPool(1)
    lateinit var filterVal: EditText
    lateinit var intensityVal: EditText
    lateinit var cRVal: EditText
    private var mPermissionList: MutableList<String> = ArrayList()
    var dataStorePermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    lateinit var cropLayoutView: CropLayoutView;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        initAuthJson()
        initImageVisionAPI(this)
        filterVal = findViewById(R.id.filterVal)
        intensityVal = findViewById(R.id.intensityVal)
        cRVal = findViewById(R.id.cRVal)
        cropLayoutView = findViewById(R.id.cropImageView)
    }


    private fun initDataStoragePermission(permissionArray: Array<String>) {
        mPermissionList.clear()
        for (i in permissionArray.indices) {
            if (ContextCompat.checkSelfPermission(this, permissionArray[i])
                != PackageManager.PERMISSION_GRANTED
            ) {
                mPermissionList.add(permissionArray[i])
            }
        }
        if (mPermissionList.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                mPermissionList.toTypedArray(),
                Constants.REQUEST_PERMISSION
            )
        }
    }


    fun getPhotoUI(view: View) {
        initDataStoragePermission(dataStorePermissions)
        getPhoto(this)
    }

    fun filterImage(view: View) {
        if (cRVal.text != null && intensityVal.text != null && filterVal.text != null) {
            startFilter(
                filterVal.text.toString(),
                intensityVal.text.toString(),
                cRVal.text.toString(),
                authJson
            )
        } else {
            toastOnUIThread("Please enter values for filter")
        }
    }

    private fun getPhoto(activity: Activity) {
        val getPhoto = Intent(Intent.ACTION_GET_CONTENT)
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        getPhoto.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        getPhoto.type = "image/*"
        getPhoto.addCategory(Intent.CATEGORY_OPENABLE)
        activity.startActivityForResult(getPhoto, Constants.REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    Constants.REQUEST_PICK_IMAGE -> {
                        try {
                            val uri: Uri? = data.data
                            imageView!!.setImageURI(uri)
                            bitmap = (imageView!!.drawable as BitmapDrawable).bitmap
                            setCropLayoutView(bitmap!!)
                        } catch (e: Exception) {
                            Log.e(Constants.logTag, e.toString())
                        }
                    }
                }
            }
        }
    }

    private fun initImageVisionAPI(context: Context?) {
        try {
            imageVisionAPI = ImageVision.getInstance(this)
            imageVisionAPI!!.setVisionCallBack(object : VisionCallBack {
                override fun onSuccess(successCode: Int) {
                    initCode = imageVisionAPI!!.init(context, authJson)
                }

                override fun onFailure(errorCode: Int) {
                    toastOnUIThread("Error $errorCode")
                    Log.i(Constants.logTag, "Error: $errorCode")
                }

            })
        } catch (e: Exception) {
            toastOnUIThread(e.toString())
        }
    }

    private fun toastOnUIThread(toastText: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
        }
    }

    private fun startFilter(
        filterType: String, intensity: String,
        compress: String, authJson: JSONObject?
    ) {
        val runnable = Runnable {
            val jsonObject = JSONObject()
            val taskJson = JSONObject()
            try {
                taskJson.put("intensity", intensity)
                taskJson.put("filterType", filterType)
                taskJson.put("compressRate", compress)
                jsonObject.put("requestId", "1")
                jsonObject.put("taskJson", taskJson)
                jsonObject.put("authJson", authJson)
                val visionResult = imageVisionAPI!!.getColorFilter(
                    jsonObject,
                    bitmap
                )
                imageView!!.post {
                    val image = visionResult.image
                    if (image != null) {
                        imageView!!.setImageBitmap(image)
//                        setCropLayoutView(image)
                    } else {
                        toastOnUIThread("There is a problem, image not filtered please try again later.")
                    }
                }
            } catch (e: JSONException) {
                toastOnUIThread(e.toString())
                Log.e(Constants.logTag, e.toString())
            }
        }
        executorService.execute(runnable)
    }

    private fun setCropLayoutView(selectedImage: Bitmap) {
        cropLayoutView.setImageBitmap(selectedImage)
    }

    fun rotateIV(view: View) {
        val bitmap = cropLayoutView.croppedImage
        imageView.setImageBitmap(bitmap)
        imageView.visibility = View.VISIBLE
        cropLayoutView.visibility = View.INVISIBLE
    }


}