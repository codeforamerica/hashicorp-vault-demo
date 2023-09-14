package demo.hashicorp.sts.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@ConditionalOnProperty(name="spring.cloud.vault.aws.enabled")
@RefreshScope
public class AWSConfiguration {

    @Autowired
    AwsConfigurationProperties awsConfigurationProperties;

    @Bean
    @RefreshScope
    public AWSSessionCredentials basicAWSCredentials() {
        return new BasicSessionCredentials(awsConfigurationProperties.getAccesskey(),
                awsConfigurationProperties.getSecretKey(), awsConfigurationProperties.getSessionToken());
    }

    @Bean
    @RefreshScope
    public AmazonS3 amazonS3Client(AWSCredentials awsCredentials){
        String region = "us-east-1";
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();
    }
}
