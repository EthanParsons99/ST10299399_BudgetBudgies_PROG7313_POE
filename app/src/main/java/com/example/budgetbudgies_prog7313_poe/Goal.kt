package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// ───────────────────────────────────────────────────────────────────────────
// 1) Your Room Entity holds all fields: notes, category, currency, etc.
// ───────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userid"],
            childColumns = ["userid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryid"],
            childColumns = ["categoryid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Goal(
    @PrimaryKey(autoGenerate = true) val goalid: Int = 0,
    val goalname: String,
    val notes: String?,
    val categoryid: Int,
    val target: Double,
    val currency: String,
    val userid: Int
)


// ───────────────────────────────────────────────────────────────────────────
// 2) RecyclerView Adapter
// ───────────────────────────────────────────────────────────────────────────
class GoalAdapter(
    private val onClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalAdapter.VH>() {
    private val data = mutableListOf<Goal>()
    fun submitList(list: List<Goal>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_goal, p, false))
    override fun onBindViewHolder(h: VH, i: Int) {
        val g = data[i]
        h.name.text = g.goalname
        h.amount.text = "R%.2f".format(g.target)
        val pct = ((g.target * .5) / g.target * 100).toInt() // demo
        h.progressBar.progress = pct
        h.tvPct.text = "$pct%"
        h.status.setImageResource(if (pct >= 100) R.drawable.ic_happy_egg else R.drawable.ic_egg)
        h.itemView.setOnClickListener { onClick(g) }
    }
    override fun getItemCount() = data.size
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvName)
        val amount: TextView = v.findViewById(R.id.tvAmount)
        val status: ImageView = v.findViewById(R.id.ivStatus)
        val progressBar: ProgressBar = v.findViewById(R.id.progressBar)
        val tvPct: TextView = v.findViewById(R.id.tvPercent)
    }
}


// ───────────────────────────────────────────────────────────────────────────
// 3) Single Activity to List, Add & Edit
//    (replaces your three separate activities)
// ───────────────────────────────────────────────────────────────────────────
class GoalsPageActivity : AppCompatActivity() {
    private lateinit var dao: GoalDao
    private var userId: Int = 0

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.goals_page)

        // toolbar back arrow
        findViewById<Toolbar>(R.id.goalsToolbar).apply {
            setNavigationIcon(R.drawable.ic_back_arrow)
            setNavigationOnClickListener { finish() }
        }

        // DAO & current user
        dao = AppDatabase.getDatabase(this).goalDao()
        userId = intent.getIntExtra("USER_ID", 0)

        // Recycler + FAB wiring
        val rv = findViewById<RecyclerView>(R.id.goalsRecyclerView)
        val adapter = GoalAdapter { showEditDialog(it) }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        findViewById<FloatingActionButton>(R.id.fabAddGoal)
            .setOnClickListener { showAddDialog() }

        // Observe DB
        lifecycleScope.launch {
            dao.getUserGoals(userId).collect { adapter.submitList(it) }
        }
    }

    private fun showAddDialog() {
        val v = layoutInflater.inflate(R.layout.add_goals, null)
        val etName     = v.findViewById<EditText>(R.id.etName)
        val etNotes    = v.findViewById<EditText>(R.id.etNotes)
        val spCat      = v.findViewById<Spinner>(R.id.spinnerCategory)
        val etAmount   = v.findViewById<EditText>(R.id.etAmount)
        val spCur      = v.findViewById<Spinner>(R.id.spinnerCurrency)

        // populate spinners
        spCat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Supermarket","Fuel","Other"))
        spCur.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("ZAR","USD"))

        AlertDialog.Builder(this)
            .setTitle("Create a goal")
            .setView(v)
            .setPositiveButton("Setup Goal") { _,_ ->
                val g = Goal(
                    goalname   = etName.text.toString(),
                    notes      = etNotes.text.toString(),
                    categoryid = spCat.selectedItemPosition,
                    target     = etAmount.text.toString().toDoubleOrNull()?:0.0,
                    currency   = spCur.selectedItem.toString(),
                    userid     = userId
                )
                lifecycleScope.launch { dao.insertGoal(g) }
            }
            .setNegativeButton("Cancel",null)
            .show()
    }

    private fun showEditDialog(g: Goal) {
        val v = layoutInflater.inflate(R.layout.edit_goals, null)
        val etName     = v.findViewById<EditText>(R.id.etName)
        val etNotes    = v.findViewById<EditText>(R.id.etNotes)
        val spCat      = v.findViewById<Spinner>(R.id.spinnerCategory)
        val etAmount   = v.findViewById<EditText>(R.id.etAmount)
        val spCur      = v.findViewById<Spinner>(R.id.spinnerCurrency)

        // prefill
        etName.setText(g.goalname)
        etNotes.setText(g.notes)
        etAmount.setText(g.target.toString())
        spCat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Supermarket","Fuel","Other"))
        spCur.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("ZAR","USD"))
        spCat.setSelection(g.categoryid)
        spCur.setSelection(listOf("ZAR","USD").indexOf(g.currency))

        AlertDialog.Builder(this)
            .setTitle("Edit goal")
            .setView(v)
            .setPositiveButton("Save Goal") { _,_ ->
                val updated = g.copy(
                    goalname   = etName.text.toString(),
                    notes      = etNotes.text.toString(),
                    categoryid = spCat.selectedItemPosition,
                    target     = etAmount.text.toString().toDoubleOrNull()?:g.target,
                    currency   = spCur.selectedItem.toString()
                )
                lifecycleScope.launch { dao.updateGoal(updated) }
            }
            .setNegativeButton("Cancel",null)
            .show()
    }
}
