package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalsActivity : AppCompatActivity() {

    private lateinit var dao: GoalDao
    private var userId: Int = 0

    private lateinit var tvMonthlyGoal: TextView
    private lateinit var tvTotalSaved: TextView
    private lateinit var tvFulfilledCount: TextView
    private lateinit var tvFulfilledBonus: TextView
    private lateinit var swShowFulfilled: Switch
    private lateinit var activeContainer: LinearLayout
    private lateinit var fulfilledContainer: LinearLayout
    private lateinit var failedContainer: LinearLayout
    private lateinit var fabAddGoal: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goals_page)

        findViewById<Toolbar>(R.id.goalsToolbar).apply {
            setNavigationIcon(R.drawable.ic_back_arrow)
            setNavigationOnClickListener { finish() }
        }

        dao = AppDatabase.getDatabase(this).goalDao()
        userId = intent.getIntExtra("USER_ID", 0)

        tvMonthlyGoal     = findViewById(R.id.tvMonthlyGoal)
        tvTotalSaved      = findViewById(R.id.tvTotalSaved)
        tvFulfilledCount  = findViewById(R.id.tvFulfilledCount)
        tvFulfilledBonus  = findViewById(R.id.tvFulfilledBonus)
        swShowFulfilled   = findViewById(R.id.swShowFulfilled)
        activeContainer   = findViewById(R.id.activeContainer)
        fulfilledContainer= findViewById(R.id.fulfilledContainer)
        failedContainer   = findViewById(R.id.failedContainer)
        fabAddGoal        = findViewById(R.id.fabAddGoal)

        fabAddGoal.setOnClickListener {
            startActivity(
                Intent(this, CreateGoalActivity::class.java)
                    .putExtra("USER_ID", userId)
            )
        }

        swShowFulfilled.setOnCheckedChangeListener { _, visible ->
            fulfilledContainer.isVisible = visible
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val list = dao.getUserGoalsList(userId)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@GoalsActivity,
                    "Loaded ${list.size} goals",
                    Toast.LENGTH_SHORT
                ).show()
            }
            renderGoals(list)
        }
    }

    private fun renderGoals(goals: List<Goal>) {
        val monthly = goals.sumOf { it.target }
        tvMonthlyGoal.text = "R%.2f".format(monthly)

        val totalSaved = goals.sumOf { it.savedAmount }
        tvTotalSaved.text = "R%.2f".format(totalSaved)

        val fulfilledCount = goals.count { it.savedAmount >= it.target }
        tvFulfilledCount.text = "$fulfilledCount/${goals.size}"
        tvFulfilledBonus.text = "+${fulfilledCount * 10}" // bonus logic placeholder

        activeContainer.removeAllViews()
        goals.filter { it.savedAmount < it.target }.forEach {
            addGoalView(it, activeContainer)
        }

        fulfilledContainer.removeAllViews()
        goals.filter { it.savedAmount >= it.target }.forEach {
            addGoalView(it, fulfilledContainer)
        }

        failedContainer.removeAllViews()
        // Add logic for failed if needed
    }

    private fun addGoalView(goal: Goal, parent: ViewGroup) {
        val v = LayoutInflater.from(this)
            .inflate(R.layout.item_goal, parent, false)

        v.findViewById<TextView>(R.id.tvName).text =
            goal.goalname
        v.findViewById<TextView>(R.id.tvAmount).text =
            "R%.2f".format(goal.target)

        val progressPercent = if (goal.target > 0) {
            ((goal.savedAmount / goal.target) * 100).toInt().coerceAtMost(100)
        } else 0

        v.findViewById<ProgressBar>(R.id.progressBar).progress = progressPercent
        v.findViewById<TextView>(R.id.tvPercent).text = "$progressPercent%"

        v.findViewById<ImageView>(R.id.ivStatus)
            .setImageResource(
                when {
                    progressPercent >= 100 -> R.drawable.ic_check
                    progressPercent >= 50  -> R.drawable.ic_halfway
                    else                   -> R.drawable.ic_egg
                }
            )

        v.setOnClickListener {
            startActivity(
                Intent(this, EditGoalActivity::class.java)
                    .putExtra("GOAL_ID", goal.goalid)
            )
        }

        parent.addView(v)
    }
}
