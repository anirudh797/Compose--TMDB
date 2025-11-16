package com.example.atylsmovies.data.local

import android.content.Context
import com.example.atylsmovies.data.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MoviesCache(private val context: Context, private val gson: Gson = Gson()) {

    private val cacheFile: File by lazy {
        File(context.cacheDir, "trending_movies_cache.json")
    }

    fun save(movies: List<Movie>) {
        runCatching {
            val json = gson.toJson(movies)
            cacheFile.writeText(json)
        }
    }

    fun read(): List<Movie>? {
        return runCatching {
            if (!cacheFile.exists()) return null
            val json = cacheFile.readText()
            val type = object : TypeToken<List<Movie>>() {}.type
            gson.fromJson<List<Movie>>(json, type)
        }.getOrNull()
    }

    fun clear() {
        runCatching { if (cacheFile.exists()) cacheFile.delete() }
    }
}

