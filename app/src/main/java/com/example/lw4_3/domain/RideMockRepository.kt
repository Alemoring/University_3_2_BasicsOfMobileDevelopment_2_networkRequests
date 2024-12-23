package com.example.lw4_3.domain

import android.R
import android.content.Context.MODE_PRIVATE
import android.database.sqlite.SQLiteDatabase
import android.widget.TextView
import com.github.javafaker.Faker


class RideMockRepository {
    private var rides = mutableListOf<Ride>() // Все пользователи

    init {

        /*val db: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase("app.db", null)
        db.execSQL("CREATE TABLE IF NOT EXISTS users (name TEXT, password TEXT, UNIQUE(name))")
        db.execSQL("CREATE TABLE IF NOT EXISTS rides (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT, distance TEXT, FOREIGN KEY (login)  REFERENCES users (name))")
        db.execSQL("INSERT OR IGNORE INTO users VALUES ('Alemor', '123');")
        db.execSQL("INSERT OR IGNORE INTO rides(login, distance) VALUES ('Alemor', '123');")

        val query = db.rawQuery("SELECT * FROM rides;", null)
        while (query.moveToNext()) {
            val id = query.getInt(0)
            val login = query.getString(1)
            val distance = query.getString(2)
            rides.add(Ride(id.toLong(), login, distance))
        }
        query.close()
        db.close()*/
        //val faker = Faker.instance() // Переменная для создания случайных данных

//        rides = (1..1).map {
//            Ride(
//                id = it.toLong(),
//                login = "Alemor",
//                distance = faker.number().randomNumber().toString()
//            )
//        }.toMutableList()
    }
    fun getRides(): MutableList<Ride> {
        return rides
    }
    fun removeRide(ride: Ride) {
        val index = rides.indexOfFirst { it.id == ride.id } // Находим индекс поездки в списке
        if (index == -1) return // Останавливаемся, если не находим такого человека

        rides = ArrayList(rides) // Создаем новый список
        rides.removeAt(index) // Удаляем поездку
        notifyChanges()
    }
    fun createRide(login: String, distance: String) {
        rides = ArrayList(rides) // Создаем новый список
        rides.add(Ride(rides.size.toLong()+1, login, distance))
        notifyChanges()
    }
    fun editRide(ride: Ride, login: String, distance: String) {
        val index = rides.indexOfFirst { it.id == ride.id } // Находим индекс поездки в списке
        if (index == -1) return // Останавливаемся, если не находим такого человека

        rides = ArrayList(rides) // Создаем новый список
        rides[index] = Ride(rides.size.toLong(), login, distance)
        notifyChanges()
    }
    fun clearRides(){
        rides = mutableListOf<Ride>()
    }

    private var listeners = mutableListOf<RideListener>() // Все слушатели

    fun addListener(listener: RideListener) {
        listeners.add(listener)
        listener.invoke(rides)
    }

    fun removeListener(listener: RideListener) {
        listeners.remove(listener)
        listener.invoke(rides)
    }

    fun getUsername() {

    }

    private fun notifyChanges() = listeners.forEach { it.invoke(rides) }
}

//typealias RideListener = (rides: List<Ride>) -> Unit