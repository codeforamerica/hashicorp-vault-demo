package demo.hashicorp.sts;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import demo.hashicorp.sts.config.AwsConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UploadController {

    @Autowired
    private AmazonS3 amazonS3;

    private final String BUCKET_NAME = "la-vault-test-bucket";

    @Autowired
    private SecretLeaseContainer leaseContainer;

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }


    @PostMapping("/upload-file")
    public PutObjectResult uploadFile(@RequestPart(value="file") MultipartFile multipartFile) throws IOException{
        File file = convertMultiPartToFile(multipartFile);
        String fileName = multipartFile.getOriginalFilename();
        this.leaseContainer.start();
        RequestedSecret secret = this.leaseContainer.requestRenewableSecret("aws/sts/demo_role");
        log.info(String.valueOf(secret));
        return this.amazonS3.putObject(BUCKET_NAME, fileName, file);
    }
}
