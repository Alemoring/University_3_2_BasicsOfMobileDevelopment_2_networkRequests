package com.example.lw4_3.domain

import com.example.lw4_2.domain.Account

class AccountNetworkRepository {
    var accounts: ArrayList<Account> = arrayListOf(Account("Alemor", "123", "hardcode@example.com"))
    fun getAllaccounts(): ArrayList<Account> {
        return accounts
    }
    fun findAccoundByName(name: String): Account? {
        for (account in accounts){
            if (account.name == name){
                return account
            }
        }
        return null
    }
}