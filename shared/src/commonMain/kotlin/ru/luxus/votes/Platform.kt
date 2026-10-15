package ru.luxus.votes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform