moved {
  from = module.ecr_ioi.aws_ecr_repository.this
  to   = module.ecr["ioi-service"].aws_ecr_repository.this
}