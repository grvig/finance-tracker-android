package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.grvig.financetracker.repository.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    suspend fun signUp(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return repository.signUp(email, password)
    }

    suspend fun signIn(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return repository.signIn(email, password)
    }

    fun signOut() {
        repository.signOut()
    }
}
