package com.example.galleryExp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData

//1const val DATA_STATUS_CAN_LOAD_MORE: Int = 0
//1const val DATA_STATUS_NO_MORE = 1
//1const val DATA_STATUS_NETWORK_ERROR = 2

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    val pagedListLiveData = PixabayDataSourceFactory(application).toLiveData(5)
    private val factory = PixabayDataSourceFactory(application)
//  Transformations.switchMap()是用LiveData观察LiveData
    val networkStatus = Transformations.switchMap(factory.pixabayDataSource) { it.networkStatus }

//  下拉刷新，也就是Factory重新生成dataSource对象  //但是可能有点浪费性能？
    fun resetQuery() {
        pagedListLiveData.value?.dataSource?.invalidate()
    }
//  点击重试
    fun retry(){
        factory.pixabayDataSource.value?.retry?.invoke()
    }


    //  ViewModel中构造要在内部调用，不能被外部调用
//    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
//    val photoListLive: LiveData<List<PhotoItem>> get() = _photoListLive
//1    private val _dataStatusLive = MutableLiveData<Int>()//监听当前数据状态
//1    val dataStatusLive: LiveData<Int> get() = _dataStatusLive

//1    var needToScrollToTop = true
//    private val keyWords =
//        arrayOf("watermelon", "apple", "girl", "beauty", "android", "bot", "music", "animal")
//1    private var perPage = 40
//1    private var currentPage = 1
//1    private var totalPage = 1
//1    private var currentKey = "girl"
//1    private var isNewQuery = true
//1    private var isLoading = false//作为监听的阻挡

//1    init {
//1        resetQuery()
//1    }

    //1  发起重新请求
//1    fun resetQuery() {
//1        currentPage = 1
//1        totalPage = 1
//1        currentKey = keyWords.random()
//1        isNewQuery = true
//1        needToScrollToTop = true
//1        fetchData()
//1    }

    //  继续加载
//    fun fetchData() {
//1       isLoading要拦截
//1         if (isLoading) return
//1         if (currentPage > totalPage) {
//1             _dataStatusLive.value = DATA_STATUS_NO_MORE
//1             return
//1         }
//1         isLoading = true

//        val stringRequest = StringRequest(
//            Request.Method.GET,
//            getUrl(),
//            {
//                _photoListLive.value = Gson().fromJson(it, Pixabay::class.java).hits.toList()
//            },
//            {
//
//            }
//1            {
//1                with(Gson().fromJson(it, Pixabay::class.java)) {
////1                  ceil()向上取整double
//1                    totalPage = ceil(totalHits.toDouble() / perPage).toInt()
//1
//1                   if (isNewQuery) {
//1                        _photoListLive.value = hits.toList()
//1                    } else {
////1                        双感叹号是非空，flatten()使两个List扁平化生成新List
//1                        _photoListLive.value =
//1                            arrayListOf(_photoListLive.value!!, hits.toList()).flatten()
//1                    }
//1                }
//1                _dataStatusLive.value = DATA_STATUS_CAN_LOAD_MORE
//1                isLoading = false
//1                isNewQuery = false
//1                currentPage++
//1            },
//1            {
//1                _dataStatusLive.value = DATA_STATUS_NETWORK_ERROR
//1                isLoading = false
//1            }
//        )
//        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)
//    }

//    private fun getUrl(): String {
//1        return "https://pixabay.com/api/?key=22556485-96aeb2a3a21ebea4e5135e589&q=${currentKey}&per_page=${perPage}&page=${currentPage}"
//    }
}