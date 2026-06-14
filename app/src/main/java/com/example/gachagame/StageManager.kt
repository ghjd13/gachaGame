package com.example.gachagame

object StageManager {
    val clearedStages = mutableSetOf<String>()

    fun clearStage(stageId: String) {
        clearedStages.add(stageId)
    }

    fun isUnlocked(stageId: String): Boolean {
        return when (stageId) {
            "1-1" -> true
            "1-2" -> clearedStages.contains("1-1")
            "1-3" -> clearedStages.contains("1-2")
            else -> false
        }
    }
}