package ru.kaplaan.service.impl

import org.springframework.stereotype.Service
import ru.kaplaan.domain.entity.RefreshToken
import ru.kaplaan.domain.entity.User
import ru.kaplaan.domain.exception.user.*
import ru.kaplaan.domain.jwt.JwtProvider
import ru.kaplaan.domain.user.UserIdentification
import ru.kaplaan.repository.RefreshTokenRepository
import ru.kaplaan.repository.UserRepository
import ru.kaplaan.service.AuthService
import ru.kaplaan.service.CryptoService
import ru.kaplaan.service.EmailService
import ru.kaplaan.web.dto.response.jwt.JwtResponse
import java.util.*

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailService: EmailService,
    private val cryptoService: CryptoService,
    private val jwtProvider: JwtProvider,
) : AuthService {

    override fun registration(user: User) {
        checkRegistration(user)
        val hashedPassword: String = cryptoService.doHash(user)
        val activationCode = UUID.randomUUID().toString().replace("-", "")
        user.setPassword(hashedPassword)
        user.setActivationCode(activationCode)
        emailService.activateUserByEmail(user.getEmail()!!, user.getLogin()!!, activationCode)
        userRepository.save(user)
    }

    override fun login(userIdentification: UserIdentification): JwtResponse {
        val user = checkLogin(userIdentification)
        val accessToken = jwtProvider.generateJwtAccessToken(user)
        val refreshToken = jwtProvider.generateJwtRefreshToken(user)
        saveRefreshToken(user.getLogin()!!, refreshToken)
        return JwtResponse(accessToken, refreshToken)
    }

    override fun activateAccount(code: String) {
        val user = userRepository.getUserByActivationCode(code) ?: throw NotFoundUserByActivationCode()
        user.setActivationCode(null)
        user.setActivated(true)
        userRepository.save(user)
    }

    // TODO: Полностью переделать восстановление пароля
    override fun passwordRecovery(userIdentification: UserIdentification) {
        val user = getUserByLoginOrEmail(userIdentification)
        val activationCode = UUID.randomUUID().toString().replace("-", "")
        user.setActivationCode(activationCode)
        emailService.recoveryPasswordByEmail(user.getEmail()!!, user.getLogin()!!, activationCode)
        userRepository.save(user)
    }

    private fun getUserByLoginOrEmail(userIdentification: UserIdentification): User {
        return userRepository.getUserByLogin(userIdentification.getLoginOrEmail())
            ?: (userRepository.getUserByEmail(userIdentification.getLoginOrEmail())
                ?: throw UserNotFoundException("Пользователь с такой почтой или логином не найден"))
    }

    private fun saveRefreshToken(login: String, token: String) {
        val refreshToken = RefreshToken(login, token)
        refreshTokenRepository.save(refreshToken)
    }

    private fun checkLogin(userIdentification: UserIdentification): User {
        val user = getUserByLoginOrEmail(userIdentification)
        val hashedPassword: String = cryptoService.getHash(user.getLogin()!!, userIdentification.getPassword())
        if (!userRepository.existsUserByLoginAndPasswordAndEmailAndActivated(
                user.getLogin()!!,
                hashedPassword,
                user.getEmail()!!,
                true
            )
        ) throw UserNotFoundException("Пользователь с данным логином и паролем не найден")
        return user
    }

    private fun checkRegistration(user: User): User {
        validateActivationCode(user.getActivationCode(), false)
        validateActivated(user.getActivated(), false)
        if (userRepository.existsUserByLoginOrEmailAndActivated(
                user.getLogin()!!,
                user.getEmail()!!,
                true
            )
        ) throw UserAlreadyRegisteredException("Пользователь с таким логином или паролем уже существует")
        return user
    }

    private fun validateActivationCode(activationCode: String?, mustExist: Boolean){
        if((mustExist && activationCode == null) || (!mustExist && activationCode != null))
            throw UnexpectedActivationCodeException()
    }

    private fun validateActivated(activated: Boolean, mustBe: Boolean){
        if(activated != mustBe)
            throw UnexpectedActivatedException()
    }
}
