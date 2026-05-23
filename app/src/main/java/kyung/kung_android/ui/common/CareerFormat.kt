package kyung.kung_android.ui.common

fun Double.toCareerYearLabel(): String =
    if (this == this.toLong().toDouble()) "${this.toLong()}년" else "${this}년"
