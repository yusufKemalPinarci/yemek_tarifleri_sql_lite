package com.example.yemek_tarifleri_sql_lite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class ListeRecyclerAdapter (val yemekListesi:ArrayList<String>,val idListesi: ArrayList<Int>):RecyclerView.Adapter<ListeRecyclerAdapter.YemekHolder>() {


    class YemekHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view =inflater.inflate(R.layout.recyle_row,parent,false)
        return YemekHolder(view)
    }

    override fun getItemCount(): Int {
        return  yemekListesi.size
    }

    override fun onBindViewHolder(holder: YemekHolder, position: Int) {

        holder.itemView.findViewById<TextView>(R.id.recycler_row_text)?.text = yemekListesi[position]

        val yemek = yemekListesi[position]
        val id = idListesi[position]
        holder.itemView.setOnClickListener{

            val action= ListeFragmentDirections.actionListeFragmentToTarifFragment()
            action.bilgi = yemek
            action.id = id
            Navigation.findNavController(it).navigate(action)

        }

    }

}