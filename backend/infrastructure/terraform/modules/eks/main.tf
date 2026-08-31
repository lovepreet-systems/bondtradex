# =========================================================
# EKS CLUSTER IAM ROLE
# AWS EKS Control Plane assumes this role.
# =========================================================

resource "aws_iam_role" "eks_cluster" {
  name = "${var.cluster_name}-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"

    Statement = [{
      Effect = "Allow"

      Principal = {
        Service = "eks.amazonaws.com"
      }

      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Environment = var.environment
    Project     = "bondtradex"
  }
}


# =========================================================
# EKS CLUSTER IAM POLICY
# Gives the EKS control plane permissions to interact
# with required AWS resources.
# =========================================================

resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  role       = aws_iam_role.eks_cluster.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}


# =========================================================
# EKS CLUSTER
# AWS manages the Kubernetes control plane.
# =========================================================

resource "aws_eks_cluster" "this" {
  name     = var.cluster_name
  role_arn = aws_iam_role.eks_cluster.arn

  vpc_config {
    subnet_ids = var.subnet_ids

    # Allows communication with the Kubernetes API
    # through the VPC.
    endpoint_private_access = true

    # Allows kubectl from our local machine.
    # For production this can be restricted further.
    endpoint_public_access = true
  }

  tags = {
    Environment = var.environment
    Project     = "bondtradex"
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy
  ]
}


# =========================================================
# EKS WORKER NODE IAM ROLE
# EC2 worker nodes assume this role.
# =========================================================

resource "aws_iam_role" "eks_node" {
  name = "${var.cluster_name}-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"

    Statement = [{
      Effect = "Allow"

      Principal = {
        Service = "ec2.amazonaws.com"
      }

      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Environment = var.environment
    Project     = "bondtradex"
  }
}


# =========================================================
# WORKER NODE POLICY
# Allows EC2 worker nodes to operate as EKS workers.
# =========================================================

resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  role       = aws_iam_role.eks_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}


# =========================================================
# ECR PULL POLICY
# Allows worker nodes to pull Docker images from ECR.
# =========================================================

resource "aws_iam_role_policy_attachment" "ecr_pull_policy" {
  role       = aws_iam_role.eks_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPullOnly"
}


# =========================================================
# VPC CNI POLICY
#
# Allows the AWS VPC CNI to manage networking required
# for Kubernetes Pods.
#
# For this DEV learning environment we attach it to the
# node role.
#
# In a production setup, we would normally give the
# aws-node CNI its own IAM role using EKS Pod Identity
# or IRSA instead of putting these permissions on every
# worker node.
# =========================================================

resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  role       = aws_iam_role.eks_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}


# =========================================================
# EKS MANAGED NODE GROUP
#
# AWS manages the EC2 worker-node lifecycle.
#
# DEV:
#   desired = 1
#   minimum = 1
#   maximum = 2
#
# Worker nodes run inside our PRIVATE APP subnets.
# =========================================================

resource "aws_eks_node_group" "this" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${var.cluster_name}-nodes"

  node_role_arn = aws_iam_role.eks_node.arn
  subnet_ids    = var.subnet_ids

  scaling_config {
    desired_size = 1
    min_size     = 1
    max_size     = 2
  }

  instance_types = ["t3.medium"]

  tags = {
    Environment = var.environment
    Project     = "bondtradex"
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.ecr_pull_policy,
    aws_iam_role_policy_attachment.eks_cni_policy
  ]
}