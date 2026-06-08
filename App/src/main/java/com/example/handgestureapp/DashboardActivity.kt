package com.example.handgestureapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.handgestureapp.databinding.ActivityDashboardBinding
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCharts()
        observeViewModel()
    }

    private fun setupCharts() {
        binding.barChart.description.isEnabled = false
        binding.pieChart.description.isEnabled = false
        binding.lineChart.description.isEnabled = false
    }

    private fun observeViewModel() {
        viewModel.systemStatus.observe(this) { status ->
            binding.tvSystemStatus.text = "Estado: $status"
        }

        viewModel.lastGesto.observe(this) { gesture ->
            binding.tvLastGesture.text = "Último gesto: $gesture"
        }

        viewModel.allEvents.observe(this) { events ->
            if (events.isNullOrEmpty()) return@observe

            updateBarChart(events)
            updatePieChart(events)
            updateLineChart(events)
        }
    }

    private fun updateBarChart(events: List<GestureEvent>) {
        val gestureCounts = events.groupBy { it.gestureNumber }
            .mapValues { it.value.size }

        val entries = mutableListOf<BarEntry>()
        (0..5).forEach { i ->
            entries.add(BarEntry(i.toFloat(), (gestureCounts[i] ?: 0).toFloat()))
        }

        val dataSet = BarDataSet(entries, "Gestos Detectados")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        
        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("0", "1", "2", "3", "4", "5"))
        binding.barChart.invalidate()
    }

    private fun updatePieChart(events: List<GestureEvent>) {
        val actionCounts = events.groupBy { it.actionType }
            .mapValues { it.value.size }

        val entries = actionCounts.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "Acciones")
        dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
    }

    private fun updateLineChart(events: List<GestureEvent>) {
        // Simple line chart showing event counts over time (grouped by minute)
        val timeGroups = events.groupBy { it.timestamp / 60000 }
            .mapValues { it.value.size }
            .toSortedMap()

        val entries = timeGroups.entries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value.toFloat())
        }

        val dataSet = LineDataSet(entries, "Eventos por minuto")
        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.BLUE)

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }
}
