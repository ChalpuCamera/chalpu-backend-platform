# Policy for EC2 to access Parameter Store
resource "aws_iam_policy" "ec2_parameter_store_policy" {
  name        = "chalpu-${var.environment}-ec2-parameter-store-policy"
  description = "Allow EC2 to read from Parameter Store"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters",
          "ssm:GetParametersByPath"
        ]
        Resource = [
          "arn:aws:ssm:${var.aws_region}:${var.aws_account_id}:parameter/chalpu-platform/${var.environment}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "kms:ViaService" = "ssm.${var.aws_region}.amazonaws.com"
          }
        }
      }
    ]
  })
}

# Policy for EC2 to access S3 (for application files)
resource "aws_iam_policy" "ec2_s3_policy" {
  name        = "chalpu-${var.environment}-ec2-s3-policy"
  description = "Allow EC2 to access S3 buckets"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ]
        Resource = [
          "arn:aws:s3:::chalpu-photo-bucket/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::chalpu-photo-bucket"
        ]
      }
    ]
  })
}

# Policy for GitHub Actions
resource "aws_iam_policy" "github_actions_policy" {
  name        = "chalpu-${var.environment}-github-actions-policy"
  description = "Policy for GitHub Actions to deploy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters",
          "ssm:GetParametersByPath"
        ]
        Resource = [
          "arn:aws:ssm:${var.aws_region}:${var.aws_account_id}:parameter/chalpu-platform/${var.environment}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "ec2:DescribeInstances",
          "ec2:DescribeInstanceStatus"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "kms:ViaService" = "ssm.${var.aws_region}.amazonaws.com"
          }
        }
      }
    ]
  })
}

# Attach policies to EC2 role
resource "aws_iam_role_policy_attachment" "ec2_parameter_store" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.ec2_parameter_store_policy.arn
}

resource "aws_iam_role_policy_attachment" "ec2_s3" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.ec2_s3_policy.arn
}

# Attach policy to GitHub Actions role
resource "aws_iam_role_policy_attachment" "github_actions" {
  role       = aws_iam_role.github_actions_role.name
  policy_arn = aws_iam_policy.github_actions_policy.arn
}