#!/bin/bash

# Update system
sudo apt-get update -y

# Install Git
sudo apt-get install -y git

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu
sudo systemctl start docker
sudo systemctl enable docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.21.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Configure firewall
sudo ufw --force enable
sudo ufw allow ssh
sudo ufw allow 4200
sudo ufw allow 8080

# Create app directory
sudo mkdir -p /opt/librivault
sudo chown ubuntu:ubuntu /opt/librivault

# Clean up
rm -f get-docker.sh