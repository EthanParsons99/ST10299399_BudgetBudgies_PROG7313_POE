// --- START DbCategoryAdapter.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DbCategoryAdapter(
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, DbCategoryAdapter.DbCategoryViewHolder>(CategoryDiffCallback()) {

    inner class DbCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val categoryName: TextView = view.findViewById(R.id.category_name)
        private val iconView: ImageView = view.findViewById(R.id.category_icon)
        private val moreButton: ImageView = view.findViewById(R.id.more_button)
        private val context: Context = view.context // Store context

        fun bind(category: Category) {
            categoryName.text = category.categoryname

            // Set Icon based on stored resource name string
            val iconResId = getDrawableResourceIdByName(context, category.iconResName)
            if (iconResId != 0) {
                iconView.setImageResource(iconResId)
            } else {
                iconView.setImageResource(R.drawable.ic_launcher_foreground) // Fallback icon
            }


            moreButton.setOnClickListener {
                showPopupMenu(it, category)
            }
        }

        private fun showPopupMenu(view: View, category: Category) {
            val popup = PopupMenu(view.context, view)
            // Ensure you have res/menu/category_menu.xml with an item R.id.action_delete
            popup.menuInflater.inflate(R.menu.category_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        onDeleteClick(category)
                        true
                    }
                    // Add R.id.action_edit here later if needed
                    else -> false
                }
            }
            popup.show()
        }

        // Helper function to get drawable ID from its name string
        private fun getDrawableResourceIdByName(context: Context, resourceName: String?): Int {
            if (resourceName.isNullOrBlank()) return 0
            return try {
                context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            } catch (e: Exception) {
                Log.e("DbCategoryAdapter", "Failed to get resource ID for $resourceName", e)
                0 // Return 0 if not found or error
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DbCategoryViewHolder {
        // Use item_db_category.xml as the layout for each row
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return DbCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DbCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.categoryid == newItem.categoryid
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
// --- END DbCategoryAdapter.kt ---