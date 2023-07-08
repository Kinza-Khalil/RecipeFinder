package com.example.recipefinder
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipefinder.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.Serializable

data class Recipe(
    val id: Int, // this is the recipe id, which we will use to retrieve detailed information
    val title: String,
    val image: String,
    val readyInMinutes: Int?,
    val servings: Int?,
    val instructions: String?
) : Serializable

data class RecipeDetails(
    val id: Int,
    val title: String,
    val image: String,
    val readyInMinutes: Int?,
    val servings: Int?,
    val instructions: String?
) : Serializable

interface RecipeAPI {
    @GET("recipes/findByIngredients")
    fun getRecipes(@Query("ingredients") ingredients: String, @Query("apiKey") apiKey: String): Call<List<Recipe>>

    @GET("recipes/{id}/information")
    fun getRecipeDetails(@Path("id") id: Int, @Query("apiKey") apiKey: String): Call<RecipeDetails>
}

class RecipesAdapter(private val recipes: List<Recipe>) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.recipeTitle)
        val image: ImageView = view.findViewById(R.id.recipeImage)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.title.text = recipe.title
        Glide.with(holder.itemView.context).load(recipe.image).into(holder.image)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RecipeDetailsActivity::class.java)
            intent.putExtra("recipe", recipes[position]) // Pass the recipe to the details activity
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = recipes.size
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.searchButton)
        val ingredientsInput = findViewById<EditText>(R.id.ingredientInput)
        val recyclerView = findViewById<RecyclerView>(R.id.recipeRecyclerView)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(RecipeAPI::class.java)

        searchButton.setOnClickListener {
            val ingredients = ingredientsInput.text.toString()

            api.getRecipes(ingredients, "6fce4620f437412b9198bcfbe4c8587a").enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    val recipes = response.body()

                    if (recipes != null) {
                        recyclerView.apply {
                            layoutManager = LinearLayoutManager(this@MainActivity)
                            adapter = RecipesAdapter(recipes)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "No recipes found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
