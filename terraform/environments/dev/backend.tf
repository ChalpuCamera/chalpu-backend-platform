terraform {
  backend "s3" {
    bucket  = "chalpu-terraform-state"
    key     = "dev/terraform.tfstate"
    region  = "ap-northeast-2"
    encrypt = true
  }
}