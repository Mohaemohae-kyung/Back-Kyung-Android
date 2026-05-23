package kyung.kung_android.domain.location.model

data class Region(
    val id: Long,
    val name: String,
)

object Regions {

    // 백엔드 시드(depth=1 광역) ID 매핑. LocationDataInitializer 순서 기준.
    // v2: GET /api/locations 엔드포인트 도입 후 동적 로드로 교체. 세부 구·시(depth=2)도 v2에서 추가.
    val ALL: List<Region> = listOf(
        Region(2L, "서울"),
        Region(20L, "경기"),
        Region(28L, "인천"),
        Region(33L, "강원"),
        Region(39L, "충북"),
        Region(43L, "충남"),
        Region(47L, "경북"),
        Region(51L, "경남"),
        Region(55L, "대전"),
        Region(57L, "대구"),
        Region(59L, "광주"),
        Region(61L, "부산"),
        Region(63L, "울산"),
        Region(65L, "전북"),
        Region(67L, "전남"),
        Region(69L, "세종"),
        Region(71L, "제주"),
    )

    fun byId(id: Long): Region? = ALL.firstOrNull { it.id == id }
}
