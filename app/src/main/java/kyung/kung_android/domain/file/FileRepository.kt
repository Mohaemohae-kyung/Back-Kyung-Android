package kyung.kung_android.domain.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kyung.kung_android.data.file.api.FileApi
import kyung.kung_android.data.file.dto.FileUploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    private val fileApi: FileApi,
    @ApplicationContext private val context: Context,
) {

    suspend fun uploadImage(uri: Uri, domain: String): FileUploadResponse {
        val tempFile = copyToCache(uri)
        val contentType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = tempFile.name,
            body = tempFile.asRequestBody(contentType.toMediaTypeOrNull()),
        )
        return try {
            fileApi.uploadFile(file = part, domain = domain)
        } finally {
            tempFile.delete()
        }
    }

    private fun copyToCache(uri: Uri): File {
        val resolver: ContentResolver = context.contentResolver
        val extension = resolver.getType(uri)
            ?.substringAfterLast('/', "bin")
            ?.let { if (it == "jpeg") "jpg" else it }
            ?: "bin"
        val temp = File.createTempFile("upload_", ".$extension", context.cacheDir)
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open uri: $uri" }
            FileOutputStream(temp).use { output -> input.copyTo(output) }
        }
        return temp
    }
}
