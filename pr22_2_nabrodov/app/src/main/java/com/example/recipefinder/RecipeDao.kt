package com.example.recipefinder

import androidx.room.*
import androidx.room.Query

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val summary: String
)

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAll(): List<Recipe>

    @Insert
    fun insert(recipe: Recipe)

    @Update
    fun update(recipe: Recipe)

    @Delete
    fun delete(recipe: Recipe)

    @Query("DELETE FROM recipes")
    fun deleteAll()

    @Query("SELECT * FROM recipes WHERE title = :title LIMIT 1")
    fun getRecipeByTitle(title: String): Recipe?
}

@Database(entities = [Recipe::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}
