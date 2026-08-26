package audiobook.core.remoteconfig.api

interface FmcTokenProvider {

  suspend fun token(): String?
}
