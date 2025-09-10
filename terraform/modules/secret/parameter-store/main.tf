# Database parameters
resource "aws_ssm_parameter" "db_host" {
  name        = "/chalpu-platform/${var.environment}/db-host"
  description = "Database host"
  type        = "SecureString"
  value       = var.db_host

  tags = {
    Name        = "chalpu-${var.environment}-db-host"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "db_port" {
  name        = "/chalpu-platform/${var.environment}/db-port"
  description = "Database port"
  type        = "String"
  value       = var.db_port

  tags = {
    Name        = "chalpu-${var.environment}-db-port"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "db_name" {
  name        = "/chalpu-platform/${var.environment}/db-name"
  description = "Database name"
  type        = "String"
  value       = var.db_name

  tags = {
    Name        = "chalpu-${var.environment}-db-name"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "db_username" {
  name        = "/chalpu-platform/${var.environment}/db-username"
  description = "Database username"
  type        = "SecureString"
  value       = var.db_username

  tags = {
    Name        = "chalpu-${var.environment}-db-username"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "db_password" {
  name        = "/chalpu-platform/${var.environment}/db-password"
  description = "Database password"
  type        = "SecureString"
  value       = var.db_password

  tags = {
    Name        = "chalpu-${var.environment}-db-password"
    Environment = var.environment
  }
}

# JWT Secret
resource "aws_ssm_parameter" "jwt_secret" {
  name        = "/chalpu-platform/${var.environment}/jwt-secret"
  description = "JWT signing secret"
  type        = "SecureString"
  value       = var.jwt_secret

  tags = {
    Name        = "chalpu-${var.environment}-jwt-secret"
    Environment = var.environment
  }
}

# AWS Credentials for S3
resource "aws_ssm_parameter" "aws_access_key" {
  name        = "/chalpu-platform/${var.environment}/aws-access-key"
  description = "AWS access key for S3"
  type        = "SecureString"
  value       = var.aws_access_key

  tags = {
    Name        = "chalpu-${var.environment}-aws-access-key"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "aws_secret_key" {
  name        = "/chalpu-platform/${var.environment}/aws-secret-key"
  description = "AWS secret key for S3"
  type        = "SecureString"
  value       = var.aws_secret_key

  tags = {
    Name        = "chalpu-${var.environment}-aws-secret-key"
    Environment = var.environment
  }
}

# OAuth Kakao Configuration
resource "aws_ssm_parameter" "kakao_client_id" {
  name        = "/chalpu-platform/${var.environment}/kakao-client-id"
  description = "Kakao OAuth client ID"
  type        = "String"
  value       = var.kakao_client_id

  tags = {
    Name        = "chalpu-${var.environment}-kakao-client-id"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "kakao_client_secret" {
  name        = "/chalpu-platform/${var.environment}/kakao-client-secret"
  description = "Kakao OAuth client secret"
  type        = "SecureString"
  value       = var.kakao_client_secret

  tags = {
    Name        = "chalpu-${var.environment}-kakao-client-secret"
    Environment = var.environment
  }
}

# OAuth2 Redirect URLs
resource "aws_ssm_parameter" "oauth2_redirect_success_url" {
  name        = "/chalpu-platform/${var.environment}/oauth2-redirect-success-url"
  description = "OAuth2 redirect success URL"
  type        = "String"
  value       = var.oauth2_redirect_success_url

  tags = {
    Name        = "chalpu-${var.environment}-oauth2-redirect-success-url"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "oauth2_redirect_failure_url" {
  name        = "/chalpu-platform/${var.environment}/oauth2-redirect-failure-url"
  description = "OAuth2 redirect failure URL"
  type        = "String"
  value       = var.oauth2_redirect_failure_url

  tags = {
    Name        = "chalpu-${var.environment}-oauth2-redirect-failure-url"
    Environment = var.environment
  }
}

# S3 Bucket Name
resource "aws_ssm_parameter" "s3_bucket" {
  name        = "/chalpu-platform/${var.environment}/s3-bucket"
  description = "S3 bucket name for photos"
  type        = "String"
  value       = var.s3_bucket

  tags = {
    Name        = "chalpu-${var.environment}-s3-bucket"
    Environment = var.environment
  }
}

# CloudFront Domain
resource "aws_ssm_parameter" "cloudfront_domain" {
  name        = "/chalpu-platform/${var.environment}/cloudfront-domain"
  description = "CloudFront distribution domain"
  type        = "String"
  value       = var.cloudfront_domain

  tags = {
    Name        = "chalpu-${var.environment}-cloudfront-domain"
    Environment = var.environment
  }
}