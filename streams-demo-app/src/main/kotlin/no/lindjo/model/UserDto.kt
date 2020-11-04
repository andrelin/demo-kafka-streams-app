package no.lindjo.model

data class UserDto(
        val username: String,
        val name: String,
        val mail: String
) {
    constructor() : this("", "", "")
}