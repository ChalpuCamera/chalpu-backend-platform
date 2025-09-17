terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Get current AWS account ID
data "aws_caller_identity" "current" {}

# VPC Module
module "vpc" {
  source = "../../modules/network/vpc"
  
  vpc_id = var.vpc_id
}

# IAM Module
module "iam" {
  source = "../../modules/iam"
  
  environment    = var.environment
  aws_account_id = data.aws_caller_identity.current.account_id
  aws_region     = var.aws_region
  github_repo    = var.github_repo
}

# Parameter Store Module
module "parameter_store" {
  source = "../../modules/secret/parameter-store"

  environment                 = var.environment
  db_host                    = var.db_host
  db_port                    = var.db_port
  db_name                    = var.db_name
  db_username                = var.db_username
  db_password                = var.db_password
  jwt_secret                 = var.jwt_secret
  aws_access_key             = var.aws_access_key
  aws_secret_key             = var.aws_secret_key
  kakao_client_id           = var.kakao_client_id
  kakao_client_secret       = var.kakao_client_secret
  oauth2_redirect_success_url = var.oauth2_redirect_success_url
  oauth2_redirect_failure_url = var.oauth2_redirect_failure_url
  s3_bucket                  = var.s3_bucket
  cloudfront_domain          = var.cloudfront_domain
  sentry_dsn                 = var.sentry_dsn
  gemini_api_key             = var.gemini_api_key
  gemini_model               = var.gemini_model
}

# EC2 Module
module "ec2" {
  source = "../../modules/compute/ec2"

  environment                = var.environment
  instance_type             = var.instance_type
  ami_id                    = var.ami_id
  key_name                  = var.key_name
  vpc_id                    = module.vpc.vpc_id
  subnet_id                 = "subnet-057b5df3446b515ca"  # ap-northeast-2a public subnet
  iam_role_name             = module.iam.ec2_role_name
  enable_monitoring         = var.enable_monitoring
  enable_deletion_protection = var.enable_deletion_protection
  tags                      = var.tags
}