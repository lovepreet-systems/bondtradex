variable "cluster_name" {
  type        = string
  description = "Name of the EKS cluster"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnet IDs used by the EKS cluster"
}

variable "environment" {
  type        = string
  description = "Deployment environment"
}