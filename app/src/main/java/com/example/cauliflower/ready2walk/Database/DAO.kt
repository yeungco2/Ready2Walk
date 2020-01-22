package com.example.cauliflower.ready2walk.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {

    @Query("SELECT * from sessions_table")
    fun getAllSessions(): LiveData<List<Sessions>>

    @Insert
    fun addSession(sessions: Sessions)

    @Query("DELETE FROM sessions_table")
    fun deleteAll()
}
