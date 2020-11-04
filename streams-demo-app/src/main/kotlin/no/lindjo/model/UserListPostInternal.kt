package no.lindjo.model

data class UserListPostInternal(
        val post: PostDto,
        val userList: ListDto
) {
    constructor() : this(PostDto("", ""), ListDto(ArrayList()))
}