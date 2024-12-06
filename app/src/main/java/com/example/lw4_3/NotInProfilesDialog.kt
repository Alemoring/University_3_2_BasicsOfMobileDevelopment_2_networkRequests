package com.example.lw4_3

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class NotInProfilesDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        return builder.setTitle("Не найден пользователь")
            .setMessage("Пользователя с данным именем не существует")
            .setPositiveButton("ОК", null)
            .create()
    }
}