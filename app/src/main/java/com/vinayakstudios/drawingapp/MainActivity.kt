package com.vinayakstudios.drawingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity(), View.OnClickListener {

   private var flBottomMenuDialog : FrameLayout? = null
   private var flColorSelectDialog : FrameLayout? = null
   private var drawingView : DrawingView? = null
   private var slBrushSizeSlider : Slider? = null
   private lateinit var ibBrushSize : ImageButton
   private lateinit var ibColorSelect : ImageButton
   private lateinit var ibPickImageBtn : ImageButton
   private lateinit var ibUndoBtn : ImageButton
   private lateinit var tvBrushSize : TextView
   private lateinit var ivBackgroundImage : ImageView

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
            var permissionName = it.key
            var isGranted = it.value

            if(isGranted){
               if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE)
               {
                  val intent = Intent(Intent.ACTION_PICK,
                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                  openGalleryLauncher.launch(intent)
               }
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

   private fun ibUndoOnClick(){
      drawingView?.onClickUndo()
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

   private fun requestPermission() {
      if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
         showRequestPermissionRationale("DrawingApp","Drawing App needs Permission"+
         "to access Your Storage for setting Background Image")
      }

      requestPermission.launch(arrayOf
         (Manifest.permission.READ_EXTERNAL_STORAGE))
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


   override fun onClick(view: View?) {

      when(view?.id){

         R.id.ib_brushSizeBtn -> {
            ibBrushSizeOnClick()
         }

         R.id.ib_colorSelectBtn -> {
            ibColorSelectListener()
         }

         R.id.ib_pickImageBtn -> {
            requestPermission()
         }

         R.id.ib_undoBtn -> {
            ibUndoOnClick()
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