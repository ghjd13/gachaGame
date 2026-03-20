package com.example.gachagame

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
// 정확한 패키지명으로 바인딩 클래스 임포트
import com.example.gachagame.databinding.ActivityMainBinding
import org.json.JSONObject // JSON 파싱을 위해 추가

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

// 1. 시스템 바 숨기기 (상태 표시줄 + 네비게이션 바 모두)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

// 2. 화면 스와이프 시 일시적으로 나타났다가 다시 사라지는 설정 (Immersive Mode)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 게임이 켜질 때 기본 캐릭터 데이터(JSON)를 세팅
        // (이미 데이터가 생성되어 있다면 알아서 무시하고 넘어갑니다)
        CharacterDataManager.initDataIfNeeded(this)

        // ==========================================
        // [1] JSON 데이터 로드 및 적용
        // ==========================================
        // 실제 게임에서는 서버 통신이나 로컬 파일(SharedPreferences/assets)에서 가져옵니다.
        // 지금은 테스트를 위해 가상의 JSON 문자열을 만들어 사용합니다.
        val dummyUserJson = """
            {
                "nickname": "초보마법사",
                "level": 12,
                "uid": "98765432",
                "character": "character_2_ld"
            }
        """.trimIndent()

        loadUserDataFromJson(dummyUserJson)

        // ==========================================
        // [2] 화면 이동 버튼 이벤트
        // ==========================================
        binding.btnSummon.setOnClickListener {
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("DESTINATION", "SUMMON")
            startActivity(intent)
        }

        binding.btnBattle.setOnClickListener {
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("DESTINATION", "BATTLE")
            startActivity(intent)
        }
    }

    // JSON 문자열을 분석하여 뷰 바인딩으로 화면에 적용하는 함수
    private fun loadUserDataFromJson(jsonString: String) {
        try {
            // 1. 텍스트를 JSON 객체로 변환
            val jsonObject = JSONObject(jsonString)

            // 2. 키(key) 값을 이용해 데이터 추출
            val nickname = jsonObject.getString("nickname")
            val level = jsonObject.getInt("level")
            val uid = jsonObject.getString("uid")

            // 3. UI에 적용
            binding.tvNickname.text = nickname
            binding.tvLevelNumber.text = level.toString() // 숫자는 문자로 변환해서 넣어야 함
            binding.tvUidNumber.text = uid
            // UID 라벨("UID:")은 고정이므로 번호 영역만 변경해줍니다.

            // (참고) 이미지 이름도 JSON으로 불러와 리소스 ID를 찾아 변경할 수 있습니다.
            // val characterImageName = jsonObject.getString("character")
            // val resourceId = resources.getIdentifier(characterImageName, "drawable", packageName)
            // binding.imgCharacter.setImageResource(resourceId)

        } catch (e: Exception) {
            e.printStackTrace() // JSON 형식이 잘못되었을 때 오류 로그 출력
        }
    }
}