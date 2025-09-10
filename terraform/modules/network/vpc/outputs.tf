output "vpc_id" {
  description = "ID of the VPC"
  value       = data.aws_vpc.existing.id
}

output "vpc_cidr" {
  description = "CIDR block of the VPC"
  value       = data.aws_vpc.existing.cidr_block
}

output "public_subnet_ids" {
  description = "List of public subnet IDs"
  value       = data.aws_subnets.public.ids
}

output "private_subnet_ids" {
  description = "List of private subnet IDs"
  value       = data.aws_subnets.private.ids
}

output "all_subnet_ids" {
  description = "List of all subnet IDs"
  value       = data.aws_subnets.available.ids
}