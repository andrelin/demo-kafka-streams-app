package no.lindjo.model

data class ListDto(
        val strings: ArrayList<String> = ArrayList()
) {
    constructor() : this(ArrayList())
}