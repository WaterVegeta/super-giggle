package com.example.task_king.homwrklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.task_king.R
import com.example.task_king.data_base.ImageItem

class ImageAdapter(
    val images: List<ImageItem>,
    val addImage: () -> Unit,
    val startFullScreen: (Array<String>, Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val IMAGE = 0
    val BUTTON = 1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            IMAGE -> {
                val view = inflater.inflate(R.layout.item_child_image, parent, false)
                ImageViewHolder(view)
            }
            BUTTON -> {
                val view = inflater.inflate(R.layout.item_child_button, parent, false)
                ButtonViewHolder(view)
            }
            else -> throw IllegalArgumentException("unknown viewtype :3")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is ImageViewHolder -> {
                val imageUri = images[position].imageUri
                Glide.with(holder.itemView.context)
                    .load(imageUri)
                    .into(holder.imageView)

                val context = holder.itemView.context
                holder.imageView.setOnClickListener {
                    startFullScreen(images.map{ it.imageUri }.toTypedArray(), position)
                }
            }

            is ButtonViewHolder -> {
                holder.addBtn.setOnClickListener {
                    addImage()
                }
            }
        }
    }

    override fun getItemCount(): Int = images.size + 1
    override fun getItemViewType(position: Int): Int {
        return if (position == images.size) BUTTON else IMAGE
    }
    class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val addBtn : ImageButton = itemView.findViewById(R.id.addImage)
    }
    class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}