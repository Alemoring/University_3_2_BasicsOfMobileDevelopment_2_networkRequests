package com.example.lw4_3

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AlreadyExistAccountDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        return builder.setTitle("Логин занят")
            .setMessage("Пользователь с данным логином уже существует")
            .setPositiveButton("ОК", null)
            .create()
    }
}