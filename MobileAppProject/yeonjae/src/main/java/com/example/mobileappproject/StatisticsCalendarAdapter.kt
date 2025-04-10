package com.example.mobileappproject

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappproject.R
import com.example.mobileappproject.databinding.DaysCellBinding
import com.example.mobileappproject.CalendarUtil
import java.time.LocalDate
import android.content.Context
import android.graphics.Typeface
//import android.graphics.Color
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mobileappproject.CalendarUtil.Companion.today
import com.example.mobileappproject.databinding.StatisticsDaysCellBinding
import java.text.SimpleDateFormat
import java.util.*


class StatisticsCalendarAdapter(private val days: MutableList<LocalDate?>, private var activity: LifecycleOwner, val context: Context,
/*val onStatisticsValues: (Array<Long>) -> Unit*/
                                val onPieChart : (MutableList<Int>)->Unit,
                                val onLineChart : (MutableList<String>)->Unit,
                                val onBarChart : (MutableList<String>)->Unit,
                                var onTotaltime : (MutableList<String>)->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class StatisticsCalendarViewHolder(val binding: StatisticsDaysCellBinding):
        RecyclerView.ViewHolder(binding.root)
    private var list = mutableListOf<Todo>()
    var pieList = mutableListOf<Int>()
    var lineList = mutableListOf<String>("0","0","0","0","0","0","0","0")
    var barList = mutableListOf<String>("0","0","0","0","0","0","0","0")
    var totalList = mutableListOf<String>()
    var totalBasic = 0
    var totalPomodoro = 0


    // cell clickListener interface
    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int)
    }
    var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.statistics_days_cell, parent, false)
        val layoutParams: ViewGroup.LayoutParams = view.layoutParams
        layoutParams.height = (parent.height * 0.166666666).toInt()

        return StatisticsCalendarViewHolder(StatisticsDaysCellBinding.bind(view))
    }

    //override fun getItemCount_todo(): Int = list.size

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as StatisticsCalendarViewHolder).binding

        // itemClickListener 연결
        if (listener != null){
            binding.cellRoot.setOnClickListener(View.OnClickListener {
                listener?.onItemClick(it, position)
            })
        }


        // 날짜표시 & 오늘날짜 바탕색
        val day : LocalDate? = days[position]
        val dayDel = days[15].toString().split("-")[1] == day.toString().split("-")[1]

        if (day == null) {
            binding.cellDayText.text = ""
            //binding.todoCount.text = ""
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.cellDayText.text = day.dayOfMonth.toString()
            }
            if (day.equals(CalendarUtil.today)) { //오늘날짜라면
                binding.cellDayText.setBackgroundColor(Color.LTGRAY)
            } else {
                binding.cellRoot.setBackgroundResource(0)
                binding.cellDayText.setTextColor(Color.BLACK)
                //binding.todoCount.setTextColor(Color.parseColor("#5282FF"))

                // change weekend textColor
                if ((position + 1) % 7 == 0 && dayDel) {
                    binding.cellDayText.setTextColor(Color.BLUE)
                } else if (position == 0 || position % 7 == 0 && dayDel) {
                    binding.cellDayText.setTextColor(Color.RED)
                }

                if (!dayDel) {
                    binding.cellDayText.setTextColor(Color.LTGRAY)
                    //binding.todoCount.setTextColor(Color.LTGRAY)
                }
            }
        }


        // set Colors to cells

        var title: MutableList<String> = mutableListOf()
        var startTime = mutableListOf<String>()
        var endTime = mutableListOf<String>()
        var content = mutableListOf<String>()
        var date = mutableListOf<String>()
        var isTimer = mutableListOf<Boolean>()
        var categoryNum = mutableListOf<Int>()
        var basicTimer = mutableListOf<String>()
        var pomodoro = mutableListOf<String>()
        var timeBox = mutableListOf<String>()


        val setCellColors = TodoViewModel().getCurrentDay(day.toString())
        setCellColors.observe(activity) { it ->
            // when there is to do in current day
            //println("======================> ${it.size}")

            if(position == itemCount-1) {
                onPieChart(pieList)
                onTotaltime(totalList)
                usingDB()
            }

            if (it.size == 0)
            {
                //binding.cellRoot.setBackgroundColor(Color.GREEN)
            }

            // all to do datas in current cell
            // you can't get db data out of this block So work here
            else if (it.size > 0) {
                for (i in 0 until it.size) {
                    if(it[i].isTimer) {
                        categoryNum.add(it[i].categoryNum)
                        basicTimer.add(it[i].basicTimer)
                        pomodoro.add(it[i].pomodoro)
                        timeBox.add(it[i].timeBox)

                        //totalList(timebox) add
                        if(it[i].timeBox != "0") {
                            totalList.add(it[i].timeBox)
                        }

                        if (it[i].basicTimer != "0") {
                            totalBasic += it[i].basicTimer.split(":")[0].toInt() * 3600 +
                                    it[i].basicTimer.split(":")[1].toInt() * 60 +
                                    it[i].basicTimer.split(":")[2].toInt()
                        }

                        if (it[i].pomodoro != "0") {
                            totalPomodoro += it[i].pomodoro.split("회")[0].toInt()
                        }
                    }

                    pieList.add(it[i].categoryNum)
                }


                // calculate
                var startEndTImeInTodo = 0
                // total time of basic timers in one day
                for(i in 0 until startTime.size)
                {
                    if (startTime[i].toString() != "시작시간" && endTime[i].toString() != "종료시간") {
                        val (hs, ms) = startTime[i].split(":").map { it1 -> it1.toInt() }
                        val (he, me) = endTime[i].split(":").map { it2 -> it2.toInt() }
                        // convert hours to min and add
                        startEndTImeInTodo += (he * 60 + me) - (hs * 60 + ms)
                    }
                }

                var basicTime = 0
                // if 0 than will raise error while split basicTimer variable to do in more easy way when not timer than set to 00:00:00 type
                for (i in 0 until basicTimer.size){
                    if (basicTimer[i] != "0"){
                        var (h, m, s) = basicTimer[i].split(":").map { it3->it3.toInt() }
                        //converting to second
                        basicTime += h * 3600 + m * 60 + s
                    }
                }

                //show one day total basic time
                var h = basicTime/3600
                var m = (basicTime - h)/60
                var s = basicTime - h - m
                binding.StatisticsBasicTime.text = "%02d:%02d:%02d".format(h, m, s)
                binding.StatisticsBasicTime.setTextColor(Color.BLACK)

                if(basicTime in 1..3) {
                    binding.cellRoot.setBackgroundColor(Color.parseColor("#110000FF"))
                }
                else if (basicTime in 4..6)
                    binding.cellRoot.setBackgroundColor(Color.parseColor("#440000FF"))
                else if (basicTime in 7..9)
                    binding.cellRoot.setBackgroundColor(Color.parseColor("#770000FF"))
                else if(basicTime in 10..12)
                    binding.cellRoot.setBackgroundColor(Color.parseColor("#AA0000FF"))
                else if(basicTime > 12) {
                    println("in more 8 hours")
                    binding.cellRoot.setBackgroundColor(Color.parseColor("#FF0000FF"))
                }
            }
        }

    }

    override fun getItemCount(): Int = days.size

    fun update(newList: MutableList<Todo>) {
        this.list = newList
        //notifyDataSetChanged()
    }

    //send to StatisticMain
    @RequiresApi(Build.VERSION_CODES.O)
    private fun usingDB() {
        //DB 전체 이용
        for (idx in 0 .. list.size-1){
            Log.d("s00","list[$idx] / ${list.size}= ${list[idx].date}")

            if(list[idx].date == CalendarUtil.today.toString() ||
                list[idx].date == CalendarUtil.today.minusDays(1).toString() ||
                list[idx].date == CalendarUtil.today.minusDays(2).toString() ||
                list[idx].date == CalendarUtil.today.minusDays(3).toString() ||
                list[idx].date == CalendarUtil.today.minusDays(4).toString() ||
                list[idx].date == CalendarUtil.today.minusDays(5).toString() ||
                list[idx].date == CalendarUtil.today.minusDays(6).toString()) {

                val startDate = SimpleDateFormat("yyyy-MM-dd").parse(list[idx].date).time
                var todayDate = Calendar.getInstance().time.time


                Log.d("s00","today-start $startDate = ${(todayDate-startDate)/(24*60*60*1000)}")
                val dayValue : Int = 7-((todayDate-startDate)/(24*60*60*1000)).toInt()

                if(!list[idx].isTimer) continue

                //lineList add
                if(lineList[dayValue] == "0"){
                    lineList.set(dayValue,list[idx].pomodoro)
                }
                else{
                    lineList.set(dayValue, (lineList[dayValue].toInt() + list[idx].pomodoro.toInt()).toString())
                }

                //barList add
                if(barList[dayValue] == "0" || list[idx].basicTimer == "0" || list[idx].basicTimer == "00:00:00"){
                    barList.set(dayValue,list[idx].basicTimer)
                }
                else{
                    var h1 = barList[dayValue].split(":")[0]
                    var m1 = barList[dayValue].split(":")[1]
                    var s1 = barList[dayValue].split(":")[2]
                    var time1 = h1.toInt() * 3600 + m1.toInt() * 60 + s1.toInt()

                    var h2 = list[idx].basicTimer.split(":")[0]
                    var m2 = list[idx].basicTimer.split(":")[1]
                    var s2 = list[idx].basicTimer.split(":")[2]
                    var time2 = h2.toInt() * 3600 + m2.toInt() * 60 + s2.toInt()

                    var time = time1 + time2

                    var h = time / 3600
                    var m = (time - h * 3600) / 60
                    var s = (time - h * 3600 - m * 60)

                    var h_string: String
                    if(h < 10){
                        h_string = "0" + (h).toString()
                    }
                    else{
                        h_string = (h).toString()
                    }

                    var m_string: String
                    if(m < 10){
                        m_string = "0" + (m).toString()
                    }
                    else{
                        m_string = (m).toString()
                    }

                    var s_string: String
                    if(s < 10){
                        s_string = "0" + (s).toString()
                    }
                    else{
                        s_string = (s).toString()
                    }

                    var total_time_string = h_string + ":" + m_string + ":" + s_string
                    barList.set(dayValue,total_time_string)
                }

                Log.d("Ye0n", "list[idx].basicTimer == ${list[idx].basicTimer}")


                //Log.d("s00","list[idx].date = ${list[idx].date} ${list[idx].basicTimer}, ${list[idx].pomodoro}")
            }
        }
        lineList.set(0,totalPomodoro.toString())
        barList.set(0, totalBasic.toString())

        //Log.d("ye0n", "barList_size == ${barList.size}")

        onLineChart(lineList)
        onBarChart(barList)

    }

}