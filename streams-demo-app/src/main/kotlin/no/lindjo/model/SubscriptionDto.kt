package no.lindjo.model

data class SubscriptionDto (
        val username: String,
        val subscribesTo:String
) {
    constructor() : this("", "")
}