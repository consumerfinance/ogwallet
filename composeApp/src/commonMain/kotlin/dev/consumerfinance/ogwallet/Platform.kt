package dev.consumerfinance.ogwallet

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform