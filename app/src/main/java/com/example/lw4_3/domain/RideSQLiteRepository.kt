package com.example.lw4_3.domain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.annotation.RequiresApi
import kotlin.math.log


class RideSQLiteRepository(val context: Context?, val version: Int = 1, val dbName: String = "Rides.db"): SQLiteOpenHelper(context, dbName, null, version) {
    val TABLE_USER: String = "users"
    val USER_ID: String = "id"
    val USER_LOGIN: String = "login"
    val USER_PASSWORD: String = "password"
    val CREATE_TABLE_USER: String =
        ("CREATE TABLE IF NOT EXISTS " + TABLE_USER + "(" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_LOGIN + " TEXT NOT NULL,"
                + USER_PASSWORD + " TEXT NOT NULL, UNIQUE($USER_LOGIN));")
    val DROP_TABLE_USER: String = "DROP TABLE IF EXISTS $TABLE_USER"

    val TABLE_RIDE: String = "rides"
    val RIDE_ID: String = "id"
    val RIDE_LOGIN: String = "login"
    val RIDE_DISTANCE: String = "distance"
    val CREATE_TABLE_RIDE: String =
        ("CREATE TABLE IF NOT EXISTS " + TABLE_RIDE + "(" +
                RIDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RIDE_LOGIN + " TEXT NOT NULL,"
                + RIDE_DISTANCE + " TEXT NOT NULL, " +
                "FOREIGN KEY ($RIDE_LOGIN)  REFERENCES $TABLE_USER ($USER_LOGIN));")
    val DROP_TABLE_RIDE: String = "DROP TABLE IF EXISTS $TABLE_RIDE"

    private var listeners = mutableListOf<RideListener>() // Все слушатели

    private var rides = mutableListOf<Ride>()

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            db!!.execSQL(CREATE_TABLE_USER)
            db.execSQL(DROP_TABLE_RIDE)
            db.setForeignKeyConstraintsEnabled(true)
            //db.execSQL("INSERT OR IGNORE INTO users(login, password) VALUES ('Alemor', '123');")
            db.execSQL(CREATE_TABLE_RIDE)

            //db.execSQL("INSERT OR IGNORE INTO rides(login, distance) VALUES ('Alemor', '123');")
        } catch (e: Exception) {
            Log.e("error onCreate", e.toString())
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL(DROP_TABLE_USER)
        db.execSQL(DROP_TABLE_RIDE)
        onCreate(db)
    }
    fun createRide(ride: Ride){
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(RIDE_LOGIN, ride.login)
        cv.put(RIDE_DISTANCE, ride.distance)

        db.setForeignKeyConstraintsEnabled(true)
        val result = db.insert(TABLE_RIDE, null, cv)
        getRides()
        notifyChanges()
        Log.i("create Ride", result.toString())
        //return if (result == -1L) false
        //else true
    }
    fun getRides(): MutableList<Ride> {
        rides = mutableListOf<Ride>()
        val db = this.readableDatabase
        val query = db.rawQuery("SELECT * FROM $TABLE_RIDE", null)
        while (query.moveToNext()) {
            val id = query.getInt(0)
            val login = query.getString(1)
            val distance = query.getString(2)
            rides.add(Ride(id.toLong(), login, distance))
        }
        query.close()
        db.close()
        return rides
    }

    fun removeRide(ride: Ride){
        var index = ride.id // Находим индекс поездки в списке
        val db = this.writableDatabase
        val result = db.delete(TABLE_RIDE, "id=?", arrayOf(index.toString()))
        getRides()
        notifyChanges()
        //return result
    }
    fun editRide(ride: Ride, login: String, distance: String){
        val index = ride.id // Находим индекс поездки в списке
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(RIDE_LOGIN, login)
        cv.put(RIDE_DISTANCE, distance)
        db.setForeignKeyConstraintsEnabled(true)
        val result = db.update(TABLE_RIDE, cv, "id=?", arrayOf(index.toString()))
        getRides()
        notifyChanges()
        //return result
    }

    fun addListener(listener: RideListener) {
        listeners.add(listener)
        listener.invoke(rides)
    }
    fun removeListener(listener: RideListener) {
        listeners.remove(listener)
        listener.invoke(rides)
    }
    private fun notifyChanges() = listeners.forEach { it.invoke(rides) }
}
typealias RideListener = (rides: List<Ride>) -> Unit