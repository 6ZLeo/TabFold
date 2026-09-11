package io.github.sixzleo.tabfold

object ConsentPolicy {
    fun canActivate(acceptedVersion: Int, currentVersion: Int, userEnabled: Boolean): Boolean =
        currentVersion > 0 && acceptedVersion == currentVersion && userEnabled
}
