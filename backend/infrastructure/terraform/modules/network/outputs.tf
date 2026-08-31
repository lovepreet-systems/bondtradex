output "vpc_id" {
  value       = aws_vpc.main.id
  description = "ID of the BondTradeX VPC"
}

output "private_app_subnet_ids" {
  value = [
    for key, subnet in aws_subnet.this :
    subnet.id
    if local.subnets[key].tier == "private-app"
  ]

  description = "IDs of private application subnets"
}

output "private_data_subnet_ids" {
  value = [
    for key, subnet in aws_subnet.this :
    subnet.id
    if local.subnets[key].tier == "private-data"
  ]

  description = "IDs of private data subnets"
}

output "public_subnet_ids" {
  value = [
    for key, subnet in aws_subnet.this :
    subnet.id
    if local.subnets[key].tier == "public"
  ]

  description = "IDs of public subnets"
}