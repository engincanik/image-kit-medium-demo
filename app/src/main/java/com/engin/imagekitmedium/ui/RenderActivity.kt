package com.engin.imagekitmedium.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.engin.imagekitmedium.R
import com.engin.imagekitmedium.base.IKMBaseActivity
import com.engin.imagekitmedium.utils.Constants
import com.engin.imagekitmedium.utils.Utils
import com.huawei.hms.image.render.ImageRender
import com.huawei.hms.image.render.ImageRenderImpl
import com.huawei.hms.image.render.ResultCode
import java.io.File


class RenderActivity : IKMBaseActivity() {
    var sourcePath: String? = null
    private val sourcePathName = "sources"
    var imageRenderAPI: ImageRenderImpl? = null
    private lateinit var contentView: FrameLayout
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_render)
        sourcePath = filesDir.path + File.separator + sourcePathName
        initView()
        initAuthJson()
        initPermission()
    }

    private fun initImageRender() {
        ImageRender.getInstance(this, object : ImageRender.RenderCallBack {
            override fun onSuccess(imageRender: ImageRenderImpl?) {
                imageRenderAPI = imageRender
                initRenderView()
            }

            override fun onFailure(errorCode: Int) {
                Log.e(Constants.logTag, "Error Code: $errorCode")
            }
        })
    }

    fun initRenderView() {
        if (imageRenderAPI != null) {
            addView()
        } else {
            Log.e(Constants.logTag, "Init failed.")
        }
    }

    private fun addView() {
        // Initialize the ImageRender object.
        val initResult = imageRenderAPI!!.doInit(sourcePath, authJson)
        Log.i(Constants.logTag, "DoInit result == $initResult")
        if (initResult == 0) {
            // Obtain the rendered view.
            val renderView = imageRenderAPI!!.renderView
            if (renderView.resultCode == ResultCode.SUCCEED) {
                val view = renderView.view
                if (null != view) {
                    // Add the rendered view to the layout.
                    contentView.addView(view)
                } else {
                    Log.w(Constants.logTag, "GetRenderView fail, view is null")
                }
            } else if (renderView.resultCode == ResultCode.ERROR_GET_RENDER_VIEW_FAILURE) {
                Log.w(Constants.logTag, "GetRenderView fail")
            } else if (renderView.resultCode == ResultCode.ERROR_XSD_CHECK_FAILURE) {
                Log.w(
                    Constants.logTag,
                    "GetRenderView fail, resource file parameter error, please check resource file."
                )
            } else if (renderView.resultCode == ResultCode.ERROR_VIEW_PARSE_FAILURE) {
                Log.w(
                    Constants.logTag,
                    "GetRenderView fail, resource file parsing failed, please check resource file."
                )
            } else if (renderView.resultCode == ResultCode.ERROR_REMOTE) {
                Log.w(
                    Constants.logTag,
                    "GetRenderView fail, remote call failed, please check HMS service"
                )
            } else if (renderView.resultCode == ResultCode.ERROR_DOINIT) {
                Log.w(Constants.logTag, "GetRenderView fail, init failed, please init again")
            }
        } else {
            Log.w(Constants.logTag, "Do init fail, errorCode == $initResult")
        }
    }

    fun startAnimation(view: View?) {
        // Play the rendered view.
        Log.i(Constants.logTag, "Start animation")
        if (imageRenderAPI != null) {
            val playResult = imageRenderAPI!!.playAnimation()
            if (playResult == ResultCode.SUCCEED) {
                Log.i(Constants.logTag, "Start animation success")
            } else {
                Log.i(Constants.logTag, "Start animation failure")
            }
        } else {
            Log.w(Constants.logTag, "Start animation fail, please init first.")
        }
    }

    private fun initView() {
        contentView = findViewById(R.id.content)
        spinner = findViewById(R.id.animations_spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val currentAnim = spinner.adapter.getItem(position).toString()
                changeAnimation(currentAnim)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun changeAnimation(animationName: String) {
        if (!Utils.copyAssetsFilesToDirs(this, animationName, sourcePath.toString())) {
            Log.e(Constants.logTag, "copy files failure, please check permissions");
            return;
        }
        if (imageRenderAPI == null) {
            Log.e(Constants.logTag, "initRemote failed, please check Image Kit version")
            return
        }
        if (contentView.childCount > 0) {
            imageRenderAPI!!.removeRenderView()
            contentView.removeAllViews()
            addView()
        }
    }

    private fun initPermission() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            initData()
            initImageRender()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Constants.REQUEST_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // The permission is granted.
                initData()
                initImageRender()
            } else {
                // The permission is rejected.
                Log.w(Constants.logTag, "permission denied")
                Toast.makeText(
                    this,
                    "Please grant the app the permission to read the SD card",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initData() {
        if (!Utils.createResourceDirs(sourcePath)) {
            Log.e(Constants.logTag,"Create dirs fail, please check permission")
        }
        if (!Utils.copyAssetsFileToDirs(
                this, "AlphaAnimation" + File.separator + "ty.png",
                sourcePath + File.separator + "ty.png"
            )
        ) {
            Log.e(Constants.logTag,"Copy resource file fail, please check permission")
        }
        if (!Utils.copyAssetsFileToDirs(
                this,
                "AlphaAnimation" + File.separator + "bj.jpg",
                sourcePath + File.separator + "bj.jpg"
            )
        ) {
            Log.e(Constants.logTag,"Copy resource file fail, please check permission")
        }
        if (!Utils.copyAssetsFileToDirs(
                this,
                "AlphaAnimation" + File.separator + "manifest.xml",
                sourcePath + File.separator + "manifest.xml"
            )
        ) {
            Log.e(Constants.logTag,"Copy resource file fail, please check permission")
        }
    }
}