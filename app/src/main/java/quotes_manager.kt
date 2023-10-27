import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quotes")


class QuotesManager(private val activity: Activity) {
    private val api_key = "Y0LNR9RITDIc5E38/7qyKA==xTWNJzmsuP2jhTVE"
    private val api_url = "https://api.api-ninjas.com/v1/quotes"
    private val dataStore: DataStore<Preferences> = activity.applicationContext.dataStore
    private val categories = listOf(
        "change", "education", "faith", "family", "friendship", "funny", "happiness", "inspirational", "intelligence", "knowledge", "life", "love", "morning", "success"
    )


    init {
        // get the last_updated date from DataStore

        runBlocking {
            withContext(Dispatchers.IO) {
                // val preferences = dataStore.data.first().contains(stringPreferencesKey("last_updated"))
                val currentDt = SimpleDateFormat("yyyy-MM-dd").format(Date())
                val lastUpdated = dataStore.data.first()[stringPreferencesKey("last_updated")]

                Log.d("QuotesManager", "Last updated: $lastUpdated")

                if (!lastUpdated.equals(currentDt) || lastUpdated == null) {
                    Log.d("QuotesManager", "Updating quotes...")
                    try {
                        dataStore.edit { preferences ->
                            Log.d("QuotesManager", "Connecting to API..." )
                            val url = URL("$api_url?category=${categories.random()}")
                            val urlConnection = url.openConnection() as HttpURLConnection
                            urlConnection.setRequestProperty("X-Api-Key", api_key)


                            val responseCode = urlConnection.responseCode
                            Log.d("QuotesManager", "Connected to the API" )

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                val inputStream = urlConnection.inputStream
                                val reader = BufferedReader(InputStreamReader(inputStream))

                                Log.d("QuotesManager", "Quote of the day acquired" )

                                // Parse the JSON response and store quotes in DataStore
                                preferences[stringPreferencesKey("quote_of_the_day")] = reader.readText()

                            } else {
                                Log.e("QuotesManager", "API request failed with error code: $responseCode")
                                throw Exception("API request failed with error code: $responseCode")
                            }

                            urlConnection.disconnect()



                            // Make 15 API requests and populate the DataStore
                            for (category in categories) {
                                val url = URL("$api_url?category=$category&limit=10")
                                val urlConnection = url.openConnection() as HttpURLConnection
                                urlConnection.setRequestProperty("X-Api-Key", api_key)


                                val responseCode = urlConnection.responseCode
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    val inputStream = urlConnection.inputStream
                                    val reader = BufferedReader(InputStreamReader(inputStream))

                                    Log.d("QuotesManager", "Quote list \"$category\" acquired" )

                                    // Parse the JSON response and store quotes in DataStore
                                    preferences[stringPreferencesKey("quotes_${category}")] = reader.readText()
                                } else {
                                    throw Exception("API request failed with error code: $responseCode")
                                }
                                Log.d("QuotesManager", "Quote data stored successfully!" )
                                urlConnection.disconnect()

                            }
                            Log.d("QuotesManager", "Set the last update date" )
                            preferences[stringPreferencesKey("last_updated")] = currentDt
                        }
                    } catch (e: Exception) {
                        showErrorDialog(e.message)
                    }
                }
            }
        }
    }

    private fun showErrorDialog(message: String?) {
        activity.runOnUiThread {
            MaterialAlertDialogBuilder(activity)
                .setTitle("Error")
                .setMessage("API request failed with error: $message")
                .setNegativeButton("Leave") { dialog, which ->
                    activity.finishAndRemoveTask()
                }
                .setPositiveButton("Retry") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    suspend fun getQuotesByCategory(category: String): List<Quote> {
        val quotesJson = dataStore.data.first()[stringPreferencesKey("quotes_$category")]
        Log.d("QuotesManager", "Collected quotes data: $quotesJson")
        val quotesList = mutableListOf<Quote>()
        if (quotesJson != null) {
            val jsonArray = JSONArray(quotesJson)
            Log.d("QuotesManager", "Quotes size: ${jsonArray.length()}")
            for (i in 0 until jsonArray.length()) {
                val jsonQuote = jsonArray.getJSONObject(i)
                val author = jsonQuote.getString("author")
                val quote = jsonQuote.getString("quote")
                quotesList.add(Quote(category, author, quote))
                Log.d("QuotesManager", "Parsing quote at $i")

            }
        }
        return quotesList
    }

    suspend fun addToFavorites(quote: String, author: String){
        val favoritesKey = stringPreferencesKey("favorites")
        Log.d("QuotesManager", "Adding quote: $quote, $author")
        dataStore.edit { preferences ->
            val favoritesJson = preferences[favoritesKey]
            val jsonArray = if (favoritesJson != null) JSONArray(favoritesJson) else JSONArray()
            for (i in 0 until jsonArray.length()) {
                val jsonQuote = jsonArray.getJSONObject(i)
                if (jsonQuote.getString("quote") == quote &&
                    jsonQuote.getString("author") == author)
                    return@edit
            }
            val quoteJson = JSONObject()
            quoteJson.put("author", author)
            quoteJson.put("quote", quote)
            quoteJson.put("category", SimpleDateFormat("MMMM dd, yyyy hh:mma", Locale.US).format(Date()))
            jsonArray.put(quoteJson)
            val currentFavorites = jsonArray.toString()
            preferences[favoritesKey] = currentFavorites
        }
    }

    fun addToFavorites(quote: Quote){
        addToFavorites(Quote(quote.category, quote.author, quote.quote))
    }

    suspend fun removeFromFavorites(category: String, quote: String, author: String){
        val favoritesKey = stringPreferencesKey("favorites")
        Log.d("QuotesManager", "Removing quote: $quote, $author")
        dataStore.edit { preferences ->
            val favoritesJson = preferences[favoritesKey]
            val jsonArray = if (favoritesJson != null) JSONArray(favoritesJson) else JSONArray()
            for (i in 0 until jsonArray.length()) {
                val jsonQuote = jsonArray.getJSONObject(i)
                Log.d("QuotesManager", "at index $i")
                if (jsonQuote.getString("quote") == quote &&
                    jsonQuote.getString("author") == author) {
                    Log.d("QuotesManager", "Removing quote: $jsonQuote, at index $i")
                    jsonArray.remove(i)
                    break
                }
            }
            val currentFavorites = jsonArray.toString()
            preferences[favoritesKey] = currentFavorites
        }
    }

    suspend fun removeFromFavorites(quote: Quote){
        removeFromFavorites(quote.category, quote.quote, quote.author)
    }

    suspend fun getFavorites(): List<Quote> {
        val quotesJson = dataStore.data.first()[stringPreferencesKey("favorites")]
        //Log.d("QuotesManager", "Collected quotes data: $quotesJson")
        val favoritesList = mutableListOf<Quote>()
        if (quotesJson != null) {
            val jsonArray = JSONArray(quotesJson)
            //Log.d("QuotesManager", "Quotes size: ${jsonArray.length()}")
            for (i in 0 until jsonArray.length()) {
                val jsonQuote = jsonArray.getJSONObject(i)
                val author = jsonQuote.getString("author")
                val quote = jsonQuote.getString("quote")
                val category = jsonQuote.getString("category")
                favoritesList.add(Quote(category, author, quote))
                //Log.d("QuotesManager", "Parsing quote at $i")

            }
        }
        return favoritesList
    }

    suspend fun getQuoteOfTheDay(): Quote? {
        try {
            val jsonQuote = dataStore.data.first()[stringPreferencesKey("quote_of_the_day")]
            Log.d("QuotesManager", "Collected quote of the day data: $jsonQuote")
            val jsonArray = JSONArray(jsonQuote).getJSONObject(0)
            if (jsonArray != null ) {
                val author = jsonArray.getString("author")
                val quote = jsonArray.getString("quote")
                val category = jsonArray.getString("category")
                return Quote(category, author, quote)
            }
        } catch (e: Exception) {
            showErrorDialog(e.message)
        }
        return null


    }


}

var quotesManager: QuotesManager? = null

class Quote(
    val category: String,
    val author: String,
    val quote: String
) {

    private val categories = listOf(
        "change", "education", "faith", "family", "friendship", "funny", "happiness", "inspirational", "intelligence", "knowledge", "life", "love", "morning", "success"
    )


    fun isFavorite(): Boolean {
        return category in categories
    }

    override fun toString(): String {
        return "$quote\n\n$author"
    }
}
