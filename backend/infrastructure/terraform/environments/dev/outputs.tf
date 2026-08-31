output "ecr_repository_urls" {
  value = {
    for service, repo in module.ecr :
    service => repo.repository_url
  }

  description = "ECR repository URLs for BondTradeX services"
}

output "vpc_id" {
  value = module.network.vpc_id
}


output "public_subnet_ids" {
  value = module.network.public_subnet_ids
}

output "private_app_subnet_ids" {
  value = module.network.private_app_subnet_ids
}

output "private_data_subnet_ids" {
  value = module.network.private_data_subnet_ids
}

output "eks_cluster_name" {
  value = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  value = module.eks.cluster_endpoint
}