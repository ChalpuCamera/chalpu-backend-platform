# Reference existing VPC
data "aws_vpc" "existing" {
  id = var.vpc_id
}

# Get available subnets in the VPC
data "aws_subnets" "available" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.existing.id]
  }
}

# Get details of each subnet
data "aws_subnet" "selected" {
  for_each = toset(data.aws_subnets.available.ids)
  id       = each.value
}

# Get public subnets (those with route to IGW)
data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.existing.id]
  }

  filter {
    name   = "map-public-ip-on-launch"
    values = ["true"]
  }
}

# Get private subnets
data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.existing.id]
  }

  filter {
    name   = "map-public-ip-on-launch"
    values = ["false"]
  }
}