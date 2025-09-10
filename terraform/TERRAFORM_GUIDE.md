# Terraform 코드 구조 및 문법 가이드

## 📁 전체 구조 설명

### 1. 모듈 기반 아키텍처
```
terraform/
├── environments/        # 환경별 루트 모듈
│   ├── dev/            # 개발 환경 설정
│   │   ├── main.tf     # 모듈 호출 및 조합
│   │   ├── variables.tf # 변수 정의
│   │   ├── terraform.tfvars # 실제 값 (git ignore 필요)
│   │   ├── backend.tf  # State 저장소 설정
│   │   └── outputs.tf  # 출력 값
│   └── prod/           # 프로덕션 환경 (동일 구조)
└── modules/            # 재사용 가능한 모듈
    ├── compute/ec2/    # EC2 관련 리소스
    ├── iam/            # IAM 역할 및 정책
    ├── network/vpc/    # 네트워크 설정
    └── secret/parameter-store/ # 파라미터 스토어
```

## 🔧 주요 Terraform 문법 설명

### 1. Provider 설정 (main.tf)
```hcl
terraform {
  required_version = ">= 1.0"  # Terraform 최소 버전
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"  # AWS Provider
      version = "~> 5.0"          # 5.x 버전 사용
    }
  }
}

provider "aws" {
  region = var.aws_region  # 변수로 리전 지정
}
```

### 2. 모듈 호출 (environments/dev/main.tf)
```hcl
module "ec2" {
  source = "../../modules/compute/ec2"  # 모듈 경로
  
  # 모듈에 전달할 변수들
  environment   = var.environment
  instance_type = var.instance_type
  vpc_id        = module.vpc.vpc_id  # 다른 모듈의 출력 사용
}
```

### 3. 변수 정의 (variables.tf)
```hcl
variable "environment" {
  description = "Environment name"  # 설명
  type        = string            # 타입 (string, number, bool, list, map)
  default     = "dev"             # 기본값 (선택사항)
}

variable "db_password" {
  type      = string
  sensitive = true  # 민감한 정보 (로그에 숨김)
}
```

### 4. 리소스 정의 (modules/compute/ec2/main.tf)
```hcl
resource "aws_instance" "app_server" {
  ami           = data.aws_ami.ubuntu.id  # Data source 참조
  instance_type = var.instance_type       # 변수 참조
  
  tags = {
    Name = "chalpu-${var.environment}-server"  # 문자열 보간
  }
  
  lifecycle {
    create_before_destroy = true  # 리소스 교체 시 새것 먼저 생성
  }
}
```

### 5. Data Sources (기존 리소스 참조)
```hcl
# 최신 Ubuntu AMI 찾기
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]  # Canonical 계정
  
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-*"]
  }
}

# 기존 VPC 참조
data "aws_vpc" "existing" {
  id = var.vpc_id
}
```

### 6. 조건문 사용
```hcl
resource "aws_security_group_rule" "ssh" {
  # 삼항 연산자 사용
  cidr_blocks = var.environment == "dev" ? ["0.0.0.0/0"] : ["10.0.0.0/8"]
  
  # count를 이용한 조건부 생성
  count = var.enable_ssh ? 1 : 0
}
```

### 7. 반복문 (for_each, count)
```hcl
# count 사용
resource "aws_instance" "workers" {
  count = 3
  
  tags = {
    Name = "worker-${count.index}"
  }
}

# for_each 사용
resource "aws_ssm_parameter" "params" {
  for_each = {
    db_url  = var.db_url
    db_user = var.db_username
  }
  
  name  = "/app/${each.key}"
  value = each.value
}
```

### 8. 출력 값 (outputs.tf)
```hcl
output "ec2_public_ip" {
  description = "Public IP of EC2 instance"
  value       = aws_instance.app_server.public_ip
  sensitive   = false
}
```

### 9. Backend 설정 (backend.tf)
```hcl
terraform {
  backend "s3" {
    bucket         = "terraform-state-bucket"
    key            = "dev/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "terraform-locks"  # State locking
  }
}
```

## 🏗️ 우리 프로젝트의 구조 특징

### 1. 환경 분리
- `environments/dev/` - 개발 환경
- `environments/prod/` - 프로덕션 환경 (추후 확장)
- 각 환경은 독립적인 state 파일 관리

### 2. 모듈화
```hcl
# EC2 모듈
module "ec2" {
  source = "../../modules/compute/ec2"
  # EC2 인스턴스, 보안 그룹, Elastic IP 포함
}

# IAM 모듈
module "iam" {
  source = "../../modules/iam"
  # EC2 역할, GitHub Actions 역할, 정책 포함
}

# Parameter Store 모듈
module "parameter_store" {
  source = "../../modules/secret/parameter-store"
  # 모든 환경 변수를 암호화하여 저장
}

# VPC 모듈
module "vpc" {
  source = "../../modules/network/vpc"
  # 기존 VPC 참조 (신규 생성 X)
}
```

### 3. 변수 우선순위
1. 명령줄 옵션 (`-var`)
2. `.tfvars` 파일
3. 환경 변수 (`TF_VAR_`)
4. `variables.tf`의 default 값

### 4. 의존성 관리
```hcl
# 명시적 의존성
depends_on = [aws_iam_role_policy_attachment.ec2_policy]

# 암시적 의존성 (자동)
subnet_id = module.vpc.public_subnet_ids[0]  # vpc 모듈 완료 후 실행
```

## 📝 Terraform 명령어

### 기본 워크플로우
```bash
# 1. 초기화 (Provider 다운로드, Backend 설정)
terraform init

# 2. 포맷팅
terraform fmt -recursive

# 3. 유효성 검사
terraform validate

# 4. 실행 계획 확인
terraform plan

# 5. 인프라 생성/수정
terraform apply

# 6. 인프라 삭제
terraform destroy
```

### 유용한 명령어
```bash
# 현재 상태 확인
terraform show

# 특정 리소스만 적용
terraform apply -target=module.ec2

# 리소스 목록 확인
terraform state list

# 리소스 상세 정보
terraform state show module.ec2.aws_instance.app_server

# 출력 값 확인
terraform output
terraform output ec2_public_ip

# 그래프 생성 (시각화)
terraform graph | dot -Tpng > graph.png
```

## 🔐 보안 Best Practices

### 1. 민감한 정보 관리
```hcl
# variables.tf에서 sensitive 플래그 사용
variable "db_password" {
  type      = string
  sensitive = true  # 로그에 표시 안 됨
}

# Parameter Store 사용 (암호화)
resource "aws_ssm_parameter" "secret" {
  type  = "SecureString"  # KMS로 암호화
  value = var.secret_value
}
```

### 2. .gitignore 설정
```
*.tfvars        # 실제 값 파일
*.tfstate       # State 파일
*.tfstate.*     # State 백업
.terraform/     # Provider 파일
```

### 3. IAM 최소 권한
```hcl
# 필요한 권한만 부여
resource "aws_iam_policy" "minimal" {
  policy = jsonencode({
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:GetObject"]  # 필요한 액션만
      Resource = ["arn:aws:s3:::bucket/*"]  # 특정 리소스만
    }]
  })
}
```

## 🎯 우리 프로젝트 특징

### 1. 기존 VPC 재사용
```hcl
# 새로 생성하지 않고 기존 VPC 참조
data "aws_vpc" "existing" {
  id = "vpc-02eecf0b187d2702a"
}
```

### 2. 환경별 확장성
```hcl
# 환경에 따른 인스턴스 타입 변경
instance_type = var.environment == "prod" ? "t3.small" : "t2.micro"
```

### 3. GitHub Actions 연동
```hcl
# OIDC를 통한 안전한 인증
resource "aws_iam_role" "github_actions" {
  assume_role_policy = jsonencode({
    Statement = [{
      Effect = "Allow"
      Principal = {
        Federated = "arn:aws:iam::${account_id}:oidc-provider/token.actions.githubusercontent.com"
      }
      Action = "sts:AssumeRoleWithWebIdentity"
    }]
  })
}
```

### 4. User Data 스크립트
```hcl
# EC2 초기 설정 자동화
user_data = <<-EOF
  #!/bin/bash
  apt-get update
  apt-get install -y openjdk-17-jdk
  mkdir -p /home/ubuntu/app
EOF
```

## 📚 참고 자료

- [Terraform AWS Provider 문서](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Terraform 언어 문서](https://www.terraform.io/language)
- [AWS Best Practices](https://www.terraform.io/docs/cloud/guides/recommended-practices/index.html)