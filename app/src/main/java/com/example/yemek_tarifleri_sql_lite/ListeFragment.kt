package com.example.yemek_tarifleri_sql_lite

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter


class ListeFragment : Fragment() {
    var yemekIsmiListesi= ArrayList<String>()
    var yemekIdListesi=ArrayList<Int>()

    private lateinit var listeAdapter: ListeRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liste, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // RecyclerView'ı bulma
        val recyclerView = view.findViewById<RecyclerView>(R.id.ReycylerView)

        // Liste adaptörünü oluşturma
        listeAdapter = ListeRecyclerAdapter(yemekIsmiListesi, yemekIdListesi)

        // RecyclerView'ın düzen yöneticisini ayarlama
        recyclerView.layoutManager = LinearLayoutManager(context)

        // RecyclerView'a adaptörü ayarlama
        recyclerView.adapter = listeAdapter


        sqlVeriAlma()
    }


    fun sqlVeriAlma(){
        try {
            activity?.let {
                val datbase=it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                val cursor =datbase.rawQuery("SELECT * FROM yemekler",null)
                val yemekIsmiIndex=cursor.getColumnIndex("yemekismi")
                val yemekIdIndex=cursor.getColumnIndex("id")

                yemekIsmiListesi.clear()
                yemekIdListesi.clear()

                while(cursor.moveToNext()){
                    yemekIsmiListesi.add(cursor.getString(yemekIsmiIndex))
                    yemekIdListesi.add(cursor.getInt(yemekIdIndex))
                }

                listeAdapter.notifyDataSetChanged()
                cursor.close()
            }
        }
        catch (e :Exception){
            println(e.message)
        }

    }


}