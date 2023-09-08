package demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
@ConditionalOnProperty(name="spring.cloud.vault.aws.enabled")
@RefreshScope
public class AWSConfiguration {

    @Autowired
    AwsConfigurationProperties awsConfigurationProperties;

    @Value("${cloud.aws.region}")
    private String region;

    @Bean
    @RefreshScope
    public AWSSessionCredentials basicAWSCredentials() {
        return new BasicSessionCredentials(awsConfigurationProperties.getAccesskey(),
                awsConfigurationProperties.getSecretKey(), awsConfigurationProperties.getSessionToken());
    }

    @Bean
    @RefreshScope
    public AmazonS3 amazonS3Client(AWSCredentials awsCredentials){
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();
    }
}
