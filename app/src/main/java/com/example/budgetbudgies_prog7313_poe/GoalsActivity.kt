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

        // 1) Toolbar back arrow
        findViewById<Toolbar>(R.id.goalsToolbar).apply {
            setNavigationIcon(R.drawable.ic_back_arrow)
            setNavigationOnClickListener { finish() }
        }

        // 2) Room DAO + userId
        dao = AppDatabase.getDatabase(this).goalDao()
        userId = intent.getIntExtra("USER_ID", 0)

        // 3) Widgets
        tvMonthlyGoal     = findViewById(R.id.tvMonthlyGoal)
        tvTotalSaved      = findViewById(R.id.tvTotalSaved)
        tvFulfilledCount  = findViewById(R.id.tvFulfilledCount)
        tvFulfilledBonus  = findViewById(R.id.tvFulfilledBonus)
        swShowFulfilled   = findViewById(R.id.swShowFulfilled)
        activeContainer   = findViewById(R.id.activeContainer)
        fulfilledContainer= findViewById(R.id.fulfilledContainer)
        failedContainer   = findViewById(R.id.failedContainer)
        fabAddGoal        = findViewById(R.id.fabAddGoal)

        // 4) FAB -> full-screen Add page
        fabAddGoal.setOnClickListener {
            startActivity(
                Intent(this, CreateGoalActivity::class.java)
                    .putExtra("USER_ID", userId)
            )
        }

        // 5) Toggle fulfilled section
        swShowFulfilled.setOnCheckedChangeListener { _, visible ->
            fulfilledContainer.isVisible = visible
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            // 1) Grab the full list
            val list = dao.getUserGoalsList(userId)

            // 2) Debug toast
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@GoalsActivity,
                    "Loaded ${list.size} goals",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // 3) Render them
            renderGoals(list)
        }
    }


    private fun renderGoals(goals: List<Goal>) {
        // 1) Monthly target
        val monthly = goals.sumOf { it.target }
        tvMonthlyGoal.text = "R%.2f".format(monthly)

        // 2) Total saved (stubbed)
        tvTotalSaved.text = "R0.00"

        // 3) Fulfilled count (stubbed)
        tvFulfilledCount.text = "0/${goals.size}"
        tvFulfilledBonus.text = "+0"

        // 4) Populate Active section
        activeContainer.removeAllViews()
        goals.forEach { addGoalView(it, activeContainer) }

        // 5) Clear other sections for now
        fulfilledContainer.removeAllViews()
        failedContainer.removeAllViews()
    }

    private fun addGoalView(goal: Goal, parent: ViewGroup) {
        val v = LayoutInflater.from(this)
            .inflate(R.layout.item_goal, parent, false)

        // Bind data
        v.findViewById<TextView>(R.id.tvName).text   = goal.goalname
        v.findViewById<TextView>(R.id.tvAmount).text = "R%.2f".format(goal.target)
        v.findViewById<ProgressBar>(R.id.progressBar).progress = 0
        v.findViewById<TextView>(R.id.tvPercent).text         = "0%"
        v.findViewById<ImageView>(R.id.ivStatus)
            .setImageResource(R.drawable.ic_egg)

        // Edit on click
        v.setOnClickListener {
            startActivity(
                Intent(this, EditGoalActivity::class.java)
                    .putExtra("GOAL_ID", goal.goalid)
            )
        }

        parent.addView(v)
    }
}
