variable "environment" {
  description = "Environment name (dev, prod)"
  type        = string
}

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

variable "oauth2_redirect_success_path" {
  description = "OAuth2 redirect success path"
  type        = string
  default     = "/oauth2/success"
}

variable "oauth2_redirect_failure_path" {
  description = "OAuth2 redirect failure path"
  type        = string
  default     = "/oauth2/failure"
}

variable "oauth2_owner_domain" {
  description = "OAuth2 owner domain"
  type        = string
  default     = "owner.chalpu.com"
}

variable "oauth2_customer_domain" {
  description = "OAuth2 customer domain"
  type        = string
  default     = "customer.chalpu.com"
}

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