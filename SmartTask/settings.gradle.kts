import java.net.URI

fun configureProxyFromEnv() {
    val proxyCandidate = listOf("HTTPS_PROXY", "https_proxy", "HTTP_PROXY", "http_proxy")
        .mapNotNull(System::getenv)
        .firstOrNull()
        ?: return

    try {
        val proxyUri = URI(proxyCandidate)
        val host = proxyUri.host ?: return
        val port = if (proxyUri.port != -1) {
            proxyUri.port
        } else {
            when (proxyUri.scheme?.lowercase()) {
                "https" -> 443
                else -> 80
            }
        }

        System.setProperty("http.proxyHost", host)
        System.setProperty("https.proxyHost", host)
        System.setProperty("http.proxyPort", port.toString())
        System.setProperty("https.proxyPort", port.toString())

        proxyUri.userInfo?.let { userInfo ->
            val parts = userInfo.split(":", limit = 2)
            if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
                System.setProperty("http.proxyUser", parts[0])
                System.setProperty("https.proxyUser", parts[0])
            }
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                System.setProperty("http.proxyPassword", parts[1])
                System.setProperty("https.proxyPassword", parts[1])
            }
        }

        val nonProxyHosts = (System.getenv("NO_PROXY") ?: System.getenv("no_proxy"))
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.joinToString("|")

        nonProxyHosts?.let {
            System.setProperty("http.nonProxyHosts", it)
            System.setProperty("https.nonProxyHosts", it)
        }
    } catch (e: Exception) {
        println("Skipping proxy configuration due to invalid proxy value: ${e.message}")
    }
}

configureProxyFromEnv()

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartTask"
include(":app")
