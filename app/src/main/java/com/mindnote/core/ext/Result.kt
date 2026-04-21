package com.mindnote.core.ext

import android.util.Log
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object ApiError {
    const val UNKNOWN = "unknown"
    const val NETWORK = "network"
    const val SERVER = "server"
}

@Serializable
data class ErrorModel(
    val code: String? = null,
    val message: String? = null,
)

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(
        val code: String,
        val message: String,
        val exception: Throwable? = null,
    ) : Result<Nothing>
    data object Loading : Result<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}

fun <T> Result<T>.getDataOrNull(): T? = (this as? Result.Success)?.data

fun Result.Error.userMessage(fallback: String = "Something went wrong"): String = when {
    code == ApiError.NETWORK -> "Check your connection and try again."
    code == ApiError.SERVER -> "Server error — please try again."
    message.isNotBlank() -> message
    else -> fallback
}

inline fun <T> Result<T>.handle(
    onSuccess: (T) -> Unit,
    onError: (Result.Error) -> Unit,
    onLoading: () -> Unit = {},
) {
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(this)
        Result.Loading -> onLoading()
    }
}

suspend fun <T> Result<T>.handleSuspend(
    onSuccess: suspend (T) -> Unit,
    onError: suspend (Result.Error) -> Unit,
    onLoading: suspend () -> Unit = {},
) {
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(this)
        Result.Loading -> onLoading()
    }
}

/** Wraps a suspending API call and funnels any thrown exception into [Result.Error]. */
suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (t: Throwable) {
    t.logAsNonFatal()
    t.toResult()
}

/** Map any Throwable into a typed [Result.Error] — parses server `ErrorModel` JSON on 4xx. */
suspend fun Throwable.toResult(): Result.Error = try {
    when (this) {
        is ClientRequestException -> {
            val body = runCatching { response.bodyAsText() }.getOrNull()
            val parsed = body?.takeIf { it.isNotBlank() }?.let {
                runCatching { errorJson.decodeFromString<ErrorModel>(it) }.getOrNull()
            }
            if (parsed == null) {
                Result.Error(ApiError.UNKNOWN, "", this)
            } else {
                Result.Error(parsed.code.orEmpty(), parsed.message.orEmpty(), this)
            }
        }
        is ServerResponseException -> Result.Error(ApiError.SERVER, "", this)
        is UnknownHostException,
        is SSLException,
        is InterruptedIOException,
        is ConnectException -> Result.Error(ApiError.NETWORK, "", this)
        else -> Result.Error(ApiError.UNKNOWN, message.orEmpty(), this)
    }
} catch (e: Exception) {
    Result.Error(ApiError.UNKNOWN, "", e)
}

/** Turn a `Flow<T>` into a `Flow<Result<T>>` that emits Loading → Success / Error. */
inline fun <reified T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { t ->
            t.logAsNonFatal()
            emit(t.toResult())
        }

/** Hook point for crash reporting — currently logs to Logcat. Swap for Crashlytics etc. when wired. */
fun Throwable.logAsNonFatal() {
    Log.e(LOG_TAG, message.orEmpty(), this)
}

private const val LOG_TAG = "MindNote"
private val errorJson = Json { ignoreUnknownKeys = true }
