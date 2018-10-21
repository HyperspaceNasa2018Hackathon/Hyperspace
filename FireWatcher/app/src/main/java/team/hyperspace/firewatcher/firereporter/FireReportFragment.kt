package team.hyperspace.firewatcher.firereporter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import team.hyperspace.firewatcher.R
import team.hyperspace.firewatcher.network.NetworkService
import team.hyperspace.firewatcher.network.Prediction
import team.hyperspace.firewatcher.network.ReportResponse
import team.hyperspace.firewatcher.utility.LocationHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FireReportFragment : Fragment() {

    private lateinit var pickedImage : ImageView
    private var pickerImageBitmap : Bitmap? = null
    private lateinit var location : Location
    private lateinit var time : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_fire_report, container, false)
        pickedImage = view.findViewById(R.id.picked_image)
        initComponent(view)
        return view
    }

    private val PICK_IMAGE : Int = 201
    private val TAKE_PHOTO : Int = 202

    private fun initComponent(view : View ?) {
        if (view != null) {
            val browseBtn : View? = view.findViewById(R.id.browse_btn)
            setOnClickListener(browseBtn, object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    val intent = Intent()
                    intent.setType("image/*")
                    intent.setAction(Intent.ACTION_GET_CONTENT)
                    startActivityForResult(Intent.createChooser(intent, "Select Fire Scene Picture"), PICK_IMAGE)
                }
            })

            val cameraBtn : View? = view.findViewById(R.id.camera_btn)
            setOnClickListener(cameraBtn, object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    val fragmentContext : Context? = context
                    if (fragmentContext != null) {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                            // Ensure that there's a camera activity to handle the intent
                            takePictureIntent.resolveActivity(fragmentContext.packageManager)?.also {
                                // Create the File where the photo should go
                                val photoFile: File? = try {
                                    createImageFile(fragmentContext)
                                } catch (ex: IOException) {
                                    // Error occurred while creating the File
                                    null
                                }
                                // Continue only if the File was successfully created
                                photoFile?.also {
                                    val photoURI: Uri = FileProvider.getUriForFile(
                                        fragmentContext,
                                        "team.hyperspace.firewatcher",
                                        it
                                    )
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(takePictureIntent, TAKE_PHOTO)
                                }
                            }
                        }
                    }
                }
            })

            val submitBin : View ? = view.findViewById(R.id.submit_btn)
            if (submitBin != null) {
                submitBin.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        if (!::time.isInitialized || TextUtils.isEmpty(time)) {
                            Toast.makeText(view.context, "time is empty", Toast.LENGTH_LONG).show()
                            return
                        }
                        if (!::location.isInitialized) {
                            Toast.makeText(view.context, "location is empty", Toast.LENGTH_LONG).show()
                            return
                        }
                        if (!::uploadImageFile.isInitialized) {
                            Toast.makeText(view.context, "Please select image!!", Toast.LENGTH_LONG).show()
                            return
                        }

                        if (context != null ) {
                            val fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), uploadImageFile)
                            val fileMultipart : MultipartBody.Part = MultipartBody.Part.createFormData("File", "test.jpg", fileBody)
                            Toast.makeText(context, "begin send report", Toast.LENGTH_LONG).show()
                            NetworkService.fireReportService.report(
                                fileMultipart,
                                RequestBody.create(MediaType.parse("multipart/form-data"), "No name"),
                                RequestBody.create(MediaType.parse("multipart/form-data"), location!!.latitude.toString()),
                                RequestBody.create(MediaType.parse("multipart/form-data"), location!!.longitude.toString()),
                                RequestBody.create(MediaType.parse("multipart/form-data"), time)
                            ).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    object : Consumer<ReportResponse> {
                                        override fun accept(report: ReportResponse?) {
                                            var reportStr : String = ""
                                            if (report != null) {
                                                val predictions : List<Prediction> = report.predictions
                                                if (predictions != null) {
                                                    for (prediction in predictions) {
                                                        reportStr += "Category: " + prediction.tagName + ", probability: " + prediction.probability + "\n"
                                                    }
                                                }
                                            }
                                            Toast.makeText(context, "send report success\n" + reportStr, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    object : Consumer<Throwable> {
                                        override fun accept(t: Throwable?) {
                                            Toast.makeText(context, "send report failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                        }
                    }
                })
            }
        }
    }

    private var disposable : Disposable = Disposables.disposed()

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun updateTimeAndLocation() {
        val timeText : TextView = view!!.findViewById(R.id.time)
        val format : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("GMT")
        time = format.format(Date())
        timeText.setText(time)

        val locationText : TextView = view!!.findViewById(R.id.location)
        val fragmentContext : Context? = context
        if (fragmentContext != null) {
            locationText.setText("")
            disposable.dispose()
            disposable = LocationHelper.locationHelper.getUserLastPosition(fragmentContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(object : Action {
                    override fun run() {
                        disposable = Disposables.disposed()
                    }
                })
                .subscribe(
                    object : io.reactivex.functions.Consumer<Location> {
                        override fun accept(location: Location) {
                            locationText.setText("${location.latitude}, ${location.longitude}")
                            this@FireReportFragment.location = location
                        }

                    },
                    object : io.reactivex.functions.Consumer<Throwable> {
                        override fun accept(p0: Throwable) {
                            Toast.makeText(fragmentContext, "get location failed!!", Toast.LENGTH_LONG).show()
                        }
                    }
                )
        }
    }

    private lateinit var photoPath : String

    @Throws(IOException::class)
    private fun createImageFile(fragmentContext : Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = fragmentContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            photoPath = absolutePath
        }
    }

    private lateinit var uploadImageFile : File

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE) {
                val uri : Uri? = data.data
                val fragmentContext : Context? = context
                if (uri != null && fragmentContext != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(fragmentContext.contentResolver, uri)
                    pickedImage.setImageBitmap(bitmap)
                    exportUploadImage(bitmap)
                }
            } else if (requestCode == TAKE_PHOTO) {
                if (::photoPath.isInitialized) {
                    val imageBitmap = BitmapFactory.decodeFile(photoPath)
                    pickedImage.setImageBitmap(imageBitmap)
                    exportUploadImage(imageBitmap)
                }
            }
        }
    }

    private fun exportUploadImage(bitmap : Bitmap) {
        uploadImageFile = createImageFile(context!!)
        val test = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, false)
        test!!.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(uploadImageFile))
    }

    private fun setOnClickListener(view : View?, onClickListener : View.OnClickListener?) {
        if (view != null && onClickListener != null) {
            view.setOnClickListener(onClickListener)
        }
    }

    fun onPageSelected() {
        updateTimeAndLocation()
    }
}