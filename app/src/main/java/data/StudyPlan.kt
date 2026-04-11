package com.cookandroid.capstone2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subject: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val isCompleted: Boolean = false
)