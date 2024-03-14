package ru.kaplaan.authserver.domain.entity

import jakarta.persistence.*
import ru.kaplaan.domain.domain.user.User

@Entity
class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var refreshTokenId: Int? = null

    @Column(length = 1024)
    lateinit var token: String

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    lateinit var user: User
}
