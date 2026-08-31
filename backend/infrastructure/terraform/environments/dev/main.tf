locals {
  services = toset([
    "ioi-service",
    "auth-service",
    "api-gateway"
  ])
}

module "ecr" {
  source = "../../modules/ecr"

  for_each = local.services

  repository_name = "bondtradex-${var.environment}-${each.key}"
}

module "network" {
  source = "../../modules/network"

  vpc_cidr           = var.vpc_cidr
  environment        = var.environment
  enable_nat_gateway = true
}

module "eks" {
  source = "../../modules/eks"

  cluster_name = "bondtradex-${var.environment}"
  environment  = var.environment
  subnet_ids   = module.network.private_app_subnet_ids
}