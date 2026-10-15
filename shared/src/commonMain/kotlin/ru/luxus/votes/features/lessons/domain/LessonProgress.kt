package ru.luxus.votes.features.lessons.domain

data class LessonProgress(
    val viewedWordIds: Set<String> = emptySet()
) {
    fun copyWithViewed(wordId: String): LessonProgress {
        return LessonProgress(viewedWordIds = viewedWordIds + wordId)
    }
}
