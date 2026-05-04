package data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudyPlanDao {

    @Query("SELECT * FROM study_plans ORDER BY date ASC, id ASC")
    fun getAllPlans(): LiveData<List<StudyPlan>>

    @Query("SELECT * FROM study_plans WHERE date = :date ORDER BY id ASC")
    fun getPlansByDate(date: String): LiveData<List<StudyPlan>>

    @Query("SELECT * FROM study_plans WHERE date = :date ORDER BY id ASC")
    suspend fun getPlansByDateOnce(date: String): List<StudyPlan>

    @Query("SELECT COUNT(*) FROM study_plans WHERE date = :date")
    suspend fun getPlanCountByDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlan)

    @Update
    suspend fun updatePlan(plan: StudyPlan)

    @Delete
    suspend fun deletePlan(plan: StudyPlan)
}