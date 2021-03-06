package com.remotejobs.io.app.home.view

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.remotejobs.io.app.data.database.dao.FavoritesDao
import com.remotejobs.io.app.R
import com.remotejobs.io.app.model.Favorite
import com.remotejobs.io.app.utils.extension.hideSoftKeyboard
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

/**
 * Created by tairo on 1/13/18 5:30 PM.
 */
class FavoriteAdapter(
    private val favorites: MutableList<String>,
    private val favoritesDao: FavoritesDao
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.item_search, parent, false)
        }

        val textViewJob = view?.findViewById<TextView>(R.id.textViewJob)
        val imgFavorite = view?.findViewById<ImageView>(R.id.imgFavorite)
        textViewJob?.text = getItem(position)

        imgFavorite?.setOnClickListener { _ ->
            (view?.context as Activity).hideSoftKeyboard()
            view.context.alert {
                title = "Favorites"
                message = "Do you whant remove this job from favorites?"

                positiveButton("Remove") {
                    doAsync { favoritesDao.delete(Favorite(getItem(position))) }
                    imgFavorite.setImageDrawable(
                        ContextCompat.getDrawable(
                            imgFavorite.context,
                            R.drawable.ic_star_border
                        )
                    )
                    view.context.toast("Success :)")
                    favorites.remove(getItem(position))
                    notifyDataSetChanged()

                    (view.context as FavoritesActivity).showMessageNoDataFound(GONE)
                    if (favorites.isEmpty()) {
                        (view.context as FavoritesActivity).showMessageNoDataFound(VISIBLE)
                    }

                    it.dismiss()
                }
                negativeButton("Cancel") { it.dismiss() }
            }.show()
        }

        doAsync {
            val favorite = favoritesDao.getByName(getItem(position))
            val drawable = if (favorite.name.isEmpty()) {
                ContextCompat.getDrawable(view?.context as Context, R.drawable.ic_star_border)
            } else {
                ContextCompat.getDrawable(view?.context as Context, R.drawable.ic_star_yellow)
            }

            uiThread { imgFavorite?.setImageDrawable(drawable) }
        }

        return view
    }

    override fun getItem(position: Int): String {
        return favorites[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return favorites.size
    }

    fun updateItems(jobs: List<String>) {
        this.favorites.clear()
        this.favorites.addAll(jobs)
        notifyDataSetChanged()
    }
}