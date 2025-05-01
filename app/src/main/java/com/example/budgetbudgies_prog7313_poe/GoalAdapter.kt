import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbudgies_prog7313_poe.Goal
import com.example.budgetbudgies_prog7313_poe.R

class GoalAdapter(private val goals: List<Goal>) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.goal_item, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]

        // Set Goal Name
        holder.tvGoalName.text = goal.goalname

        // Set Goal Notes
        holder.tvGoalNotes.text = goal.notes

        // Calculate Progress
        val progress = (goal.savedAmount / goal.target * 100).toInt()
        holder.tvProgressLabel.text = "Progress: $progress%"

        // Update ProgressBar
        holder.progressBar.progress = progress
    }

    override fun getItemCount(): Int {
        return goals.size
    }

    // ViewHolder for binding the views
    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGoalName: TextView = view.findViewById(R.id.tvGoalName)
        val tvGoalNotes: TextView = view.findViewById(R.id.tvGoalNotes)
        val tvProgressLabel: TextView = view.findViewById(R.id.tvProgressLabel)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
    }
}
