package com.vinayakstudios.drawingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.io.File.separator
import java.lang.Exception

class MainActivity : AppCompatActivity(), View.OnClickListener {

   private var flBottomMenuDialog : FrameLayout? = null
   private var flColorSelectDialog : FrameLayout? = null
   private var drawingView : DrawingView? = null
   private var slBrushSizeSlider : Slider? = null
   private lateinit var ibBrushSize : ImageButton
   private lateinit var ibColorSelect : ImageButton
   private lateinit var ibPickImageBtn : ImageButton
   private lateinit var ibUndoBtn : ImageButton
   private lateinit var ibRedoBtn : ImageButton
   private lateinit var ibShareBtn : ImageButton
   private lateinit var tvBrushSize : TextView
   private lateinit var ivBackgroundImage : ImageView
   private var tvSaveBtn : TextView? = null
   private var tvShareBtn : TextView? = null

   private lateinit var sColorBlack : TextView
   private lateinit var sColorYellow : TextView
   private lateinit var sColorWhite : TextView
   private lateinit var sColorRed : TextView
   private lateinit var sColorGray : TextView
   private lateinit var sColorOlive : TextView
   private lateinit var sColorPurple : TextView
   private lateinit var sColorLime : TextView
   private lateinit var sColorGreen : TextView
   private lateinit var sColorMaroon : TextView
   private lateinit var sColorBlue : TextView
   private lateinit var sColorNavy : TextView
   private lateinit var sSelectedColor : TextView

   private var selectedBrushSize : Int = 20
   private var flBottomMenuDialogVisible : Boolean = false
   private var flColorSelectDialogVisible : Boolean = false
   private var longAnimationDuration : Int = 0
   private var customProgressDialog : Dialog? = null
   private var shareMenuDialog : Dialog? = null

   private var openGalleryLauncher : ActivityResultLauncher<Intent> =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
         result ->

         if(result.resultCode == RESULT_OK && result.data != null){
            val uri : Uri? = result.data?.data
            ivBackgroundImage.setImageURI(uri)
         }
      }

   private var requestPermission : ActivityResultLauncher<Array<String>> =
      registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
         permissions ->
         permissions.forEach{
            var isGranted = it.value

            if(isGranted){
               Toast.makeText(this,"Permission Granted!",Toast.LENGTH_SHORT)
            }
         }
      }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.main_layout)

      flBottomMenuDialog = findViewById(R.id.fl_bottomMenuDialog)
      flColorSelectDialog = findViewById(R.id.fl_colorSelectDialog)
      slBrushSizeSlider = findViewById(R.id.sl_brushSizeSlider)
      drawingView = findViewById(R.id.drawing_View)
      ibBrushSize = findViewById(R.id.ib_brushSizeBtn)
      ibColorSelect = findViewById(R.id.ib_colorSelectBtn)
      ibPickImageBtn = findViewById(R.id.ib_pickImageBtn)
      ibUndoBtn = findViewById(R.id.ib_undoBtn)
      ibRedoBtn = findViewById(R.id.ib_redoBtn)
      ibShareBtn = findViewById(R.id.ib_shareBtn)
      tvBrushSize = findViewById(R.id.tv_brushSize)
      ivBackgroundImage = findViewById(R.id.iv_backgroundImage)

      sColorBlack = findViewById(R.id.s_colorBlack)
      sColorWhite = findViewById(R.id.s_colorWhite)
      sColorYellow = findViewById(R.id.s_colorYellow)
      sColorRed = findViewById(R.id.s_colorRed)
      sColorGray = findViewById(R.id.s_colorGray)
      sColorOlive = findViewById(R.id.s_colorOlive)
      sColorPurple = findViewById(R.id.s_colorPurple)
      sColorMaroon = findViewById(R.id.s_colorMaroon)
      sColorLime = findViewById(R.id.s_colorLime)
      sColorGreen = findViewById(R.id.s_colorGreen)
      sColorBlue = findViewById(R.id.s_colorBlue)
      sColorNavy = findViewById(R.id.s_colorNavy)
      sSelectedColor = findViewById(R.id.s_selectedColor)

      longAnimationDuration = resources.getInteger(android.R.integer.config_longAnimTime)

      ibPickImageBtn.setOnClickListener(this)
      ibColorSelect.setOnClickListener(this)
      ibBrushSize.setOnClickListener(this)
      ibUndoBtn.setOnClickListener(this)
      ibRedoBtn.setOnClickListener(this)
      ibShareBtn.setOnClickListener(this)
      sColorBlack.setOnClickListener(this)
      sColorWhite.setOnClickListener(this)
      sColorYellow.setOnClickListener(this)
      sColorGray.setOnClickListener(this)
      sColorRed.setOnClickListener(this)
      sColorOlive.setOnClickListener(this)
      sColorPurple.setOnClickListener(this)
      sColorMaroon.setOnClickListener(this)
      sColorLime.setOnClickListener(this)
      sColorGreen.setOnClickListener(this)
      sColorBlue.setOnClickListener(this)
      sColorNavy.setOnClickListener(this)

      slBrushSizeSliderListener()
      setBrushSize()
      setBrushColor("#FF000000")
   }

   private fun ibBrushSizeOnClick(){
      if(flColorSelectDialogVisible) {
         flViewOutAnimation(flColorSelectDialog)
         flColorSelectDialogVisible = false
      }
      if(!flBottomMenuDialogVisible){
         flViewInAnimation(flBottomMenuDialog)
         flBottomMenuDialogVisible = true
      }
      else{
         flViewOutAnimation(flBottomMenuDialog)
         flBottomMenuDialogVisible = false
      }
   }

   private fun ibColorSelectListener(){
      if(flBottomMenuDialogVisible) {
         flViewOutAnimation((flBottomMenuDialog))
         flBottomMenuDialogVisible = false
      }
      if(!flColorSelectDialogVisible){
         flViewInAnimation(flColorSelectDialog)
         flColorSelectDialogVisible = true
      }
      else{
         flViewOutAnimation(flColorSelectDialog)
         flColorSelectDialogVisible = false
      }

   }

   private fun ibPickImageOnClick(){
      if(isReadPermissionAloud()){
         val intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
         openGalleryLauncher.launch(intent)
      }
      else{
         requestPermission()
         ibPickImageOnClick()
      }
   }

   private fun ibUndoOnClick(){
      drawingView?.onClickUndo()
   }

   private fun ibRedoOnClick(){
      drawingView?.onClickRedo()
   }

   private fun ibShareOnClick(){
      if(isReadPermissionAloud()){
         showShareMenuDialog()
      }
      else{
         requestPermission()
         showShareMenuDialog()
      }
   }

   private fun tvSaveOnClick(){
      cancelShareMenuDialog()
      showProgressDialog()
      lifecycleScope.launch {
         val flDrawingView : FrameLayout = findViewById(R.id.fl_drawingView)
         saveBitmapInSS(getBitmapFromView(flDrawingView))
      }
   }

   private fun tvShareOnClick(){
      cancelShareMenuDialog()
      showProgressDialog()
      lifecycleScope.launch {
         val flDrawingView : FrameLayout = findViewById(R.id.fl_drawingView)
         saveBitmapFile(getBitmapFromView(flDrawingView))
      }
   }

   private fun setBrushSize(){
      tvBrushSize.text = selectedBrushSize.toString()
      drawingView?.setBrushSize(selectedBrushSize.toFloat())
   }


   @SuppressLint("ResourceType")
   private fun setBrushColor(colorString : String){
      drawingView?.setBrushColor(colorString)

      val selectedColorDrawable : View = sSelectedColor
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
         selectedColorDrawable.background.colorFilter = BlendModeColorFilter(Color.parseColor(colorString),
            BlendMode.SRC_ATOP)
         }
      }


   private fun slBrushSizeSliderListener(){
         slBrushSizeSlider?.value = selectedBrushSize.toFloat()
         slBrushSizeSlider?.addOnChangeListener{ _, value, _ ->
            selectedBrushSize = value.toInt()
            setBrushSize()
         }
   }

   private fun flViewInAnimation(view : FrameLayout?){
      view?.apply{
         alpha = 0f
         visibility = View.VISIBLE

         animate()
            .alpha(1f).duration = longAnimationDuration.toLong()

         bringToFront()
      }
   }

   private fun flViewOutAnimation(view : FrameLayout?){
      view?.apply {
         alpha = 1f
         animate()
            .alpha(0f).duration = longAnimationDuration.toLong()
         visibility = View.GONE
      }
   }

   private fun isReadPermissionAloud() : Boolean{
      val result = ContextCompat.checkSelfPermission(this,
         Manifest.permission.READ_EXTERNAL_STORAGE)

      return result == PackageManager.PERMISSION_GRANTED
   }

   private fun requestPermission() {
      if(ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)){
               showRequestPermissionRationale("DrawingApp",
                  "Drawing App needs Permission"+
                          "to access Your Storage for setting Background Image")
      }

      requestPermission.launch(arrayOf
         (Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE))
   }

   private fun showRequestPermissionRationale(
      title : String,
      message : String){

      val builder : AlertDialog.Builder = AlertDialog.Builder(this)
      builder.setTitle(title)
         .setMessage(message)
         .setPositiveButton("Cancel"){ dialog,_ ->
            dialog.dismiss()
         }
      builder.create().show()
   }

   private fun getBitmapFromView(view : View): Bitmap{
      val returnedBitmap = Bitmap.createBitmap(view.width,
         view.height,Bitmap.Config.ARGB_8888)
      val canvas = Canvas(returnedBitmap)
      val bgDrawable = view.background
      if(bgDrawable != null){
         bgDrawable.draw(canvas)
      }
      else{
         canvas.drawColor(Color.WHITE)
      }
      view.draw(canvas)
      return returnedBitmap
   }

   private suspend fun saveBitmapInSS(sBitmap : Bitmap?): Boolean {
      var result: Boolean = false
      var uri: Uri? = null
      withContext(Dispatchers.IO) {
         if (sBitmap != null) {
            var imageCollection: Uri? = null

            //var completeUri : Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

               val contentValues = addContentValues(sBitmap)
               contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawingApp")
               contentValues.put(MediaStore.Images.Media.IS_PENDING, true)

               try {
                  if (imageCollection != null) {
                     uri = contentResolver.insert(imageCollection, contentValues)
                     if (uri != null) {
                        saveImageInStream(sBitmap, contentResolver.openOutputStream(uri!!))
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
                        contentResolver.update(uri!!, contentValues, null, null)
                     } else {
                        throw IOException("Unable to store Image")
                     }
                     result = true
                  } else {
                     throw IOException("Couldn't find path to save file")
                  }

                  runOnUiThread {
                     cancelProgressDialog()
                     if (result == true) {
                        Toast.makeText(
                           this@MainActivity,
                           "File Saved to Gallery",
                           Toast.LENGTH_SHORT
                        ).show()
                     } else {
                        Toast.makeText(
                           this@MainActivity,
                           "Unable to Save File",
                           Toast.LENGTH_SHORT
                        ).show()
                     }
                  }
               } catch (e: IOException) {
                  e.printStackTrace()
                  result = false
               }
            } else {
               imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
               val directory = File(
                  Environment.getExternalStorageDirectory().toString()
                          + separator + "DrawingApp"
               )

               if (!directory.exists()) {
                  directory.mkdir()
               }
               val intSet = "0123456789"
               val fileLastName = (1..7).map { (intSet.random()) }.joinToString("")
               val fileName = "DrawingApp-$fileLastName.png"
               val file = File(directory, fileName)

               saveImageInStream(sBitmap, FileOutputStream(file))
               if (file.absolutePath != null) {
                  val contentValues = ContentValues().apply {
                     put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                     put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                     put(MediaStore.Images.Media.WIDTH, sBitmap.width)
                     put(MediaStore.Images.Media.HEIGHT, sBitmap.height)
                     put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                     put(MediaStore.Images.Media.DATA, file.absolutePath)
                  }

                  contentResolver.insert(imageCollection, contentValues)
                  result = true

                  runOnUiThread {
                     if (result) {
                        Toast.makeText(
                           this@MainActivity,
                           "File Saved to Gallery",
                           Toast.LENGTH_SHORT
                        ).show()
                     } else {
                        Toast.makeText(
                           this@MainActivity,
                           "Unable to Save File",
                           Toast.LENGTH_SHORT
                        ).show()
                     }
                  }
               }
            }
         }
      }
      return result
   }

   private fun addContentValues(sBitmap : Bitmap): ContentValues{
      val intSet = "0123456789"
      val fileLastName = (1..7).map{(intSet.random())}.joinToString("")
      val fileName = "DrawingApp-$fileLastName.png"
      val contentValues = ContentValues().apply {
         put(MediaStore.Images.Media.DISPLAY_NAME,fileName)
         put(MediaStore.Images.Media.MIME_TYPE, "image/png")
         put(MediaStore.Images.Media.WIDTH, sBitmap.width)
         put(MediaStore.Images.Media.HEIGHT, sBitmap.height)
         put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis()/1000)
      }
      return contentValues
   }

   private fun saveImageInStream(sBitmap : Bitmap, outputStream : OutputStream?){
      if(outputStream != null){
         try{
            sBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream)
            outputStream.close()
         }catch(e:Exception){
            e.printStackTrace()
         }
      }
   }

   private suspend fun saveBitmapFile(mBitmap : Bitmap?): String{
      var result = ""
      withContext(Dispatchers.IO){
         if(mBitmap != null){
            try{
               var bytes = ByteArrayOutputStream()
               mBitmap.compress(Bitmap.CompressFormat.PNG,100,bytes)

               val file = File(externalCacheDir?.absolutePath
                       + File.separator + "DrawingApp" + System.currentTimeMillis() /1000 + ".png")

               val fileOutputStream = FileOutputStream(file)
               fileOutputStream.write(bytes.toByteArray())
               fileOutputStream.close()

               result = file.absolutePath

               runOnUiThread{
                  cancelProgressDialog()
                  if(file != null){
                     shareImage(FileProvider.getUriForFile(this@MainActivity
                     ,"com.vinayakstudios.drawingapp.fileprovider",file))
                  }
                  else{
                     Toast.makeText(this@MainActivity,
                        "Unable to share",
                        Toast.LENGTH_SHORT).show()
                  }

               }
            }catch(e:Exception){
               result = ""
               e.printStackTrace()
            }
         }
      }
      return result
   }

   private fun shareImage(uri:Uri){

      val shareIntent = Intent()
      shareIntent.action = Intent.ACTION_SEND
      shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
      shareIntent.type = "image/*"
      startActivity(Intent.createChooser(shareIntent,"Share"))

   }

   private fun showProgressDialog(){

      customProgressDialog = Dialog(this@MainActivity)

      customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

      customProgressDialog?.show()
   }

   private fun cancelProgressDialog(){
      if(customProgressDialog != null){
         customProgressDialog!!.dismiss()
         customProgressDialog = null
      }
   }

   private fun showShareMenuDialog(){

      shareMenuDialog = BottomSheetDialog(this)
      val view = layoutInflater.inflate(R.layout.share_menu_dialog,null)
      tvSaveBtn = view.findViewById(R.id.tv_SaveBtn)
      tvShareBtn = view.findViewById(R.id.tv_shareBtn)
      tvSaveBtn?.setOnClickListener(this)
      tvShareBtn?.setOnClickListener(this)

      shareMenuDialog?.setContentView(view)
      shareMenuDialog?.show()

   }

   private fun cancelShareMenuDialog(){
      if(shareMenuDialog != null){
         shareMenuDialog?.dismiss()
      }
      shareMenuDialog = null
   }

   override fun onClick(view: View?) {

      when(view?.id){

         R.id.ib_brushSizeBtn -> {
            ibBrushSizeOnClick()
         }

         R.id.ib_colorSelectBtn -> {
            ibColorSelectListener()
         }

         R.id.ib_pickImageBtn -> {
            ibPickImageOnClick()
         }

         R.id.ib_undoBtn -> {
            ibUndoOnClick()
         }

         R.id.ib_redoBtn -> {
            ibRedoOnClick()
         }

         R.id.ib_shareBtn -> {
            ibShareOnClick()
         }

         R.id.tv_SaveBtn -> {
            tvSaveOnClick()
         }

         R.id.tv_shareBtn -> {
            tvShareOnClick()
         }

         R.id.s_colorBlack -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorWhite -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorYellow -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorRed -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorGray -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorOlive -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorPurple -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorMaroon -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorLime -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorGreen -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorBlue -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }

         R.id.s_colorNavy -> {
            val colorString = view.tag.toString()
            setBrushColor(colorString)
         }
      }
   }
}