################################################################################
# Input Variables
################################################################################

variable "name" {
  type = string
  description = "Name of the instance"
  validation {
    condition = length(var.region) > 0
    error_message = "Name is required"
  }
}

variable "region" {
  type = string
  description = "AWS region to provision into"
  default = "us-east-1"
  validation {
    condition = length(var.region) > 0
    error_message = "Region is required"
  }
}

variable "size" {
  type = string
  description = "Instance size to use"
  default = "t3.small-v2"
  validation {
    condition = length(var.region) > 0
    error_message = "Instance size is required"
  }
}

variable "ami" {
  type = string
  description = "AWS AMI for the instance"
  validation {
    condition     = length(var.ami) > 0 && can(regex("^ami-", var.ami))
    error_message = "The image_id value must be a valid AMI id, starting with \"ami-\"."
  }
}

variable "tags" {
  type = map(string)
  description = "Tags to apply to the instance"
  default = {
    usage = "testing platform"
  }
}

################################################################################
# Provider Configuration
################################################################################

provider "aws" {
  region = var.region
}

################################################################################
# Supporting Resources
################################################################################

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 3.0"

  name = "${var.name}-vpc"
  cidr = "10.99.0.0/18"

  azs              = ["${var.region}a", "${var.region}b", "${var.region}c"]
  public_subnets   = ["10.99.0.0/24", "10.99.1.0/24", "10.99.2.0/24"]
  private_subnets  = ["10.99.3.0/24", "10.99.4.0/24", "10.99.5.0/24"]
  database_subnets = ["10.99.7.0/24", "10.99.8.0/24", "10.99.9.0/24"]

  tags = var.tags
}

resource "aws_network_interface" "this" {
  subnet_id = element(module.vpc.private_subnets, 0)
}

################################################################################
# EC2 Module
################################################################################

module "testing_platform_box" {
  source = "terraform-aws-modules/ec2-instance/aws"

  name = "${var.name}-network-interface"

  ami           = var.ami
  subnet_id     = element(module.vpc.private_subnets, 0)
  instance_type = var.size

  network_interface = [
    {
      device_index          = 0
      network_interface_id  = aws_network_interface.this.id
      delete_on_termination = false
    }
  ]

  tags = var.tags
}

################################################################################
# Output Variables
################################################################################

output "instance_connection_point" {
  value = module.testing_platform_box.public_ip
}
