package example.config

import example.security.Secrets
import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig {
    @Bean
    fun minioClient(@Value("\${minio.endpoint}") endpoint: String): MinioClient =
        MinioClient.builder()
            .endpoint(endpoint)
            .credentials(Secrets.read("minio_app_user"), Secrets.read("minio_app_password"))
            .build()
}
