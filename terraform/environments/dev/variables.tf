variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "vpc_id" {
  description = "ID of the existing VPC"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t2.micro"
}

variable "key_name" {
  description = "EC2 Key Pair name"
  type        = string
}

variable "enable_monitoring" {
  description = "Enable detailed monitoring"
  type        = bool
  default     = false
}

variable "enable_deletion_protection" {
  description = "Enable termination protection"
  type        = bool
  default     = false
}

variable "github_repo" {
  description = "GitHub repository in format 'owner/repo'"
  type        = string
}

# Database variables
variable "db_host" {
  description = "Database host"
  type        = string
  sensitive   = true
}

variable "db_port" {
  description = "Database port"
  type        = string
  default     = "3306"
}

variable "db_name" {
  description = "Database name"
  type        = string
}

variable "db_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

# Application secrets
variable "jwt_secret" {
  description = "JWT signing secret"
  type        = string
  sensitive   = true
}

variable "aws_access_key" {
  description = "AWS access key for S3"
  type        = string
  sensitive   = true
}

variable "aws_secret_key" {
  description = "AWS secret key for S3"
  type        = string
  sensitive   = true
}

variable "kakao_client_id" {
  description = "Kakao OAuth client ID"
  type        = string
}

variable "kakao_client_secret" {
  description = "Kakao OAuth client secret"
  type        = string
  sensitive   = true
}

variable "oauth2_redirect_success_url" {
  description = "OAuth2 redirect success URL"
  type        = string
  default     = "https://chalpu.com/login/success"
}

variable "oauth2_redirect_failure_url" {
  description = "OAuth2 redirect failure URL"
  type        = string
  default     = "https://chalpu.com/login/failure"
}

# S3 and CloudFront
variable "s3_bucket" {
  description = "S3 bucket name for photos"
  type        = string
  default     = "chalpu-photo-bucket"
}

variable "cloudfront_domain" {
  description = "CloudFront distribution domain"
  type        = string
  default     = "https://cdn.chalpu.com"
}

variable "sentry_dsn" {
  description = "Sentry DSN for error tracking"
  type        = string
  sensitive   = true
}

variable "gemini_api_key" {
  description = "Google Gemini API key"
  type        = string
  sensitive   = true
}

variable "gemini_model" {
  description = "Google Gemini model name"
  type        = string
  default     = "gemini-2.0-flash-latest"
}

# Tags
variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}