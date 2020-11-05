package no.lindjo.model

data class SubscriptionDto(
        val username: String,
        val subscribeTo: String
) {
    constructor() : this("", "")
}