package com.carniceria.shared.shared.models.utils

object Validator {

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        return emailRegex.matches(email)
    }

    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = Regex("^\\d{9}$")
        return phoneRegex.matches(phone)
    }

    fun isValidPostalCode(postalCode: String): Boolean {
        val postalRegex = Regex("^\\d{5}$")
        return postalRegex.matches(postalCode)
    }

    fun isNotEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }
}