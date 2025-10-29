# LibriVault Deployment Guide

## 🚀 Complete CI/CD Setup

This guide will help you set up automatic deployment for LibriVault using GitHub Actions and AWS EC2.

## 📋 Prerequisites

1. **GitHub Account** with your LibriVault repository
2. **Docker Hub Account** for storing images
3. **AWS Account** for EC2 instance

## 🔧 Step 1: Create EC2 Instance

### Launch EC2 Instance:
1. Go to AWS EC2 Console
2. Click "Launch Instance"
3. Choose **Ubuntu Server 22.04 LTS**
4. Select instance type (recommended: **t3.medium** or larger)
5. Create or select a key pair
6. Configure Security Group with these ports:
   - **SSH (22)** - Your IP only
   - **HTTP (80)** - Anywhere
   - **HTTPS (443)** - Anywhere
   - **Custom TCP (4200)** - Anywhere (Angular)
   - **Custom TCP (8080)** - Anywhere (Spring Boot)

### 🔥 IMPORTANT: Add User Data Script
In the "Advanced Details" section, paste the contents of `ec2-user-data.sh`:

```bash
#!/bin/bash
# Copy the entire content from ec2-user-data.sh file
```

7. Launch the instance
8. Wait 5-10 minutes for the setup script to complete

## 🔧 Step 2: Set up GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add these secrets:

```
DOCKER_USERNAME=your-dockerhub-username
DOCKER_PASSWORD=your-dockerhub-password
EC2_HOST=your-ec2-public-ip
EC2_USERNAME=ubuntu
EC2_PRIVATE_KEY=your-ec2-private-key-content
DB_PASSWORD=Harshini@258
JWT_SECRET=LibriVaultSecureKey123
```

### 📝 How to get EC2_PRIVATE_KEY:
1. Open your `.pem` key file in a text editor
2. Copy the entire content (including `-----BEGIN RSA PRIVATE KEY-----` and `-----END RSA PRIVATE KEY-----`)
3. Paste it as the value for `EC2_PRIVATE_KEY` secret

## 🔧 Step 3: Verify EC2 Setup

SSH into your EC2 instance:
```bash
ssh -i your-key.pem ubuntu@your-ec2-ip
```

Check the setup status:
```bash
cat /opt/librivault/setup-status.txt
```

You should see all checkmarks (✅) indicating successful installation.

## 🔧 Step 4: Test the CI/CD Pipeline

1. Make any small change to your code
2. Commit and push to the `main` branch:
   ```bash
   git add .
   git commit -m "Test CI/CD pipeline"
   git push origin main
   ```

3. Go to GitHub → Actions tab to watch the pipeline run

## 📊 Pipeline Flow

```
Code Push → GitHub Actions → Build Images → Push to Docker Hub → Deploy to EC2 → Pull Images → Run Containers
```

### What happens automatically:
1. **Test Stage**: Compiles backend and builds frontend
2. **Build & Push Stage**: Creates Docker images and pushes to Docker Hub
3. **Deploy Stage**: SSH to EC2, pulls images, and runs containers

## 🌐 Access Your Application

After successful deployment:
- **Frontend**: `http://your-ec2-ip:4200`
- **Backend API**: `http://your-ec2-ip:8080/api`
- **Health Check**: `http://your-ec2-ip:8080/api/health`

## 🔧 Useful Commands on EC2

```bash
# Check container status
docker ps

# View application logs
cd /opt/librivault
docker-compose logs

# Manual deployment
./deploy.sh

# System monitoring
./monitor.sh

# Restart containers
docker-compose restart
```

## 🐛 Troubleshooting

### If deployment fails:

1. **Check GitHub Actions logs** for build errors
2. **SSH to EC2** and check:
   ```bash
   docker ps
   docker-compose logs
   ```
3. **Verify secrets** are correctly set in GitHub
4. **Check EC2 security groups** allow required ports

### Common Issues:

- **Docker not found**: EC2 user data script didn't complete - check `/var/log/cloud-init-output.log`
- **Permission denied**: Make sure EC2_PRIVATE_KEY secret is correct
- **Connection refused**: Check security group settings
- **Images not pulling**: Verify Docker Hub credentials

## 🔄 Manual Deployment (if needed)

If you need to deploy manually:

```bash
# SSH to EC2
ssh -i your-key.pem ubuntu@your-ec2-ip

# Navigate to app directory
cd /opt/librivault

# Create docker-compose.yml (if not exists)
# Copy the docker-compose.yml from your repository

# Create .env file
cat > .env << EOF
DB_HOST=database
DB_PORT=3306
DB_NAME=librivault_db
DB_USERNAME=root
DB_PASSWORD=Harshini@258
JWT_SECRET=LibriVaultSecureKey123
CORS_ALLOWED_ORIGINS=http://your-ec2-ip:4200
BACKEND_IMAGE=your-dockerhub-username/librivault-backend:latest
FRONTEND_IMAGE=your-dockerhub-username/librivault-frontend:latest
EOF

# Deploy
./deploy.sh
```

## 🎉 Success!

Your LibriVault application is now set up with automatic CI/CD deployment. Every time you push to the main branch, your application will automatically update on EC2!

## 📞 Support

If you encounter issues:
1. Check GitHub Actions logs
2. Check EC2 system logs: `sudo tail -f /var/log/cloud-init-output.log`
3. Check application logs: `docker-compose logs`