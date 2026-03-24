package com.example.recipefinder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.room.Room

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeList = view.findViewById<TextView>(R.id.recipeList)
        val clearBtn = view.findViewById<Button>(R.id.clearBtn)
        val db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "recipe_db").build()

        Thread {
            val recipes = db.recipeDao().getAll()
            val sb = StringBuilder()
            recipes.forEach { sb.append("${it.id}. ${it.title}\n") }
            requireActivity().runOnUiThread {
                recipeList.text = if (sb.isEmpty()) "База пуста" else sb.toString()
            }
        }.start()

        clearBtn.setOnClickListener {
            Thread {
                db.recipeDao().deleteAll()
                requireActivity().runOnUiThread {
                    recipeList.text = "База очищена"
                }
            }.start()
        }
    }
}