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

# OAuth Google Configuration
resource "aws_ssm_parameter" "google_client_id" {
  name        = "/chalpu-platform/${var.environment}/google-client-id"
  description = "Google OAuth client ID"
  type        = "String"
  value       = var.google_client_id

  tags = {
    Name        = "chalpu-${var.environment}-google-client-id"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "google_client_secret" {
  name        = "/chalpu-platform/${var.environment}/google-client-secret"
  description = "Google OAuth client secret"
  type        = "SecureString"
  value       = var.google_client_secret

  tags = {
    Name        = "chalpu-${var.environment}-google-client-secret"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "google_mobile_client_ids" {
  name        = "/chalpu-platform/${var.environment}/google-mobile-client-ids"
  description = "Google mobile client IDs (Android, iOS)"
  type        = "String"
  value       = var.google_mobile_client_ids

  tags = {
    Name        = "chalpu-${var.environment}-google-mobile-client-ids"
    Environment = var.environment
  }
}

# OAuth2 Redirect Configuration
resource "aws_ssm_parameter" "oauth2_redirect_success_path" {
  name        = "/chalpu-platform/${var.environment}/oauth2-redirect-success-path"
  description = "OAuth2 redirect success path"
  type        = "String"
  value       = var.oauth2_redirect_success_path

  tags = {
    Name        = "chalpu-${var.environment}-oauth2-redirect-success-path"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "oauth2_redirect_failure_path" {
  name        = "/chalpu-platform/${var.environment}/oauth2-redirect-failure-path"
  description = "OAuth2 redirect failure path"
  type        = "String"
  value       = var.oauth2_redirect_failure_path

  tags = {
    Name        = "chalpu-${var.environment}-oauth2-redirect-failure-path"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "oauth2_owner_domain" {
  name        = "/chalpu-platform/${var.environment}/oauth2-owner-domain"
  description = "OAuth2 owner domain"
  type        = "String"
  value       = var.oauth2_owner_domain

  tags = {
    Name        = "chalpu-${var.environment}-oauth2-owner-domain"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "oauth2_customer_domain" {
  name        = "/chalpu-platform/${var.environment}/oauth2-customer-domain"
  description = "OAuth2 customer domain"
  type        = "String"
  value       = var.oauth2_customer_domain

  tags = {
    Name        = "chalpu-${var.environment}-oauth2-customer-domain"
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

# Sentry Configuration
resource "aws_ssm_parameter" "sentry_dsn" {
  name        = "/chalpu-platform/${var.environment}/sentry-dsn"
  description = "Sentry DSN for error tracking"
  type        = "SecureString"
  value       = var.sentry_dsn

  tags = {
    Name        = "chalpu-${var.environment}-sentry-dsn"
    Environment = var.environment
  }
}

# Gemini API Configuration
resource "aws_ssm_parameter" "gemini_api_key" {
  name        = "/chalpu-platform/${var.environment}/gemini-api-key"
  description = "Google Gemini API key"
  type        = "SecureString"
  value       = var.gemini_api_key

  tags = {
    Name        = "chalpu-${var.environment}-gemini-api-key"
    Environment = var.environment
  }
}

resource "aws_ssm_parameter" "gemini_model" {
  name        = "/chalpu-platform/${var.environment}/gemini-model"
  description = "Google Gemini model name"
  type        = "String"
  value       = var.gemini_model

  tags = {
    Name        = "chalpu-${var.environment}-gemini-model"
    Environment = var.environment
  }
}