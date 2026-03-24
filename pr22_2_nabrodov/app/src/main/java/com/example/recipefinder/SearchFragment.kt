package com.example.recipefinder

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SearchFragment : Fragment(R.layout.fragment_search) {


    private val API_KEY = "9354d66a34814902bd63a46882e1d187"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        val searchBtn = view.findViewById<Button>(R.id.searchBtn)
        val apiResult = view.findViewById<TextView>(R.id.apiResult)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val favoriteBtn = view.findViewById<Button>(R.id.favoriteBtn)

        var lastRecipeTitle = ""

        searchBtn.setOnClickListener {
            val input = searchInput.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Введите запрос (например: pasta)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = "https://api.spoonacular.com/recipes/complexSearch?query=$input&apiKey=$API_KEY&number=1"

            val queue = Volley.newRequestQueue(requireContext())
            val request = StringRequest(Request.Method.GET, url, { response ->
                Log.d("DEBUG_API", response)

                val json = JSONObject(response)
                val results = json.getJSONArray("results")
                if (results.length() > 0) {
                    lastRecipeTitle = results.getJSONObject(0).getString("title")
                    apiResult.text = "Найдено: $lastRecipeTitle"
                    saveBtn.visibility = View.VISIBLE
                }
                else {
                    apiResult.text = "Ничего не найдено"
                    saveBtn.visibility = View.GONE
                }
            }, {
                apiResult.text = "Ошибка сети"
                saveBtn.visibility = View.GONE
            })

            queue.add(request)
        }

        saveBtn.setOnClickListener {
            if (lastRecipeTitle.isEmpty()) return@setOnClickListener

            val db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "recipe_db").build()

            Thread {

                val recipeExists = db.recipeDao().getRecipeByTitle(lastRecipeTitle)

                requireActivity().runOnUiThread {
                    if (recipeExists == null) {
                        Thread {
                            db.recipeDao().insert(Recipe(title = lastRecipeTitle, summary = "Вкусно и полезно"))
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Рецепт добавлен в избранное", Toast.LENGTH_SHORT).show()
                            }
                        }.start()
                    }
                    else {
                        Toast.makeText(requireContext(), "Этот рецепт уже есть в избранном", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        favoriteBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, FavoritesFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}