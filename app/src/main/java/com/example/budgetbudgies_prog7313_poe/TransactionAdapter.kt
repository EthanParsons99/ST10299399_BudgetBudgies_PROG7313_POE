// --- START TransactionAdapter.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class DisplayTransaction(
    val id: Int,
    val type: String, // "Expense" or "Income"
    val description: String, // Use category name for Income if description not available
    val amount: Double,
    val date: Date,
    val categoryName: String?, // Store resolved category name
    val accountName: String?, // Store resolved account name
    val icon: Int?,   // Store category icon
    val photoPath: String? // Store photo path for expenses
)

class TransactionAdapter (
    private val onTransactionClick: (DisplayTransaction) -> Unit
) : ListAdapter<DisplayTransaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iconView: ImageView = view.findViewById(R.id.transaction_icon)
        private val descriptionText: TextView = view.findViewById(R.id.transaction_description)
        private val categoryText: TextView = view.findViewById(R.id.transaction_category) // Optional: or display account
        private val dateText: TextView = view.findViewById(R.id.transaction_date)
        private val amountText: TextView = view.findViewById(R.id.transaction_amount)
        private val photoIndicator: ImageView = view.findViewById(R.id.transaction_photo_indicator)
        private val context: Context = view.context
        private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault()) // Format like "Oct 26"

        fun bind(transaction: DisplayTransaction) {
            descriptionText.text = transaction.description
            categoryText.text = transaction.categoryName ?: "N/A" // Show category name
            dateText.text = dateFormat.format(transaction.date)

            // Set amount color based on type
            if (transaction.type == "Income") {
                amountText.text = String.format(Locale.US, "+ R %.2f", transaction.amount)
                amountText.setTextColor(ContextCompat.getColor(context, R.color.holo_green_dark)) // Use color resource
            } else { // Expense
                amountText.text = String.format(Locale.US, "- R %.2f", transaction.amount)
                amountText.setTextColor(ContextCompat.getColor(context, R.color.holo_red_dark)) // Use color resource
            }

            // Set Icon
            val iconResId = transaction.icon ?: R.drawable.ic_food
            if (iconResId != 0) {
                iconView.setImageResource(iconResId)
            } else {
                // Default icon if category icon missing or for Income
                iconView.setImageResource(if (transaction.type == "Income") R.drawable.ic_income_default else R.drawable.ic_expense_default)
                // Make sure you have ic_income_default and ic_expense_default drawables
            }

            // Show photo indicator if path exists for expenses
            if (transaction.type == "Expense" && transaction.photoPath != null) {
                photoIndicator.visibility = View.VISIBLE
            } else {
                photoIndicator.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onTransactionClick(transaction)
            }
        }

        private fun getDrawableResourceIdByName(context: Context, resourceName: String?): Int {
            if (resourceName.isNullOrBlank()) return 0
            return try {
                context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            } catch (e: Exception) { 0 }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false) // Use your transaction item layout
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<DisplayTransaction>() {
        override fun areItemsTheSame(oldItem: DisplayTransaction, newItem: DisplayTransaction): Boolean {
            // Need a unique way to identify across expense/income, combine type and id
            return oldItem.type == newItem.type && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DisplayTransaction, newItem: DisplayTransaction): Boolean {
            return oldItem == newItem
        }
    }
}
