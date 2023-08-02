package com.example.repository

import com.example.utils.model.entities.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Transactional
@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun existsUserByLogin(login: String): Boolean

    fun existsUserByLoginOrEmailAndActivated(login: String, email: String, activated: Boolean): Boolean

    fun existsUserByLoginAndPasswordAndEmailAndActivated(
        login: String,
        password: String,
        email: String,
        activated: Boolean
    ): Boolean

    fun getUserByEmail(email: String): User?

    fun getUserByLogin(login: String): User?

    fun existsUserByActivationCode(activationCode: String): Boolean

    fun getUserByActivationCode(code: String): User?

    @Modifying
    @Query("update User u set u.activated = :activated where u.activationCode = :activationCode")
    fun updateActivatedByActivationCode(
        @Param("activated") activated: Boolean,
        @Param("activationCode") activationCode: String
    )
}
