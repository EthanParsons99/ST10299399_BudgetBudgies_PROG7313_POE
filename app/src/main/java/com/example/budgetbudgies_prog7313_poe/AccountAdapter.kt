// --- START AccountAdapter.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AccountAdapter(
    private val onAccountClick: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    inner class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.account_item_name)
        private val balanceTextView: TextView = view.findViewById(R.id.account_item_balance)
        // Add ImageView for icon later if needed

        fun bind(account: Account) {
            nameTextView.text = account.accountname
            // Consider account color/icon name here later for visual differentiation
            balanceTextView.text = String.format(Locale.US, "R %.2f", account.balance)

            itemView.setOnClickListener {
                onAccountClick(account)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false) // Use item_account.xml
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.accountid == newItem.accountid
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}
// --- END AccountAdapter.kt ---