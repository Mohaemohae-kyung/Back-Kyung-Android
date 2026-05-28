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

    // 백엔드 시드(depth=2 세부분류) ID 매핑. 대분류 ID + 시드 순서 기준.
    // v2: GET /api/categories 도입 후 동적 로드로 교체.
    val SUBCATEGORIES: Map<Long, List<Category>> = mapOf(
        1L to listOf(
            Category(2L, "취업 준비"),
            Category(3L, "창업 준비"),
            Category(4L, "시험/자격증"),
            Category(5L, "기타 실무"),
        ),
        6L to listOf(
            Category(7L, "음악이론/보컬"),
            Category(8L, "미술/드로잉"),
            Category(9L, "연기/마술"),
            Category(10L, "기타 취미/자기계발"),
        ),
        11L to listOf(
            Category(12L, "국내 입시"),
            Category(13L, "유학 준비"),
            Category(14L, "체육"),
            Category(15L, "무용/댄스"),
        ),
        16L to listOf(
            Category(17L, "디자인 외주"),
            Category(18L, "개발 외주"),
            Category(19L, "번역 외주"),
            Category(20L, "마케팅"),
        ),
        21L to listOf(
            Category(22L, "심리"),
            Category(23L, "번역 작업"),
            Category(24L, "심부름"),
        ),
    )

    fun subcategoriesOf(mainCategoryId: Long): List<Category> =
        SUBCATEGORIES[mainCategoryId].orEmpty()

    fun subcategoryByName(name: String): Category? =
        SUBCATEGORIES.values.flatten().firstOrNull { it.name == name }
}
