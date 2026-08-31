variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the VPC"
}

variable "environment" {
  type        = string
  description = "Deployment environment"
}

variable "enable_nat_gateway" {
  type        = bool
  description = "Whether to create a NAT Gateway for private application subnets"
  default     = false
}