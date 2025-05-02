// --- START of CategorySpinnerAdapter.kt ---

package com.example.budgetbudgies_prog7313_poe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes

/**
 * Custom adapter for displaying categories in a spinner with icons and category type
 */
class CategorySpinnerAdapter(
    context: Context,
    @LayoutRes private val layoutResource: Int,
    private val categories: List<Category>
) : ArrayAdapter<Category>(context, layoutResource, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent, true)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup, isDropDown: Boolean = false): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            if (isDropDown) R.layout.item_category_dropdown else R.layout.item_category_spinner,
            parent,
            false
        )

        val category = getItem(position) ?: return view

        // Set category name
        val tvCategoryName = view.findViewById<TextView>(R.id.tvCategoryName)
        tvCategoryName.text = category.categoryname

        // Set category type if available in the layout
        view.findViewById<TextView>(R.id.tvCategoryType)?.let {
            it.text = category.categoryType
        }

        // Set category icon if available in the layout
        view.findViewById<ImageView>(R.id.ivCategoryIcon)?.let {
            it.setImageResource(category.icon)
        }

        return view
    }
}

// Update an existing account