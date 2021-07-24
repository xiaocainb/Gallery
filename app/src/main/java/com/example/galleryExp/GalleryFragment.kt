package com.example.galleryExp


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {
    private val galleryViewModel by activityViewModels<GalleryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    //  menu处理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//      有很多menu的话用when处理
        when (item.itemId) {
            R.id.swipeIndicator -> {
                swipeLayoutGallery.isRefreshing = true
//              加延时，防止网速过快看不清shimmer
                Handler().postDelayed({ galleryViewModel.resetQuery() }, 1000)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //  加载menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu,menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//      加载menu
        setHasOptionsMenu(true)
//      创建galleryViewModel放入构造
        val galleryAdapter = GalleryAdapter(galleryViewModel)

        recyclerView.apply {
            adapter = galleryAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        galleryViewModel.pagedListLiveData.observe(viewLifecycleOwner, Observer {
            galleryAdapter.submitList(it)
        })

        swipeLayoutGallery.setOnRefreshListener {
            galleryViewModel.resetQuery()
        }

        galleryViewModel.networkStatus.observe(viewLifecycleOwner, Observer {
            Log.d("HELLO", "onActivityCreated: $it")
            galleryAdapter.updateNetworkStatus(it)
            swipeLayoutGallery.isRefreshing = it == NetworkStatus.INITIAL_LOADING
        })

//1        galleryViewModel.photoListLive.observe(this, Observer {
//1            if (galleryViewModel.needToScrollToTop) {
////1              初次加载画面在最下方，判断needToScrollToTop,滚动至0也就是顶部
//1                recyclerView.scrollToPosition(0)
//1                galleryViewModel.needToScrollToTop = false
//1            }
//1            galleryAdapter.submitList(it)
//1            swipeLayoutGallery.isRefreshing = false
//1        })
//1        galleryViewModel.dataStatusLive.observe(this, Observer {
//1            galleryAdapter.footerViewStatus = it
////1          最后一行数据那里手动出现网络故障
//1            galleryAdapter.notifyItemChanged(galleryAdapter.itemCount - 1)
//1            if (it == DATA_STATUS_NETWORK_ERROR) swipeLayoutGallery.isRefreshing = false
//1        })
//1
//1
////1      下拉刷新
//1        swipeLayoutGallery.setOnRefreshListener {
////1            galleryViewModel.resetQuery()
//1            galleryViewModel.fetchData()
//1        }
//1
//1        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//1            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//1                super.onScrolled(recyclerView, dx, dy)
//1                if (dy < 0) return
//1                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
//1                val intArray = IntArray(2)
////1              find用last不用lastComplete
//1                layoutManager.findLastVisibleItemPositions(intArray)
////1              判断最后一个页脚
//1                if (intArray[0] == galleryAdapter.itemCount - 1) {
//1                    galleryViewModel.fetchData()
//1                }
//1            }
//1        })
    }
}
