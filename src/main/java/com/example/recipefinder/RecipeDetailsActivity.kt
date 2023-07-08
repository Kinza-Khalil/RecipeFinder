package com.example.recipefinder

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable

class RecipeDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        val recipe: Recipe? = intent.getSerializableExtra("recipe") as? Recipe

        recipe?.let {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(RecipeAPI::class.java)

            api.getRecipeDetails(it.id, "6fce4620f437412b9198bcfbe4c8587a").enqueue(object : Callback<RecipeDetails> {
                override fun onResponse(call: Call<RecipeDetails>, response: Response<RecipeDetails>) {
                    val recipeDetails = response.body()

                    if (recipeDetails != null) {
                        Glide.with(this@RecipeDetailsActivity).load(it.image).into(findViewById<ImageView>(R.id.recipeImage))
                        findViewById<TextView>(R.id.recipeTitle).text = recipeDetails.title
                        findViewById<TextView>(R.id.readyInMinutes).text = "Ready in minutes: ${recipeDetails.readyInMinutes?.toString()}"
                        findViewById<TextView>(R.id.servings).text = "Servings: ${recipeDetails.servings?.toString()}"
                        findViewById<TextView>(R.id.instructions).text = recipeDetails.instructions
                        Log.d("RecipeFinder", "Recipe: $recipe")

                    } else {
                        Toast.makeText(this@RecipeDetailsActivity, "No recipe details found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RecipeDetails>, t: Throwable) {
                    Toast.makeText(this@RecipeDetailsActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
