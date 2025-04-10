package com.example.mobileappproject

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappproject.databinding.ActivityTimerListTodoPopupBinding
import kotlinx.coroutines.NonDisposableHandle.parent

class timerTodoListAdapter(
    val context: Context,
    val onShowDB: (Array<String>) -> Unit,
    val onSelectTimer : (Long) -> Unit,
    val onClick : (Array<String>) -> Unit
) : RecyclerView.Adapter<timerTodoListAdapter.TimerListViewHolder>(){

    private var list = mutableListOf<Todo>()
    private var preView : View? = null
    private var preId : String = "-1"

    inner class TimerListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var title = itemView.findViewById<TextView>(R.id.timertodoTitle)
        var startTime = itemView.findViewById<TextView>(R.id.timertodoTime)
        var content = itemView.findViewById<TextView>(R.id.timertodoContent)
        var date = itemView.findViewById<TextView>(R.id.timertodoDate)

        fun onBind(TodoT:String, TodoS:String, TodoE:String,TodoC:String, TodoD: String) {
            title.text = "title = ${TodoT}              "
            startTime.text = "time = ${TodoS} ~ ${TodoE}"
            content.text = "context = ${TodoC}"
            date.text = "date = ${TodoD}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_timer_list_todo_popup, parent, false)
        return TimerListViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: TimerListViewHolder, position: Int) {

        if(!list[position].isTimer){ //isTimer == false
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            holder.onBind("-","-","-","-","-")
        }
        else if(timerTodoSelectedDay.equals(list[position].date) || timerTodoSelectedDay.equals("ALL")) {
            // 선택된 date || date == None
            val to = list[position]
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            holder.onBind(to.title, to.startTime, to.endTime, to.content, to.date)
        }
        else{
            // 선택된 date가 아닌 경우
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            holder.onBind("-","-","-","-","-")
        }

        if(list[position].id == preId.toLong()) {
            holder.itemView.findViewById<Button>(R.id.timerselectBtn).setBackgroundColor(Color.parseColor("#D0A4ED"))
            preView = holder.itemView.findViewById<Button>(R.id.timerselectBtn)
        }
        else {
            holder.itemView.findViewById<Button>(R.id.timerselectBtn).setBackgroundColor(Color.parseColor("#000000"))
        }

        holder.itemView.findViewById<Button>(R.id.timertodoBtn).setOnClickListener {
            val to = list[position]
            val timeArray = arrayOf(to.basicTimer, to.pomodoro, to.timeBox)
            onShowDB(timeArray)
        }

        holder.itemView.findViewById<Button>(R.id.timerselectBtn).setOnClickListener {
            val to = list[position]
            val timeArray = arrayOf(to.id.toString(), to.basicTimer, to.pomodoro, to.timeBox)


            if(preId == to.id.toString()) {
                it.setBackgroundColor(Color.parseColor("#000000"))
                timeArray[0] = "-1"
                preId = "-1"
            }
            else {
                preView?.setBackgroundColor(Color.parseColor("#000000"))
                it.setBackgroundColor(Color.parseColor("#D0A4ED"))
                preId = to.id.toString()
            }
            onClick(timeArray)
            onSelectTimer(list[position].id)
            preView = it
        }
    }

    fun update(newList: MutableList<Todo>) {
        this.list = newList
        notifyDataSetChanged()
    }
    fun updating(){
        notifyDataSetChanged()
    }
}