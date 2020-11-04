package no.lindjo.model

data class PostDto(
        val title: String,
        val username: String
){
    constructor() : this("", "")
}