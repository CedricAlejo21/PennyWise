package com.mobdeve.mco.pennywise

data class Transaction(
    val id: String? = null,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val location: String? = null,
    val date: String? = null
) {
    constructor() : this(null, null, null, null, null, null)
}
