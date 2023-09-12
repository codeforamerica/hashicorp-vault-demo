source .env
export VAULT_ADDR='http://127.0.0.1:8200'
vault secrets enable aws
vault write aws/config/root \
  access_key=${AWS_ACCESS_KEY} \
  secret_key=${AWS_SECRET_KEY} \
  region=${AWS_REGION}
vault write aws/roles/demo_role \
    credential_type=iam_user \
    policy_document=-<<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:*",
      "Resource": "*"
    }
  ]
}
EOF
vault write aws/sts/demo_role ttl=60m