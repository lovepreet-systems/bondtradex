locals {
  subnets = {
    public-a = {
      cidr = "10.10.1.0/24"
      az   = data.aws_availability_zones.available.names[0]
      tier = "public"
    }

    public-b = {
      cidr = "10.10.2.0/24"
      az   = data.aws_availability_zones.available.names[1]
      tier = "public"
    }

    private-app-a = {
      cidr = "10.10.11.0/24"
      az   = data.aws_availability_zones.available.names[0]
      tier = "private-app"
    }

    private-app-b = {
      cidr = "10.10.12.0/24"
      az   = data.aws_availability_zones.available.names[1]
      tier = "private-app"
    }

    private-data-a = {
      cidr = "10.10.21.0/24"
      az   = data.aws_availability_zones.available.names[0]
      tier = "private-data"
    }

    private-data-b = {
      cidr = "10.10.22.0/24"
      az   = data.aws_availability_zones.available.names[1]
      tier = "private-data"
    }
  }
}

resource "aws_subnet" "this" {
  for_each = local.subnets

  vpc_id            = aws_vpc.main.id
  cidr_block        = each.value.cidr
  availability_zone = each.value.az

  tags = {
    Name        = "bondtradex-${var.environment}-${each.key}"
    Environment = var.environment
    Project     = "bondtradex"
    Tier        = each.value.tier
  }
}

resource "aws_vpc" "main"{
    cidr_block=var.vpc_cidr

    enable_dns_support=true
    enable_dns_hostnames=true

    tags = {
    Name        = "bondtradex-${var.environment}-vpc"
    Environment = var.environment
    Project     = "bondtradex"
        }
 }

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name        = "bondtradex-${var.environment}-igw"
    Environment = var.environment
    Project     = "bondtradex"
  }
}

resource "aws_route_table_association" "public" {
  for_each = {
    for key, subnet in local.subnets :
    key => subnet
    if subnet.tier == "public"
  }

  subnet_id      = aws_subnet.this[each.key].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name        = "bondtradex-${var.environment}-public-rt"
    Environment = var.environment
    Project     = "bondtradex"
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}


resource "aws_route_table" "private_data" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name        = "bondtradex-${var.environment}-private-data-rt"
    Environment = var.environment
    Project     = "bondtradex"
  }
}


resource "aws_route_table_association" "private_data" {

     for_each = {
        for key, subnet in local.subnets :
        key => subnet
        if subnet.tier == "private-data"
      }

  subnet_id      = aws_subnet.this[each.key].id
  route_table_id = aws_route_table.private_data.id
}

resource "aws_eip" "nat" {
  count = var.enable_nat_gateway ? 1 : 0

  domain = "vpc"

  tags = {
    Name        = "bondtradex-${var.environment}-nat-eip"
    Environment = var.environment
    Project     = "bondtradex"
  }
}

resource "aws_nat_gateway" "main" {
  count = var.enable_nat_gateway ? 1 : 0

  allocation_id = aws_eip.nat[0].id

  subnet_id = aws_subnet.this["public-a"].id

  tags = {
    Name        = "bondtradex-${var.environment}-nat"
    Environment = var.environment
    Project     = "bondtradex"
  }

  depends_on = [
    aws_internet_gateway.main
  ]
}

resource "aws_route_table" "private_app" {
  vpc_id = aws_vpc.main.id

  dynamic "route" {
    for_each = var.enable_nat_gateway ? [1] : []

    content {
      cidr_block     = "0.0.0.0/0"
      nat_gateway_id = aws_nat_gateway.main[0].id
    }
  }

  tags = {
    Name        = "bondtradex-${var.environment}-private-app-rt"
    Environment = var.environment
    Project     = "bondtradex"
  }
}

resource "aws_route_table_association" "private_app" {
  for_each = {
    for key, subnet in local.subnets :
    key => subnet
    if subnet.tier == "private-app"
  }

  subnet_id      = aws_subnet.this[each.key].id
  route_table_id = aws_route_table.private_app.id
}