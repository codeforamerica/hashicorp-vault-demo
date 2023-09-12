package demo.hashicorp.sts;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import demo.hashicorp.sts.config.AwsConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AmazonS3 amazonS3;

    private AwsConfigurationProperties properties;

    private final String BUCKET_NAME = "la-vault-test-bucket";

    @Autowired
    UploadController(AmazonS3 amazonS3, AwsConfigurationProperties properties){
        this.amazonS3 = amazonS3;
        this.properties = properties;
    }

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
        log.info("FILENAME: " + fileName);
        log.info(properties.toString());
        return this.amazonS3.putObject(BUCKET_NAME, fileName, file);
    }
}
