#this - It's just the Terraform local name inside the module.
#It is small module represents the main resource it manages

resource "aws_ecr_repository" "this" {
  name = var.repository_name
}