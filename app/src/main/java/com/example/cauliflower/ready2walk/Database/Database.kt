package com.example.cauliflower.ready2walk.Database

import android.content.Context
import androidx.room.*

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
    entities = arrayOf(Sessions::class),
    version = 1,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class SessionsDatabase : RoomDatabase(){ //inherit from room database

    //get DAOs
    abstract fun getSessionsDao(): SessionDao

    companion object{
        @Volatile //TO BE ACCESSED AT ALL THREADS
        private var instance : SessionsDatabase? = null
        private val LOCK = Any()
        //check if instance is null, if so create database and assign istance
        operator fun invoke(context: Context) = instance
                ?: synchronized(LOCK){
            instance
                    ?: buildDatabase(context).also {
                instance = it
            }
        }
        //function to create a room database
        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            SessionsDatabase::class.java,
            "sessiondatabase"
        ).build()
    }

}