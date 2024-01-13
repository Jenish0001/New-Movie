package com.om.mymovie

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseMovie(

	@field:SerializedName("totalData")
	val totalData: Int? = null,

	@field:SerializedName("totalPage")
	val totalPage: Int? = null,

	@field:SerializedName("currentPageData")
	val currentPageData: Int? = null,

	@field:SerializedName("nextPage")
	val nextPage: Int? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("prevPage")
	val prevPage: Int? = null,

	@field:SerializedName("currentPage")
	val currentPage: Int? = null,

	@field:SerializedName("results")
	val results: List<ResultsItem?>? = null
)

data class ResultsItem(

	@field:SerializedName("backdrop_path")
	val backdropPath: String? = null,

	@field:SerializedName("overview")
	val overview: String? = null,

	@field:SerializedName("original_title")
	val originalTitle: String? = null,

	@field:SerializedName("release_date")
	val releaseDate: String? = null,

	@field:SerializedName("imdb_id")
	val imdbId: String? = null,

	@field:SerializedName("video_path")
	val videoPath: String? = null,

	@field:SerializedName("__v")
	val v: Int? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("poster_path")
	val posterPath: String? = null
) : Serializable
