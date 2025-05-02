package com.example.budgetbudgies_prog7313_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// Adapter for displaying a list of category spending items in a RecyclerView
class CategorySpendingAdapter :
    ListAdapter<CategorySpending, CategorySpendingAdapter.CategoryViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_spending, parent, false)
        return CategoryViewHolder(view)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder class for holding and binding views
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val nameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val totalSpentTextView: TextView = itemView.findViewById(R.id.totalSpentTextView)

        // Bind data from CategorySpending to views
        fun bind(item: CategorySpending) {
            // Set icon if available, otherwise clear it
            item.iconResId?.let {
                iconView.setImageResource(it)
            } ?: iconView.setImageDrawable(null)

            // Set category name and total spent text
            nameTextView.text = item.categoryName
            totalSpentTextView.text = "R ${String.format("%.2f", item.totalAmount)}"
        }
    }

    // Callback to optimize RecyclerView updates
    class DiffCallback : DiffUtil.ItemCallback<CategorySpending>() {
        // Check if two items represent the same category (based on name)
        override fun areItemsTheSame(oldItem: CategorySpending, newItem: CategorySpending): Boolean {
            return oldItem.categoryName == newItem.categoryName // Assuming names are unique
        }

        // Check if the content of items is the same
        override fun areContentsTheSame(oldItem: CategorySpending, newItem: CategorySpending): Boolean {
            return oldItem == newItem
        }
    }
}
