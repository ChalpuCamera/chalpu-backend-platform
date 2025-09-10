variable "environment" {
  description = "Environment name (dev, prod)"
  type        = string
}

variable "aws_account_id" {
  description = "AWS Account ID"
  type        = string
}

variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "ap-northeast-2"
}

variable "github_repo" {
  description = "GitHub repository in format 'owner/repo'"
  type        = string
}