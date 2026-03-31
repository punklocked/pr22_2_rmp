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
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
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

        var lastRecipeId = 0
        var lastRecipeTitle = ""
        var lastRecipeInstructions = ""

        fun getRecipeInstructions(recipeId: Int, queue: RequestQueue) {
            val instructionsUrl = "https://api.spoonacular.com/recipes/$recipeId/analyzedInstructions?apiKey=$API_KEY"

            val instructionsRequest = StringRequest(Request.Method.GET, instructionsUrl, { response ->
                Log.d("DEBUG_INSTRUCTIONS", response)

                val instructionsArray = JSONArray(response)
                val instructions = StringBuilder()

                if (instructionsArray.length() > 0) {
                    val steps = instructionsArray.getJSONObject(0).getJSONArray("steps")
                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        instructions.append("${step.getInt("number")}. ${step.getString("step")}\n")
                    }
                    lastRecipeInstructions = instructions.toString()

                    apiResult.text = "Найдено: $lastRecipeTitle\n\nИнструкция:\n${lastRecipeInstructions.take(200)}..."
                    saveBtn.visibility = View.VISIBLE
                } else {
                    lastRecipeInstructions = "Инструкция не найдена"
                    apiResult.text = "Найдено: $lastRecipeTitle\n\nИнструкция не доступна"
                    saveBtn.visibility = View.VISIBLE
                }
            }, {
                lastRecipeInstructions = "Ошибка загрузки инструкции"
                apiResult.text = "Найдено: $lastRecipeTitle\n\nОшибка загрузки инструкции"
                saveBtn.visibility = View.VISIBLE
            })

            queue.add(instructionsRequest)
        }

        searchBtn.setOnClickListener {
            val input = searchInput.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Введите запрос (например: pasta)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = "https://api.spoonacular.com/recipes/complexSearch?query=$input&apiKey=$API_KEY&number=1&instructionsRequired=true"

            val queue = Volley.newRequestQueue(requireContext())
            val request = StringRequest(Request.Method.GET, url, { response ->
                Log.d("DEBUG_API", response)

                val json = JSONObject(response)
                val results = json.getJSONArray("results")
                if (results.length() > 0) {
                    val recipe = results.getJSONObject(0)
                    lastRecipeId = recipe.getInt("id")
                    lastRecipeTitle = recipe.getString("title")

                    getRecipeInstructions(lastRecipeId, queue)
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
            if (lastRecipeTitle.isEmpty() || lastRecipeInstructions.isEmpty()) return@setOnClickListener

            val db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "recipe_db").build()

            Thread {
                val recipeExists = db.recipeDao().getRecipeByTitle(lastRecipeTitle)

                requireActivity().runOnUiThread {
                    if (recipeExists == null) {
                        Thread {
                            db.recipeDao().insert(Recipe(title = lastRecipeTitle, instructions = lastRecipeInstructions))
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