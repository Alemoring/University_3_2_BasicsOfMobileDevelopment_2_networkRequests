package com.example.lw4_3

import com.example.lw4_3.domain.Ride

interface RideActionListener {
    fun onRideGetId(person: Ride)
    fun onRideEdit(person: Ride, login: String, distance: String)
    fun onRideRemove(person: Ride)
    fun onRideAdd(login: String, distance: String)
}