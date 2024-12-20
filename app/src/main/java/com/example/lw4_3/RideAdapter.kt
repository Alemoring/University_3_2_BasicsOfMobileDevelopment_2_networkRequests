package com.example.lw4_3

import android.annotation.SuppressLint
import android.health.connect.datatypes.ExerciseCompletionGoal.DistanceGoal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lw4_3.domain.Ride
import com.example.lw4_3.databinding.RideItemBinding

class RideAdapter(private val rideActionListener: RideActionListener): RecyclerView.Adapter<RideAdapter.RideViewHolder>(), View.OnClickListener  {
    var data: List<Ride> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class RideViewHolder(val binding: RideItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RideItemBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
        binding.deleteIconView.setOnClickListener(this)
        return RideViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = data[position] // Получение поездки из списка данных по позиции
        holder.itemView.tag = ride
        holder.binding.deleteIconView.tag = ride
        val context = holder.itemView.context

        with(holder.binding) {
            loginTextView.text = ride.login // Отрисовка имени пользователя
            distanceTextView.text = ride.distance + " км" // Отрисовка дистанции пользователя
        }
    }

    override fun onClick(v: View?) {
        val ride: Ride = v?.tag as Ride // Получаем из тэга Поездки

        when (v.id) {
            R.id.deleteIconView -> rideActionListener.onRideRemove(ride)
            else -> rideActionListener.onRideGetId(ride)
        }
    }

}