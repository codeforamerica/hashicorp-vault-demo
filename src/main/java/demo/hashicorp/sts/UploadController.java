package demo.hashicorp.sts;

import com.amazonaws.services.s3.AmazonS3;
import demo.hashicorp.sts.config.AwsConfigurationProperties;
import demo.hashicorp.sts.config.VaultAwsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.vault.repository.mapping.Secret;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;

@RestController
@Slf4j
public class UploadController {

    @Autowired
    private AmazonS3 amazonS3;

    private final String BUCKET_NAME = "la-vault-test-bucket";

    @Autowired
    private SecretLeaseContainer leaseContainer;

    @Autowired
    private VaultAwsConfiguration vaultAwsConfiguration;

    @Autowired
    AwsConfigurationProperties properties;

    private RequestedSecret secret;

    public UploadController(SecretLeaseContainer leaseContainer){
        this.leaseContainer = leaseContainer;
        leaseContainer.start();
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }


    @PostMapping("/upload-file")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView uploadFile(@RequestPart(value="file") MultipartFile multipartFile, Model model) throws IOException {
        File file = convertMultiPartToFile(multipartFile);
        String fileName = multipartFile.getOriginalFilename();




        RequestedSecret secret = this.leaseContainer.requestRenewableSecret("aws/sts/demo_role");

        this.amazonS3.putObject(BUCKET_NAME, fileName, file);
        List<Lease> leases = this.vaultAwsConfiguration.leases;
        Lease currentLease = leases.get(leases.size() - 1);
        currentLease.setUploadRequest("upload " + fileName + " to S3");
        this.vaultAwsConfiguration.leases.set(leases.size() - 1, currentLease);
        model.addAttribute("leases", this.vaultAwsConfiguration.leases);
        return new ModelAndView("upload");
    }
}
