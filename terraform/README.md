# Terraform Infrastructure for Chalpu Platform

## 디렉토리 구조
```
terraform/
├── environments/     # 환경별 설정
│   ├── dev/         # 개발 환경
│   └── prod/        # 프로덕션 환경 (추후)
├── modules/         # 재사용 가능한 모듈
│   ├── compute/ec2/ # EC2 인스턴스
│   ├── iam/         # IAM 역할 및 정책
│   ├── network/vpc/ # VPC 설정
│   └── secret/      # Parameter Store
└── global/          # 공통 변수
```

## 사전 요구사항

1. **AWS CLI 설치 및 설정**
   ```bash
   aws configure
   ```

2. **Terraform 설치** (v1.0 이상)
   ```bash
   brew install terraform
   ```

3. **S3 버킷 생성** (Terraform state 저장용)
   ```bash
   aws s3 mb s3://chalpu-terraform-state --region ap-northeast-2
   ```

4. **DynamoDB 테이블 생성** (State lock용)
   ```bash
   aws dynamodb create-table \
     --table-name chalpu-terraform-locks \
     --attribute-definitions AttributeName=LockID,AttributeType=S \
     --key-schema AttributeName=LockID,KeyType=HASH \
     --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
     --region ap-northeast-2
   ```

5. **EC2 Key Pair 생성**
   ```bash
   aws ec2 create-key-pair \
     --key-name chalpu-dev-key \
     --query 'KeyMaterial' \
     --output text > ~/.ssh/chalpu-dev-key.pem
   
   chmod 400 ~/.ssh/chalpu-dev-key.pem
   ```

## 배포 방법

### 1. terraform.tfvars 파일 수정
```bash
cd terraform/environments/dev
cp terraform.tfvars.example terraform.tfvars
# 실제 값으로 수정
```

### 2. Terraform 초기화
```bash
terraform init
```

### 3. 계획 확인
```bash
terraform plan
```

### 4. 인프라 생성
```bash
terraform apply
```

### 5. 출력 확인
```bash
terraform output
```

## GitHub Actions 설정

### 필요한 Secrets 설정
GitHub 저장소 Settings > Secrets and variables > Actions에서 설정:

- `AWS_ROLE_ARN`: GitHub Actions용 IAM Role ARN (Terraform output 참조)
- `EC2_SSH_KEY`: EC2 접속용 SSH Private Key 내용

### Parameter Store 값 설정
AWS Systems Manager > Parameter Store에서 다음 값들을 설정:

```
/chalpu-platform/dev/db-url
/chalpu-platform/dev/db-username
/chalpu-platform/dev/db-password
/chalpu-platform/dev/jwt-secret
/chalpu-platform/dev/aws-access-key
/chalpu-platform/dev/aws-secret-key
/chalpu-platform/dev/oauth-kakao-secret
/chalpu-platform/dev/s3-bucket
/chalpu-platform/dev/cloudfront-domain
```

## EC2 인스턴스 접속
```bash
ssh -i ~/.ssh/chalpu-dev-key.pem ubuntu@<PUBLIC_IP>
```

## 인프라 삭제
```bash
terraform destroy
```

## 주의사항

1. **terraform.tfvars 파일은 절대 커밋하지 마세요** (민감한 정보 포함)
2. **프로덕션 환경 배포 시 별도의 VPC 사용 권장**
3. **IAM 권한은 최소 권한 원칙 적용**
4. **정기적으로 AWS 비용 모니터링**

## 문제 해결

### Terraform state lock 문제
```bash
terraform force-unlock <LOCK_ID>
```

### EC2 인스턴스 연결 불가
1. 보안 그룹 확인 (포트 22, 8080)
2. 인스턴스 상태 확인
3. SSH 키 권한 확인 (400)

### Parameter Store 접근 오류
1. IAM 역할 정책 확인
2. KMS 키 권한 확인
3. Parameter 경로 확인