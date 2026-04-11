package com.cookandroid.capstone2.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StudyPlanDao {

    @Query("SELECT * FROM study_plans ORDER BY date ASC, startTime ASC")
    fun getAllPlans(): LiveData<List<StudyPlan>>

    @Query("SELECT * FROM study_plans WHERE date = :date ORDER BY startTime ASC")
    fun getPlansByDate(date: String): LiveData<List<StudyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlan)

    @Update
    suspend fun updatePlan(plan: StudyPlan)

    @Delete
    suspend fun deletePlan(plan: StudyPlan)
}