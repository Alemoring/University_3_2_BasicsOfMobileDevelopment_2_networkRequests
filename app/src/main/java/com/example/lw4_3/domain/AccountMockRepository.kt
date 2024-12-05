package com.example.lw4_2.domain

class AccountMockRepository {
    var accounts: ArrayList<Account> = arrayListOf(Account("Alemor", "123"))
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