package com.example.recipefinder

import RecipeAdapter
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var clearBtn: Button
    private lateinit var db: AppDatabase
    private var recipes = listOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        clearBtn = view.findViewById(R.id.clearBtn)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = RecipeAdapter(
            recipes = recipes,
            onItemClick = { recipe -> showRecipeDetails(recipe) },
            onItemLongClick = { recipe -> showEditDialog(recipe) }
        )

        recyclerView.adapter = adapter

        db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "recipe_db").build()

        loadRecipes()

        clearBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Очистить всё")
                .setMessage("Вы уверены, что хотите удалить все рецепты?")
                .setPositiveButton("Да") { _, _ ->
                    Thread {
                        db.recipeDao().deleteAll()
                        requireActivity().runOnUiThread {
                            loadRecipes()
                            Toast.makeText(requireContext(), "Все рецепты удалены", Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                }
                .setNegativeButton("Нет", null)
                .show()
        }
    }

    private fun loadRecipes() {
        Thread {
            recipes = db.recipeDao().getAll()
            requireActivity().runOnUiThread {
                adapter.updateRecipes(recipes)
            }
        }.start()
    }

    private fun showRecipeDetails(recipe: Recipe) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_recipe, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
        val editInstructions = dialogView.findViewById<EditText>(R.id.editInstructions)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)

        dialogTitle.text = "Просмотр рецепта"
        editTitle.setText(recipe.title)
        editInstructions.setText(recipe.instructions)

        // Делаем поля только для чтения для просмотра
        editTitle.isEnabled = false
        editInstructions.isEnabled = false
        btnSave.visibility = View.GONE

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.text = "Закрыть"
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showEditDialog(recipe: Recipe) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_recipe, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
        val editInstructions = dialogView.findViewById<EditText>(R.id.editInstructions)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)

        dialogTitle.text = "Редактирование рецепта"
        editTitle.setText(recipe.title)
        editInstructions.setText(recipe.instructions)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val newTitle = editTitle.text.toString().trim()
            val newInstructions = editInstructions.text.toString().trim()

            if (newTitle.isEmpty()) {
                Toast.makeText(requireContext(), "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newInstructions.isEmpty()) {
                Toast.makeText(requireContext(), "Инструкция не может быть пустой", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Обновляем рецепт в базе данных
            Thread {
                val updatedRecipe = recipe.copy(
                    title = newTitle,
                    instructions = newInstructions
                )
                db.recipeDao().update(updatedRecipe)

                requireActivity().runOnUiThread {
                    loadRecipes()
                    Toast.makeText(requireContext(), "Рецепт обновлен", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }.start()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}