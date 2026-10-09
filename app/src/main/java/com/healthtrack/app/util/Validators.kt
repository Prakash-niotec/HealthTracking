package com.healthtrack.app.util

import android.util.Patterns

object Validators {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidWeight(weightKg: Float): Boolean {
        return weightKg in 20.0f..300.0f
    }
}
