package com.example.yemek_tarifleri_sql_lite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream
import java.io.OutputStream


class TarifFragment : Fragment() {
    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val buttonKaydet = view.findViewById<Button>(R.id.buttonKaydet)
        buttonKaydet.setOnClickListener() {
            kaydet(it)
        }


        val imageView = view.findViewById<ImageView>(R.id.imageView) // Düzeltme burada
        imageView.setOnClickListener() {
            gorselSec(it)
        }

        arguments?.let {

            var gelenbilgi = TarifFragmentArgs.fromBundle(it)
            println("gelen bilgi" + gelenbilgi.toString())
            val yemekIsmiEditText = requireView().findViewById<EditText>(R.id.yemekIsmiEditText)
            val yemekMalzemeleriEditText =
                requireView().findViewById<EditText>(R.id.yemekMalzemeleri)


            if (gelenbilgi.bilgi.equals("menuden_geldim")) {
                // onViewCreated içinde EditText öğelerini al
                yemekIsmiEditText.setText("")
                yemekMalzemeleriEditText.setText("")
                buttonKaydet.visibility = View.VISIBLE


            } else {
                // kaydedilmiş yemeğe bakmaya geldi
                val textView = requireView().findViewById<TextView>(R.id.textView)
                textView.visibility=View.INVISIBLE
                buttonKaydet.visibility = View.INVISIBLE

                val secilenId = TarifFragmentArgs.fromBundle(it).id

                context?.let {
                    try {

                        val db = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery(
                            "SELECT * FROM yemekler WHERE id = ? ",
                            arrayOf(secilenId.toString())
                        )
                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekmalzemesi = cursor.getColumnIndex("yemekmalzemesi")
                        val gorsel = cursor.getColumnIndexOrThrow("gorsel")

                        while (cursor.moveToNext()) {
                            yemekIsmiEditText.setText(cursor.getString(yemekIsmiIndex))
                            yemekMalzemeleriEditText.setText(cursor.getString(yemekmalzemesi))
                            val byteDizisi = cursor.getBlob(gorsel)
                            val bitmap =
                                BitmapFactory.decodeByteArray(byteDizisi, 0, byteDizisi.size)
                            imageView.setImageBitmap(bitmap)

                        }
                        cursor.close()


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }


            }
        }


    }

    fun gorselSec(view: View) {

        activity?.let {

            if (ContextCompat.checkSelfPermission(
                    it.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

            } else {
                val galeriIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }
        val textView = requireView().findViewById<TextView>(R.id.textView)

        textView.visibility = View.INVISIBLE

    }

    fun kaydet(view: View) {

        // onViewCreated içinde EditText öğelerini al
        val yemekIsmiEditText = requireView().findViewById<EditText>(R.id.yemekIsmiEditText)
        val yemekMalzemeleriEditText = requireView().findViewById<EditText>(R.id.yemekMalzemeleri)

        // Aldığınız EditText öğelerini kullanabilirsiniz
        // Örneğin:
        val yemekIsmi = yemekIsmiEditText.text.toString()
        val yemekMalzemeleri = yemekMalzemeleriEditText.text.toString()





        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapmOlustur(secilenBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler(id INTEGER PRIMARY KEY,yemekismi VARCHAR,yemekmalzemesi VARCHAR,gorsel BLOB )")

                    val sqlString =
                        " INSERT INTO yemekler(yemekismi, yemekmalzemesi, gorseL) VALUES (?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, yemekIsmi)
                    statement.bindString(2, yemekMalzemeleri)
                    statement.bindBlob(3, byteDizisi)
                    statement.execute()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)


        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {

            if (grantResults.size < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //izin alındı
                val galeriIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)

            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK /*kullancı bir şey seçti mi onun kontrolu*/ && data != null) {

            secilenGorsel = data.data //uri veriyor
            try {
                context.let {

                    if (secilenGorsel != null) {
                        if (Build.VERSION.SDK_INT >= 28) {

                            val source = it?.let { it1 ->
                                ImageDecoder.createSource(
                                    it1.contentResolver,
                                    secilenGorsel!!
                                )
                            }
                            secilenBitmap = source?.let { it1 -> ImageDecoder.decodeBitmap(it1) }
                            view.let {

                                val imageView = it!!.findViewById<ImageView>(R.id.imageView)
                                imageView.setImageBitmap(secilenBitmap)
                            }
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                it!!.contentResolver,
                                secilenGorsel
                            )
                            view.let {

                                val imageView = it!!.findViewById<ImageView>(R.id.imageView)
                                imageView.setImageBitmap(secilenBitmap)
                            }

                        }


                    }

                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    fun kucukBitmapmOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int): Bitmap {

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani: Double = width.toDouble() / height.toDouble()
        if (bitmapOrani < 1) {
            //görsel yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani

            height = kisaltilmisHeight.toInt()

        } else {
            //görsel dikey
            height = maximumBoyut
            val kisaltilmisHeight = height * bitmapOrani
            width = kisaltilmisHeight.toInt()

        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width, height, true)
    }


}