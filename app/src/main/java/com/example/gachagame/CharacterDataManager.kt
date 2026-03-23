package com.example.gachagame

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CharacterData(
    val id: Int,
    var name: String,
    var isAcquired: Boolean
)

object CharacterDataManager {
    private const val PREFS_NAME = "gacha_prefs"
    private const val KEY_CHARACTERS_JSON = "characters_json"
    private const val KEY_FORMATION_JSON = "formation_json" // 편성 데이터용 키 추가

    private val gson = Gson()

    // 1. 앱을 처음 실행했을 때 기본 데이터 생성
    fun initDataIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 도감 초기화
        if (!prefs.contains(KEY_CHARACTERS_JSON)) {
            val characterList = (1..20).map { i ->
                CharacterData(id = i, name = "캐릭터 $i", isAcquired = false)
            }
            val jsonString = gson.toJson(characterList)
            prefs.edit().putString(KEY_CHARACTERS_JSON, jsonString).apply()
        }

        // 편성 초기화 (기본으로 1번, 2번, 3번 캐릭터 배치)
        if (!prefs.contains(KEY_FORMATION_JSON)) {
            val defaultFormation = listOf(1, 2, 3)
            val jsonString = gson.toJson(defaultFormation)
            prefs.edit().putString(KEY_FORMATION_JSON, jsonString).apply()
        }
    }

    // 2. 현재 편성된 캐릭터 ID 리스트 불러오기 (크기는 3이라고 가정)
    fun getFormation(context: Context): List<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_FORMATION_JSON, "[1, 2, 3]")
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    // 3. 편성 데이터 저장하기 (나중에 편성 화면 만들 때 사용)
    fun saveFormation(context: Context, formation: List<Int>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = gson.toJson(formation)
        prefs.edit().putString(KEY_FORMATION_JSON, jsonString).apply()
    }

    // 기존 획득 관련 함수들 유지
    fun acquireCharacter(context: Context, characterId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_CHARACTERS_JSON, "[]")
        val type = object : TypeToken<MutableList<CharacterData>>() {}.type
        val characterList: MutableList<CharacterData> = gson.fromJson(jsonString, type)

        val character = characterList.find { it.id == characterId }
        character?.isAcquired = true

        val updatedJsonString = gson.toJson(characterList)
        prefs.edit().putString(KEY_CHARACTERS_JSON, updatedJsonString).apply()
    }

    fun isCharacterAcquired(context: Context, characterId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_CHARACTERS_JSON, "[]")
        val type = object : TypeToken<List<CharacterData>>() {}.type
        val characterList: List<CharacterData> = gson.fromJson(jsonString, type)
        return characterList.find { it.id == characterId }?.isAcquired ?: false
    }
}