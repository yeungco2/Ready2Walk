package com.example.cauliflower.ready2walk.Database

import androidx.room.*

@Dao
interface SessionDao {

    //suspend functions because of coroutines
    @Query("SELECT * from sessions_table ORDER By id DESC" )
    suspend fun getAllSessions(): List<Sessions>

    @Insert
    suspend fun addSession(sessions: Sessions)

    @Delete
    suspend fun deleteSession(sessions: Sessions)

    @Update
    suspend fun updateSession (sessions: Sessions)

}