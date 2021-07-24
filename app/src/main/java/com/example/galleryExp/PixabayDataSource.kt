package com.example.galleryExp

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

enum class NetworkStatus {
    INITIAL_LOADING, LOADING, FAILED, COMPLETED, LOADED
}

class PixabayDataSource(private val context: Context) : PageKeyedDataSource<Int, PhotoItem>() {

    var retry: (() -> Any)? = null
    private val _networkStatus = MutableLiveData<NetworkStatus>()
    val networkStatus: LiveData<NetworkStatus> = _networkStatus
    private val queryKey = arrayOf(
        "watermelon",
        "apple",
        "girl",
        "beauty",
        "lolita",
        "bot",
        "music",
        "animal"
    ).random()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, PhotoItem>
    ) {
        retry = null
//      .postValue()比.value()更安全，因为能在父线程子线程中用
        _networkStatus.postValue(NetworkStatus.INITIAL_LOADING)
        val url =
            "https://pixabay.com/api/?key=22556485-96aeb2a3a21ebea4e5135e589&q=${queryKey}&per_page=40&page=1"
        StringRequest(
            Request.Method.GET,
            url,
            {
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()
                callback.onResult(dataList, null, 2)
                _networkStatus.postValue(NetworkStatus.LOADED)
            },
            {
//              前面retry需要返回lambda，这里就直接={}
                retry = { loadInitial(params, callback) }
                _networkStatus.postValue(NetworkStatus.FAILED)
                Log.d("hello", "loadInitial: $it")
            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {
        retry = null
        _networkStatus.postValue(NetworkStatus.LOADING)

        val url =
            "https://pixabay.com/api/?key=22556485-96aeb2a3a21ebea4e5135e589&q=${queryKey}&per_page=40&page=${params.key}"
        StringRequest(
            Request.Method.GET,
            url,
            {
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()
                callback.onResult(dataList, params.key + 2)
                _networkStatus.postValue(NetworkStatus.LOADED)
            },
            {
//              下拉到最下面COMPLETED
                if (it.toString() == "com.android.volley.ClientError") {
                    _networkStatus.postValue(NetworkStatus.COMPLETED)
                } else {
                    retry = { loadAfter(params, callback) }
                    _networkStatus.postValue(NetworkStatus.FAILED)
                }
                Log.d("hello", "loadAfter: $it")
            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {

    }
}