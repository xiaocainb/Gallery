package com.example.galleryExp

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.gallery_cell.view.*
import kotlinx.android.synthetic.main.gallery_footer.view.*

class GalleryAdapter(private val galleryViewModel: GalleryViewModel) :
    PagedListAdapter<PhotoItem, RecyclerView.ViewHolder>(DIFFCALLBACK) {

//1    companion object {
//1        const val NORMAL_VIEW_TYPE = 0
//1        const val FOOTER_VIEW_TYPE = 1
//1    }
//1    var footerViewStatus = DATA_STATUS_CAN_LOAD_MORE

    private var networkStatus: NetworkStatus? = null
    private var hasFooter = false

    init {
        galleryViewModel.retry()
    }

    fun updateNetworkStatus(networkStatus: NetworkStatus?) {
        this.networkStatus = networkStatus
        if (networkStatus == NetworkStatus.INITIAL_LOADING) hideFooter() else showFooter()
    }

    private fun hideFooter() {
        if (hasFooter) {
            notifyItemRemoved(itemCount - 1)
        }
        hasFooter = false
    }

    private fun showFooter() {
        if (hasFooter) {
            notifyItemChanged(itemCount - 1)
        } else {
            hasFooter = true
            notifyItemInserted(itemCount - 1)
        }
    }

    //第一次加载不显示footer因为有swiper了，在PixabayDataSource的loadInitial中取消显示footer
    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasFooter && position == itemCount - 1) R.layout.gallery_footer else R.layout.gallery_cell
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.gallery_cell -> PhotoViewHolder.newInstance(parent).also { holder ->
//              这里有个it嵌套
                holder.itemView.setOnClickListener {
                    Bundle().apply {
                        putInt("PHOTO_POSITION", holder.adapterPosition)
                        holder.itemView.findNavController()
                            .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)
                    }
                }
            }
            else -> FooterViewHolder.newInstance(parent).also {
                it.itemView.setOnClickListener {
                    galleryViewModel.retry()
                }
            }
        }
//1        val holder = PhotoViewHolder(
//1            LayoutInflater.from(parent.context).inflate(R.layout.gallery_cell, parent, false)
//1        )
//1        if (viewType == NORMAL_VIEW_TYPE) {
//1        holder.itemView.setOnClickListener {
////1          用bundle传递数据
//1            Bundle().apply {
////1              加入到bundle
//1                putParcelableArrayList("PHOTO_LIST", ArrayList(currentList))
//1                putInt("PHOTO_POSITION", holder.adapterPosition)
////1              导航过去
//1                holder.itemView.findNavController()
//1                    .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)
//1            }
//1        }
//1        } else {
//1          写footer
//1            holder = MyViewHolder(
//1                LayoutInflater.from(parent.context).inflate(R.layout.gallery_footer, parent, false)
//1                    .also {
////1                      底部footer放在中间
//1                        (it.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan =
//1                            true
////1                      lambda嵌套要区分it！！！！！！！！！！！！！！！！
//1                        it.setOnClickListener {itemView->
//1                            itemView.progressBar.visibility=View.VISIBLE
//1                            itemView.textView.text="正在加载"
//1                            galleryViewModel.fetchData()
//1                        }
//1                    }
//1            )
//1        }
//1        return holder
    }

//1    override fun getItemCount(): Int {
//1        return super.getItemCount() + 1
//1    }
//1
//1    override fun getItemViewType(position: Int): Int {
////1      返回判断最后一行if
//1        return if (position == itemCount - 1) FOOTER_VIEW_TYPE else NORMAL_VIEW_TYPE
//1    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

////1      如果position是最后一个就不会用下面步骤
//1        if (position == itemCount - 1) {
//1            with(holder.itemView) {
//1                when (footerViewStatus) {
//1                    DATA_STATUS_CAN_LOAD_MORE -> {
//1                        progressBar.visibility = View.VISIBLE
//1                        textView.text = "正在加载"
//1                        isClickable = false
//1                    }
//1                    DATA_STATUS_NO_MORE -> {
//1                        progressBar.visibility = View.GONE
//1                        textView.text = "再往下就没有了"
//1                        isClickable = false
//1                    }
//1                    DATA_STATUS_NETWORK_ERROR -> {
//1                        progressBar.visibility = View.GONE
//1                        textView.text = "网络错误，点击重试"
////1                      可点击！
//1                        isClickable = true
//1                    }
//1                }
//1            }
//1            return
//1        }
        when (holder.itemViewType) {
            R.layout.gallery_footer -> (holder as FooterViewHolder).bindWithNetworkStatus(
                networkStatus
            )
            else -> {
                //  实现photo的参数赋值
                val photoItem = getItem(position) ?: return
                (holder as PhotoViewHolder).bindWithPhotoItem(photoItem)
            }
        }
    }

    object DIFFCALLBACK : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem == newItem
        }
    }
}


//将代码精简到两个ViewHolder中
class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun newInstance(parent: ViewGroup): PhotoViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.gallery_cell, parent, false)
            return PhotoViewHolder(view)
        }
    }

    fun bindWithPhotoItem(photoItem: PhotoItem) {
        with(itemView) {
            shimmerLayoutCell.apply {
                setShimmerColor(0x55FFFFFF)
                setShimmerAngle(0)
                startShimmerAnimation()
            }
            textViewUser.text = photoItem.photoUser
            textViewCollections.text = photoItem.photoCollections.toString()
            textViewLikes.text = photoItem.photoLikes.toString()
            textViewViews.text = photoItem.photoViews.toString()
            imageView.layoutParams.height = photoItem.photoHeight
        }
//      加载图片 实现闪动load
        Glide.with(itemView)
            .load(photoItem.previewUrl)
            .placeholder(R.drawable.ic_photo_placeholder)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false.also { itemView.shimmerLayoutCell?.stopShimmerAnimation() }
                }

            })
            .into(itemView.imageView)

    }
}

//将代码精简到两个ViewHolder中
class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun newInstance(parent: ViewGroup): FooterViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.gallery_footer, parent, false)
//          使得加载更多圈在中间显示
            (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
            return FooterViewHolder(view)
        }
    }

    fun bindWithNetworkStatus(networkStatus: NetworkStatus?) {
        with(itemView) {
            when (networkStatus) {
                NetworkStatus.FAILED -> {
                    textView.text = "点击重试"
                    progressBar.visibility = View.GONE
                    isClickable = true
                }
                NetworkStatus.COMPLETED -> {
                    textView.text = "再怎么拉也没有啦"
                    progressBar.visibility = View.GONE
                    isClickable = false
                }
                else -> {
                    textView.text = "正在加载"
                    progressBar.visibility = View.VISIBLE
                    isClickable = false
                }
            }
        }
    }
}