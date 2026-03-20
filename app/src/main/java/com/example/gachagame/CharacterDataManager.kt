package com.example.gachagame

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ========================================================
// 1. 캐릭터 정보의 '틀'을 만듭니다. (데이터 클래스)
// 나중에 레벨, 속성 등을 추가하고 싶으면 여기에 줄만 추가하면 끝입니다!
// ========================================================
data class CharacterData(
    val id: Int,
    var name: String,
    var isAcquired: Boolean
)

object CharacterDataManager {
    private const val PREFS_NAME = "gacha_prefs"
    private const val KEY_CHARACTERS_JSON = "characters_json"

    // Gson 마법사 객체 생성
    private val gson = Gson()

    // 2. 앱을 처음 실행했을 때 기본 데이터 생성
    fun initDataIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.contains(KEY_CHARACTERS_JSON)) {
            // 코틀린의 map 기능을 써서 1~20번 캐릭터를 순식간에 리스트로 만듭니다.
            val characterList = (1..20).map { i ->
                CharacterData(id = i, name = "캐릭터 $i", isAcquired = false)
            }

            // 리스트를 단 한 줄만에 JSON 글자로 변환!
            val jsonString = gson.toJson(characterList)
            prefs.edit().putString(KEY_CHARACTERS_JSON, jsonString).apply()
        }
    }

    // 3. 가챠에서 캐릭터를 뽑았을 때 획득 상태 갱신
    fun acquireCharacter(context: Context, characterId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_CHARACTERS_JSON, "[]")

        // JSON 글자를 다시 우리가 만든 CharacterData 리스트로 변환!
        val type = object : TypeToken<MutableList<CharacterData>>() {}.type
        val characterList: MutableList<CharacterData> = gson.fromJson(jsonString, type)

        // 리스트에서 ID가 일치하는 캐릭터를 찾아 isAcquired를 true로 바꿈
        val character = characterList.find { it.id == characterId }
        character?.isAcquired = true

        // 다시 JSON으로 변환해서 저장
        val updatedJsonString = gson.toJson(characterList)
        prefs.edit().putString(KEY_CHARACTERS_JSON, updatedJsonString).apply()
    }

    // 4. (보너스) 도감 화면 같은 곳에서 특정 캐릭터를 얻었는지 확인할 때 쓰는 함수
    fun isCharacterAcquired(context: Context, characterId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_CHARACTERS_JSON, "[]")

        val type = object : TypeToken<List<CharacterData>>() {}.type
        val characterList: List<CharacterData> = gson.fromJson(jsonString, type)

        // 해당 아이디를 찾고, 있으면 획득 여부를 반환 (못 찾으면 기본값 false)
        return characterList.find { it.id == characterId }?.isAcquired ?: false
    }
}