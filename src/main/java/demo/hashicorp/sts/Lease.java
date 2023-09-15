package demo.hashicorp.sts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Lease {

    private String leaseRequestedTime;

    private String awsRole;

    private String leaseExpiryTime;

    private String uploadRequest;

    private String uploadRequestStatus;

}
