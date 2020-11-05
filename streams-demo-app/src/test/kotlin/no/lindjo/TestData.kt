package no.lindjo

import no.lindjo.model.UserDto

class TestData {
    companion object {
        fun testUser(tmp: String): UserDto {
            return UserDto(username = "user$tmp", name = "$tmp ${tmp}son", mail = "$tmp@gmail.com")
        }
    }
}
