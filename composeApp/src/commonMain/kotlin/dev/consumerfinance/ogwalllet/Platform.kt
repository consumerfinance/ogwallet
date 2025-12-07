package dev.consumerfinance.walllet

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform