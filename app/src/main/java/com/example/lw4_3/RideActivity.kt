package com.example.lw4_3

import SwipeHelper
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lw4_3.databinding.ActivityRideBinding
import com.example.lw4_3.domain.Ride
import com.example.lw4_3.domain.RideListener
import com.example.lw4_3.domain.RideMockRepository
import kotlin.properties.Delegates
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfDocument
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import com.itextpdf.text.pdf.parser.TextExtractionStrategy
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.io.FileWriter
import java.io.OutputStream
import java.sql.ResultSet
import java.util.Objects

class RideActivity : AppCompatActivity() {
    private var _binding: ActivityRideBinding? = null
    val binding
        get() = _binding?: throw IllegalStateException("No binding!")
    private lateinit var adapter: RideAdapter // Объект Adapter
    private val repository: RideMockRepository = App.rideRepository
    private val listener: RideListener = {adapter.data = it}
    private lateinit var login: String
    private lateinit var distance: String
    private lateinit var type: String
    private var pos: Int = 0
    @RequiresApi(Build.VERSION_CODES.O)
    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            intent = result.data
            login = intent.getStringExtra("LOGIN").toString()
            distance = intent.getStringExtra("DISTANCE").toString()
            if (type == "EDIT"){
                repository.editRide(repository.getRides().get(pos), login, distance)

                var file = binding.root.context.filesDir.path + "/Rides.csv"
                var writer = Files.newBufferedWriter(Paths.get(file))
                var csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("ID", "Login", "Distance"))
                //csvPrinter.printRecord("ID", "Login", "Distance")
                for (el in repository.getRides()){
                    csvPrinter.printRecord(el.id, el.login, el.distance)
                }
                csvPrinter.flush()
            }else if (type == "ADD"){
                repository.createRide(login, distance)

                var file = binding.root.context.filesDir.path + "/Rides.csv"
                var writer = FileWriter(file, true)
                var csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
                //csvPrinter.printRecord("ID", "Login", "Distance")
                csvPrinter.printRecord(repository.getRides().size, login, distance)
                csvPrinter.flush()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSavePDF.setOnClickListener({
            var document = Document();
            var file = binding.root.context.filesDir.path + "/Rides.pdf"
            PdfWriter.getInstance(document, FileOutputStream(file));
            document.open();
            var i = 0;
            for (el in repository.getRides()){
                var p = Paragraph(i.toString() + " " + el.id + " " + el.login + " " + el.distance)
                document.add(p)
                i++
            }
            document.close()
        })

        binding.btnReadPDF.setOnClickListener({
            var file = binding.root.context.filesDir.path + "/Rides.pdf"
            var pdfReader = PdfReader(file)
            repository.clearRides()
            for (i in 1 .. pdfReader.numberOfPages){
                var strategy = SimpleTextExtractionStrategy()
                var text = PdfTextExtractor.getTextFromPage(pdfReader, i, strategy)
                var list = text.split('\n')
                for (el in list){
                    var listObj = el.split(' ')
                    repository.createRide(listObj[2].toString(), listObj[3].toString())
                }
            }
            pdfReader.close()
        })

        binding.btnSaveCSV.setOnClickListener({
            var file = binding.root.context.filesDir.path + "/Rides.csv"
            var writer = Files.newBufferedWriter(Paths.get(file))
            var csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("ID", "Login", "Distance"))
            //csvPrinter.printRecord("ID", "Login", "Distance")
            for (el in repository.getRides()){
                csvPrinter.printRecord(el.id, el.login, el.distance)
            }
            csvPrinter.flush()
        })

        binding.btnReadSCV.setOnClickListener({
            var file = binding.root.context.filesDir.path + "/Rides.csv"
            var reader = Files.newBufferedReader(Paths.get(file))
            var csvParser = CSVParser(reader, CSVFormat.DEFAULT)
            var text = csvParser.records
            for (i in 1 .. text.size-1){
                Log.i(i.toString(), text.get(i).toString())
            }
        })
        var iv : ImageView = binding.ivphoto
        binding.btnPhoto.setOnClickListener({
            var bitmapDrawable = iv.drawable
            var bitmap = bitmapDrawable.toBitmap()
            saveImageToGallery(bitmap)
            //dispatchTakePictureIntent()
        })

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) // LayoutManager
        adapter = RideAdapter(object : RideActionListener { // Создание объекта
            override fun onRideGetId(ride: Ride) =
                Toast.makeText(this@RideActivity, "Rides ID: ${ride.id}", Toast.LENGTH_SHORT).show()

            override fun onRideAdd(login: String, distance: String) = repository.createRide(login, distance)

            override fun onRideRemove(ride: Ride) = repository.removeRide(ride)

            override fun onRideEdit(ride: Ride, login: String, distance: String) = repository.editRide(ride, login, distance)

        }) // Создание объекта
        adapter.data = repository.getRides() // Заполнение данными

        binding.recyclerView.layoutManager = manager // Назначение LayoutManager для RecyclerView
        binding.recyclerView.adapter = adapter // Назначение адаптера для RecyclerView
        repository.addListener(listener)

        val swipeHelper = object : SwipeHelper(binding.recyclerView) {
            override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
                return listOf(
                    UnderlayButton(
                        this@RideActivity,
                        "Edit",
                        14f,
                        android.R.color.holo_blue_light,
                        R.drawable.icon_edit,
                        10,
                        object : UnderlayButtonClickListener {
                            override fun onClick() {
                                pos = position
                                type = "EDIT"
                                var ride = repository.getRides().get(pos)
                                var intent = Intent(binding.root.context, EditAddRideActivity::class.java)
                                intent.putExtra("LoginIN", ride.login)
                                intent.putExtra("DistanceIn", ride.distance)
                                startForResult.launch(intent)
                            }
                        }
                    ),
                    UnderlayButton(
                        this@RideActivity,
                        "Add",
                        14f,
                        android.R.color.holo_green_light,
                        R.drawable.icon_add,
                        10,
                        object : UnderlayButtonClickListener {
                            override fun onClick() {
                                type = "ADD"
                                var intent = Intent(binding.root.context, EditAddRideActivity::class.java)
                                startForResult.launch(intent)
                            }
                        }
                    )
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        var os : OutputStream
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            var resolver = contentResolver
            var contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + ".jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "TestFolder")
            var imageuri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            os = (Objects.requireNonNull(imageuri)?.let { resolver.openOutputStream(it) }) as OutputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            Objects.requireNonNull(os)
        }
    }

    private val REQUEST_IMAGE_CAPTURE = 1
    private fun dispatchTakePictureIntent(){
        var tekePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (tekePictureIntent.resolveActivity(packageManager) != null){
            startActivityForResult(tekePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }
}