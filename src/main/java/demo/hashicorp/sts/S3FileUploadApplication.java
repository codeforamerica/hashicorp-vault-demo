package demo.hashicorp.sts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
public class S3FileUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3FileUploadApplication.class, args);
    }
}
