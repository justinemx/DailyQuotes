package com.mooncode.dailyquotes

import Quote
import QuotesManager
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import quotesManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [quote_categories.newInstance] factory method to
 * create an instance of this fragment.
 */
class quote_categories : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_quote_categories, container, false)
        val iconQuote = view.findViewById<ImageView>(R.id.quote_icon)

        val scrollView = view.findViewById<ScrollView>(R.id.scroll_view)
        val contQuoteOfTheDay = view.findViewById<ViewGroup>(R.id.quote_container)
        val txtQuoteOfTheDay = view.findViewById<TextView>(R.id.quoteOfTheDay)
        val txtAuthorOfTheDay = view.findViewById<TextView>(R.id.authorOfTheDay)
        val btnDown = view.findViewById<Button>(R.id.down_button)
        val btnChange = view.findViewById<Button>(R.id.change_button)
        val btnEducation = view.findViewById<Button>(R.id.education_button)
        val btnFaith = view.findViewById<Button>(R.id.faith_button)
        val btnFamily = view.findViewById<Button>(R.id.family_button)
        val btnFriendship = view.findViewById<Button>(R.id.friendship_button)
        val btnFunny = view.findViewById<Button>(R.id.funny_button)
        val btnHappiness = view.findViewById<Button>(R.id.happiness_button)
        val btnInspirational = view.findViewById<Button>(R.id.inspirational_button)
        val btnIntelligence = view.findViewById<Button>(R.id.intelligence_button)
        val btnKnowledge = view.findViewById<Button>(R.id.knowledge_button)
        val btnLife = view.findViewById<Button>(R.id.life_button)
        val btnLove = view.findViewById<Button>(R.id.love_button)
        val btnMorning = view.findViewById<Button>(R.id.morning_button)
        val btnSuccess = view.findViewById<Button>(R.id.success_button)
        val btnFavorites = view.findViewById<Button>(R.id.favorites_button)


        btnChange.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "change")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnEducation.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "education")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnFaith.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "faith")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnFamily.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "family")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnFriendship.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "friendship")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnFunny.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "funny")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnHappiness.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "happiness")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnInspirational.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "inspirational")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnIntelligence.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "intelligence")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnKnowledge.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "knowledge")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnLife.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "life")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnLove.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "love")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnMorning.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "morning")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnSuccess.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "success")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        btnFavorites.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("category", "favorites")
            findNavController().navigate(R.id.action_quote_categories_to_quote, bundle)
        }

        iconQuote.setColorFilter(txtQuoteOfTheDay.currentTextColor)


        val displayMetrics = DisplayMetrics()
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels


        val maxHeight = screenHeight - 1000
        contQuoteOfTheDay.visibility = View.VISIBLE
        contQuoteOfTheDay.alpha = 0f

        txtQuoteOfTheDay.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            var textSz = 0F
            override fun onGlobalLayout() {
                //Log.d("QuoteCategories", "MaxSz: $maxHeight, Height: ${txtQuoteOfTheDay.height}, TextSz: ${txtQuoteOfTheDay.textSize}")

                 if (txtQuoteOfTheDay.height < maxHeight - 300) {
                    textSz += 2F
                    txtQuoteOfTheDay.textSize = textSz
                } else {


                     ObjectAnimator.ofFloat(contQuoteOfTheDay, View.ALPHA, 0f, 1f).apply{
                         duration = 400 // Animation duration in milliseconds
                         //startDelay = 600
                         start()
                     }
                     /*

                     ObjectAnimator.ofFloat(contQuoteOfTheDay, View.TRANSLATION_Y, contQuoteOfTheDay.height.toFloat()/10, 0F).apply{
                         duration = 1000 // Animation duration in milliseconds
                         start()
                     }

                      */

                    contQuoteOfTheDay.visibility = View.VISIBLE // Make sure the view is visible


                    txtQuoteOfTheDay.height = maxHeight
                    txtQuoteOfTheDay.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                //Log.d("QuoteCategories", "TextSz: ${txtQuoteOfTheDay.textSize}")

            }
        })



        btnDown.setOnClickListener {


            // Create a ValueAnimator to animate the scroll
            val animator = ValueAnimator.ofInt(scrollView.scrollY, screenHeight - 100)
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                scrollView.scrollTo(0, animatedValue)
            }
            animator.duration = 500 // Set the duration of the animation in milliseconds
            animator.start()
        }

        val quoteToday = runBlocking {
            return@runBlocking withContext(Dispatchers.IO) {
                return@withContext quotesManager?.getQuoteOfTheDay()
            }
        }

        if (quoteToday != null) {
            txtQuoteOfTheDay.text = quoteToday.quote
            txtAuthorOfTheDay.text = "— ${quoteToday.author} —"
        } else {
            txtQuoteOfTheDay.text = ""
            txtAuthorOfTheDay.text = ""
        }


        return view
    }

}