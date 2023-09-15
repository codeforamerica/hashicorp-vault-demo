package demo.hashicorp.sts.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.lease.SecretLeaseContainer;

@Configuration
public class LeaseConfiguration {


    @Autowired
    private VaultOperations vaultOperations;

    @Bean
    SecretLeaseContainer leaseContainer() {
        return new SecretLeaseContainer(vaultOperations, new ThreadPoolTaskScheduler());
    }
}
