package com.example.task_king.homwrklist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.task_king.R
import com.github.chrisbanes.photoview.PhotoView

//import com.ortiz.touchview.TouchImageView

class FullScreenImageAdapter(private val uris: List<String>,
//    val listener: OnImageTap
) :
    RecyclerView.Adapter<FullScreenImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullImage: PhotoView = itemView.findViewById(R.id.fullImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_full_screen_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentImage = uris[position]
        Log.i("currentImage", "image uri: $currentImage")
        Glide.with(holder.itemView.context)
            .load(currentImage)
            .into(holder.fullImage)
//        val gesture = GestureDetector(holder.fullImage.context, object : GestureDetector.SimpleOnGestureListener(){
//            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
//                listener.onSingleTap()
//                Log.i("single", "single tap")
//                return false
//            }
//
//            override fun onDoubleTap(e: MotionEvent): Boolean {
//                Log.i("double", "double tap")
//                return false
//            }
//        })
//        holder.fullImage.setOnTouchListener { _, e ->
//            gesture.onTouchEvent(e)
//            true
//        }
    }

    override fun getItemCount() = uris.size
}
