package data.network

sealed class ResponseWrapper<out T> {
    data class Success<out T>(val data: T) : ResponseWrapper<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ResponseWrapper<Nothing>()
    object EmptySuccess : ResponseWrapper<Nothing>()
}
