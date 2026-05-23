package kyung.kung_android.domain.category.model

data class Category(
    val id: Long,
    val name: String,
)

object Categories {

    // 백엔드 시드(depth=1 대분류) ID 매핑. ServiceCategoryDataInitializer 순서 기준.
    // v2: GET /api/categories 엔드포인트 도입 후 동적 로드로 교체.
    val ALL: List<Category> = listOf(
        Category(1L, "취업/직무"),
        Category(6L, "취미/자기계발"),
        Category(11L, "과외"),
        Category(16L, "외주"),
        Category(21L, "기타"),
    )

    fun byId(id: Long): Category? = ALL.firstOrNull { it.id == id }
}
