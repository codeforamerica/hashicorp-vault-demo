package demo.hashicorp.sts.config;

import demo.hashicorp.sts.Lease;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@ConditionalOnProperty(name="spring.cloud.vault.aws.enabled")
@ConditionalOnBean(SecretLeaseContainer.class)
public class VaultAwsConfiguration implements ApplicationContextAware {


    private static final String STS_PATH = "/sts/";

    ApplicationContext context;

    @Autowired
    SecretLeaseContainer container;

    @Autowired
    AwsConfigurationProperties properties;

    @Autowired ConfigurableEnvironment configurableEnvironment;

    @Autowired ConfigurationPropertiesRebinder rebinder;

    public List<Lease> leases = new ArrayList<>();

    @Value("${spring.cloud.vault.aws.backend}")
    String vaultAwsBackend;

    @Value("${spring.cloud.vault.aws.role}")
    String vaultAwsRole;

    @PostConstruct
    private void postConstruct() {
        log.info("Register lease listener");

        container.addLeaseListener(leaseEvent -> {

            if (leaseEvent.getSource().getPath().contains(vaultAwsBackend + STS_PATH + vaultAwsRole)
            && leaseEvent instanceof SecretLeaseCreatedEvent){

                // rebind aws configuration for this app
                rebind("awsConfigurationProperties");

                // refresh additional bean dependencies

                refresh("AWSConfiguration");
                refresh("basicAWSCredentials");
                refresh("amazonS3Client");

                String retrievedSecret = (((SecretLeaseCreatedEvent) leaseEvent).getSecrets().toString());
                Map<String, String> secretMap = Arrays.stream(
                        retrievedSecret.substring( 1, retrievedSecret.length() - 1 )
                                .replace(" ", "")
                                .split(","))
                        .map(s -> s.split("="))
                        .collect(Collectors.toMap(s -> s[0], s -> s[1]));
                Date date = new Date();
                Timestamp currentTimestamp = new Timestamp(date.getTime());
                String ttl = secretMap.get("ttl");
                Timestamp expiryTime = new Timestamp(currentTimestamp.getTime() + (Integer.parseInt(ttl) * 1000L));
                Lease lease = new Lease(currentTimestamp.toString(), vaultAwsRole, expiryTime.toString(), "", "success");
                leases.add(lease);
                properties.setSessionToken(secretMap.get("security_token"));
                properties.setAccesskey(secretMap.get("access_key"));
                properties.setSecretKey(secretMap.get("secret_key"));
                log.info("SecretLeaseCreatedEvent received and applied for: "+leaseEvent.getSource().getPath());
            }
        });
    }

    private void rebind(String bean){
        try {
            boolean success = this.rebinder.rebind(bean);
            if (log.isInfoEnabled()) {
                log.info(String.format(
                        "Attempted to rebind bean '%s' with updated AWS secrets from vault, success: %s",
                        bean, success));
            }
        } catch (Exception ex) {
            log.error("Exception rebinding "+bean,ex);
        }
    }

    private void refresh(String bean) {
        try {
            RefreshScope refreshScope = this.context.getBean(RefreshScope.class);
            boolean success = refreshScope.refresh(bean);
            if (log.isInfoEnabled()) {
                log.info(String.format(
                        "Attempted to refresh bean '%s' with updated AWS secrets from vault, success: %s",
                        bean, success));
            }
        } catch (Exception ex) {
            log.error("Exception rebinding "+bean,ex);
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
