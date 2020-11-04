package no.lindjo.model

data class MailDto(
        val mail: String,
        val title: String
) {
    constructor() : this("", "")
}
