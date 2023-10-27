package com.mooncode.dailyquotes

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import quotesManager


// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM = "category"
class quote : Fragment() {
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(ARG_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quote, container, false)
        val categories = listOf(
            "change", "education", "faith", "family", "friendship", "funny", "happiness", "inspirational", "intelligence", "knowledge", "life", "love", "morning", "success"
        )

        val imgQuoteIcon = view.findViewById<ImageView>(R.id.quote_icon)
        val txtHeader = view.findViewById<TextView>(R.id.header)
        val txtAuthor = view.findViewById<TextView>(R.id.author)
        val txtQuote = view.findViewById<TextView>(R.id.quote)
        val htQuote = view.findViewById<LinearLayout>(R.id.quote_height)
        val contQuote = view.findViewById<ViewGroup>(R.id.quote_container)
        val btnBack = view.findViewById<Button>(R.id.back_button)
        val btnNext = view.findViewById<Button>(R.id.next_button)
        val btnFav = view.findViewById<MaterialButton>(R.id.favorite_button)

        txtHeader.text = category?.uppercase() ?: "QUOTE"
        Log.d("QuotesManager", "CATEGORY $category")



        val quotes = runBlocking {
            return@runBlocking withContext(Dispatchers.IO) {
                if (category in categories)
                    return@withContext quotesManager?.getQuotesByCategory(category ?: "")?.toMutableList() ?: mutableListOf()
                else
                    return@withContext quotesManager?.getFavorites()?.toMutableList() ?: mutableListOf()
            }
        }

        Log.d("Quotes", "SZ out: ${quotes!!.size}")


        imgQuoteIcon.setColorFilter(txtHeader.currentTextColor)
        var imgCategory = when (category) {
            "change" -> R.drawable.change
            "education" -> R.drawable.education
            "faith" -> R.drawable.faith
            "family" -> R.drawable.family
            "friendship" -> R.drawable.friendship
            "funny" -> R.drawable.funny
            "happiness" -> R.drawable.happiness
            "inspirational" -> R.drawable.inspirational
            "intelligence" -> R.drawable.intelligence
            "knowledge" -> R.drawable.knowledge
            "life" -> R.drawable.life
            "love" -> R.drawable.love
            "morning" -> R.drawable.morning
            "success" -> R.drawable.success
            "favorites" -> R.drawable.star_filled
            else -> R.drawable.quote
        }

        imgQuoteIcon.setImageResource(imgCategory)




        val displayMetrics = DisplayMetrics()
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels


        var curI = 0


        var button_clickable = true

        contQuote.visibility = View.VISIBLE
        contQuote.alpha = 0f
        txtQuote.textSize = 0F
        var isFav = false
        fun fadeNext() {

            button_clickable = false
            val maxHeight = screenHeight- 1200
            contQuote.visibility = View.VISIBLE
            contQuote.alpha = 0f
            txtQuote.textSize = 0F


            txtQuote.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                var textSz = 0F
                override fun onGlobalLayout() {
                    //Log.d("QuoteCategories", "MaxSz: $maxHeight, Height: ${txtQuote.height} ${htQuote.height}, TextSz: ${txtQuote.textSize}")

                    if (txtQuote.height < maxHeight - 300) {
                        textSz += 2F
                        txtQuote.textSize = textSz
                    } else {
                        ObjectAnimator.ofFloat(contQuote, View.ALPHA, 0f, 1f).apply{
                            duration = 100 // Animation duration in milliseconds
                            startDelay = 350
                            addListener(onEnd = {
                                button_clickable = true
                            })
                            start()
                        }
                        ObjectAnimator.ofFloat(contQuote, View.TRANSLATION_X, contQuote.width.toFloat(), 0F).apply{
                            duration = 500 // Animation duration in milliseconds
                            start()
                        }



                        contQuote.visibility = View.VISIBLE // Make sure the view is visible

                        val params = htQuote.layoutParams
                        params.height = maxHeight
                        htQuote.layoutParams = params
                        txtQuote.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                    //Log.d("QuoteCategories", "TextSz: ${txtQuote.textSize}")

                }

            })


            txtQuote.text = quotes[curI].quote
            txtAuthor.text = "— ${quotes[curI].author} —"

            if (category == "favorites") {
                txtHeader.text = quotes[curI].category
                btnFav.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.star_filled)
                isFav = true
            } else {
                btnFav.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.star)
                val favs = runBlocking {
                    return@runBlocking withContext(Dispatchers.IO) {
                        return@withContext quotesManager?.getFavorites()
                    }
                }
                for (cat in favs!!) {
                    if (quotes[curI].quote == cat.quote &&
                        quotes[curI].author == cat.author) {
                        isFav = true
                        btnFav.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.star_filled)
                        break
                    } else {
                        isFav = false
                        btnFav.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.star)
                    }
                }
            }
            curI = (curI+1)%quotes.size
        }

        if (quotes.isNotEmpty()) {
            fadeNext()
            Log.d("QuoteCategories", "curI: $curI, SZ: ${quotes.size}")
        }

        button_clickable = true

        btnNext.setOnClickListener {
            if (quotes.isNotEmpty())
                if (button_clickable) {
                    button_clickable = false
                    ObjectAnimator.ofFloat(contQuote, View.TRANSLATION_X, 0f, -contQuote.width.toFloat()).apply{
                        duration = 300 // Animation duration in milliseconds
                        addListener(onEnd = {
                            fadeNext()
                            contQuote.translationX = 0f
                        })
                        start()
                    }
                    ObjectAnimator.ofFloat(contQuote, View.ALPHA, 1f, 0f).apply{
                        duration = 100 // Animation duration in milliseconds
                        start()
                    }
                }


        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnFav.setOnClickListener {
            curI = Math.floorMod(--curI,quotes.size)
            Log.d("QuoteCategories", "curI: $curI, SZ: ${quotes.size}")
            if (isFav) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        quotesManager?.removeFromFavorites(quotes[curI])
                    }
                }


                quotes.removeAt(curI)
                if (category == "favorites") {
                    ObjectAnimator.ofFloat(contQuote, View.TRANSLATION_X, 0f, -contQuote.width.toFloat()).apply{
                        duration = 300 // Animation duration in milliseconds
                        addListener(onEnd = {
                            contQuote.translationX = 0f
                            if (quotes.isNotEmpty())
                                fadeNext()
                        })
                        start()
                    }
                    ObjectAnimator.ofFloat(contQuote, View.ALPHA, 1f, 0f).apply{
                        duration = 100 // Animation duration in milliseconds
                        start()
                    }
                } else {

                }



                curI = 0

                isFav = false
            } else {

                runBlocking {
                    withContext(Dispatchers.IO) {
                        quotesManager?.addToFavorites(quotes[curI].quote, quotes[curI].author)
                    }
                }

                isFav = true

            }



            // set icon image

            if (quotes.isEmpty()) {
                txtHeader.text = "FAVORITES"
                btnFav.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.star)
            }
            else {
                curI = Math.floorMod(++curI, quotes.size)
                if (isFav)
                    btnFav.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.star_filled)
                else
                    btnFav.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.star)
            }
        }



        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(category: String) =
            quote().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM, category)
                }
            }
    }
}