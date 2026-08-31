#this file is to declare the variables
variable "aws_region" {
  type        = string
  description = "aws region name"
}

variable "environment" {
  type        = string
  description = "description of the environment"
}

variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the DEV VPC"
}

variable "enable_nat_gateway" {
  type        = bool
  description = "Whether NAT Gateway is enabled"
  default     = false
}