package com.example.budgetbudgies_prog7313_poe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbudgies_prog7313_poe.databinding.GoalItemBinding

class GoalAdapter : ListAdapter<Goal, GoalAdapter.GoalViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = GoalItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GoalViewHolder(private val binding: GoalItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: Goal) {
            binding.tvName.text = goal.goalname
            binding.tvTarget.text = "Target: ${goal.target} ${goal.currency}"
            binding.tvSaved.text = "Saved: ${goal.savedAmount} ${goal.currency}"
            binding.tvNotes.text = goal.notes
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean = oldItem.goalid == newItem.goalid
        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean = oldItem == newItem
    }
}