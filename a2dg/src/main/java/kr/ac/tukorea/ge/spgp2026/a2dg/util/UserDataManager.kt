package kr.ac.tukorea.ge.spgp2026.a2dg.util

import android.content.Context
import org.json.JSONObject

// 유저 정보를 담을 데이터 클래스 (엔진단에서 관리)
data class UserProfile(
    var nickname: String,
    var level: Int,
    var uid: String,
    var characterResName: String
)

// a2dg 엔진의 유틸리티로서 로컬 데이터(SharedPreferences) 입출력을 전담
object UserDataManager {
    private const val PREF_NAME = "GachaGameLocalData"
    private const val KEY_USER_PROFILE = "user_profile_json"

    fun loadUserData(context: Context): UserProfile {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedJsonString = prefs.getString(KEY_USER_PROFILE, null)

        return if (savedJsonString != null) {
            try {
                val json = JSONObject(savedJsonString)
                UserProfile(
                    nickname = json.getString("nickname"),
                    level = json.getInt("level"),
                    uid = json.getString("uid"),
                    characterResName = json.getString("character")
                )
            } catch (e: Exception) {
                e.printStackTrace()
                createDefaultProfile(context)
            }
        } else {
            createDefaultProfile(context)
        }
    }

    fun saveUserData(context: Context, profile: UserProfile) {
        val json = JSONObject().apply {
            put("nickname", profile.nickname)
            put("level", profile.level)
            put("uid", profile.uid)
            put("character", profile.characterResName)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_PROFILE, json.toString())
            .apply()
    }

    private fun createDefaultProfile(context: Context): UserProfile {
        val newProfile = UserProfile(
            nickname = "초보마법사",
            level = 1,
            uid = "10293847",
            characterResName = "character_1_ld"
        )
        saveUserData(context, newProfile)
        return newProfile
    }
}