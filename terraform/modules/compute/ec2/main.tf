# Get latest Ubuntu AMI
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2 Instance
resource "aws_instance" "app_server" {
  ami                     = var.ami_id != "" ? var.ami_id : data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [aws_security_group.app_server.id]

  iam_instance_profile = aws_iam_instance_profile.ec2_profile.name
  
  monitoring                  = var.enable_monitoring
  disable_api_termination    = var.enable_deletion_protection
  associate_public_ip_address = true

  root_block_device {
    volume_type = "gp3"
    volume_size = 20
    encrypted   = true
    
    tags = merge(
      var.tags,
      {
        Name        = "chalpu-${var.environment}-root-volume"
        Environment = var.environment
      }
    )
  }

  user_data = <<-EOF
    #!/bin/bash
    # Update system
    apt-get update
    apt-get upgrade -y
    
    # Install Java 17
    apt-get install -y openjdk-17-jdk
    
    # Install AWS CLI
    apt-get install -y awscli
    
    # Create app directory
    mkdir -p /home/ubuntu/app
    chown ubuntu:ubuntu /home/ubuntu/app
    
    # Install CloudWatch agent
    wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
    dpkg -i amazon-cloudwatch-agent.deb
    
    # Configure timezone
    timedatectl set-timezone Asia/Seoul
  EOF

  tags = merge(
    var.tags,
    {
      Name        = "chalpu-${var.environment}-app-server"
      Environment = var.environment
      Type        = "application"
    }
  )

  lifecycle {
    create_before_destroy = true
  }
}

# Elastic IP
resource "aws_eip" "app_server" {
  instance = aws_instance.app_server.id
  domain   = "vpc"

  tags = merge(
    var.tags,
    {
      Name        = "chalpu-${var.environment}-eip"
      Environment = var.environment
    }
  )
}

# IAM Instance Profile
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "chalpu-${var.environment}-ec2-profile"
  role = var.iam_role_name

  tags = merge(
    var.tags,
    {
      Name        = "chalpu-${var.environment}-ec2-profile"
      Environment = var.environment
    }
  )
}

# Security Group
resource "aws_security_group" "app_server" {
  name_prefix = "chalpu-${var.environment}-app-"
  description = "Security group for Chalpu ${var.environment} application server"
  vpc_id      = var.vpc_id

  # SSH access (restrict in production)
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.environment == "dev" ? ["0.0.0.0/0"] : ["10.0.0.0/8"]
    description = "SSH access"
  }

  # Application port
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Application port"
  }

  # HTTPS
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS"
  }

  # HTTP (for redirect to HTTPS)
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP"
  }

  # Outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = merge(
    var.tags,
    {
      Name        = "chalpu-${var.environment}-app-sg"
      Environment = var.environment
    }
  )

  lifecycle {
    create_before_destroy = true
  }
}