package no.realitylab.arface.data.models

data class UserData(
    val userId: String?,
    val userName: String?,
    val profilePictureUrl: String?,
    val userEmail: String?,
    val phone: Int = 0,
    val photos: List<Photo> = emptyList()
)
