package com.cookandroid.capstone2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import data.StudyDatabase
import data.StudyPlan
import kotlinx.coroutines.launch

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = StudyDatabase.getDatabase(application).studyPlanDao()

    val allPlans: LiveData<List<StudyPlan>> = dao.getAllPlans()

    fun getPlansByDate(date: String): LiveData<List<StudyPlan>> {
        return dao.getPlansByDate(date)
    }

    fun insertPlan(plan: StudyPlan) = viewModelScope.launch {
        dao.insertPlan(plan)
    }

    fun updatePlan(plan: StudyPlan) = viewModelScope.launch {
        dao.updatePlan(plan)
    }

    fun deletePlan(plan: StudyPlan) = viewModelScope.launch {
        dao.deletePlan(plan)
    }

    fun toggleComplete(plan: StudyPlan) = viewModelScope.launch {
        dao.updatePlan(plan.copy(isCompleted = !plan.isCompleted))
    }
}