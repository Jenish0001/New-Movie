package com.om.mymovie

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.om.mymovie.activity.DetailsOfMovieActivity
import com.om.mymovie.ads.Google_inter_ads
import com.om.mymovie.databinding.LayoutOfMovieAdpterLayoutBinding
import com.om.mymovie.dialog.AdsCallBack
import com.om.mymovie.fragment.MovieFragment

class MovieAdapter : RecyclerView.Adapter<MovieAdapter.ViewData>() {

    var list = mutableListOf<ResultsItem?>()

    inner class ViewData(var binding: LayoutOfMovieAdpterLayoutBinding) : ViewHolder(binding.root) {
        fun bind(movieDetails: ResultsItem) = with(binding) {

            Glide.with(itemView.context)
                .load("https://image.tmdb.org/t/p/w500${movieDetails.posterPath}")
                .into(ivImage)

            tvRelese.text = movieDetails.releaseDate!!.substring(0, 4)
            tvTitle.text = movieDetails.title

            root.setOnClickListener {
                Google_inter_ads.googleinter_show_GoogleInterAds(
                    itemView.context as Activity,
                    "MainAct",
                    object : AdsCallBack {
                        override fun adsCallBack() {
                            val inn = Intent(itemView.context, DetailsOfMovieActivity::class.java)
                            inn.putExtra("movieData", movieDetails)
                            itemView.context.startActivity(inn)
                        }
                    },
                    true
                )

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewData {
        return ViewData(
            LayoutOfMovieAdpterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewData, position: Int) {
        holder.bind(list[position]!!)
    }


    fun submitList(newList: MutableList<ResultsItem?>?) {
        val diffResult = DiffUtil.calculateDiff(MovieDiffCallback(list, newList!!))
        list = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun addItems(addList: MutableList<ResultsItem?>?) {
        addList?.let {
            val startPosition = list.size
            list.addAll(addList)
            notifyItemRangeInserted(startPosition, addList.size)
            isEmptyView()
        }
    }

    fun isEmptyView() {
        if (list.isEmpty()) {
            MovieFragment.isEmptyView?.visibility = View.VISIBLE
        } else {
            MovieFragment.isEmptyView?.visibility = View.GONE
        }
    }
}

class MovieDiffCallback(
    private val oldList: List<ResultsItem?>,
    private val newList: List<ResultsItem?>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]?.id == newList[newItemPosition]?.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}