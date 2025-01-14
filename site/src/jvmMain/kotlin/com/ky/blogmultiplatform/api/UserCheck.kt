package com.ky.blogmultiplatform.api

import com.ky.blogmultiplatform.data.MongoDB
import com.ky.blogmultiplatform.models.User
import com.ky.blogmultiplatform.models.UserWithoutPassword
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.data.getValue
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

/**
 * Check if the user exists in the database.
 */
@Api(routeOverride = "usercheck")
suspend fun userCheck(context: ApiContext) {
    try {
        val userRequest =
            context.req.body?.decodeToString()?.let { Json.decodeFromString<User>(it) }
        val user = userRequest?.let {
            context.data.getValue<MongoDB>().checkUserExistence(
                User(username = it.username, password = hashPassword(it.password))
            )
        }
        if (user != null) {
            context.res.setBodyText(
                Json.encodeToString(
                    UserWithoutPassword(
                        id = user.id,
                        username = user.username
                    )
                )
            )
        } else {
            context.res.setBodyText(Json.encodeToString(Exception("User does not exist")))
        }
    } catch (e: Exception) {
        context.res.setBodyText(Json.encodeToString(Exception(e.message)))
    }
}

/**
 * Check if the user id exists in the database.
 */
@Api(routeOverride = "checkuserid")
suspend fun checkUserId(context: ApiContext){
    try {
        val idRequest = context.req.body?.decodeToString()?.let { Json.decodeFromString<String>(it) }
        val result = idRequest?.let {
            context.data.getValue<MongoDB>().checkUserId(it)
        }
        if(result != null){
            context.res.setBodyText(Json.encodeToString(result))
        }else{
            context.res.setBodyText(Json.encodeToString(false))
        }
    }catch (e: Exception){
        context.res.setBodyText(Json.encodeToString(false))
    }
}

/**
 * Hash a password using SHA-256 algorithm.
 */
private fun hashPassword(password: String): String {
    val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
    val hashByte = messageDigest.digest(password.toByteArray(StandardCharsets.UTF_8))
    val hexString = StringBuffer()

    for( byte in hashByte){
        hexString.append(String.format("%02x", byte))
    }

    return hexString.toString()
}