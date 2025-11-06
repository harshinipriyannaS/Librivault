# 🐳 Docker Build Fix for LibriVault Backend

## Problem
The Docker build is failing with:
```
ERROR: openjdk:21-jdk-slim: not found
```

## Root Cause
The `openjdk:21-jdk-slim` image doesn't exist in Docker Hub. OpenJDK official images were discontinued and replaced by Eclipse Temurin.

## ✅ Solution Applied

### 1. Updated Main Dockerfile
- Changed from `openjdk:21-jdk-slim` to `eclipse-temurin:21-jdk-jammy`
- Fixed user creation commands for Ubuntu base
- Improved file permissions handling

### 2. Created Alternative Dockerfiles
- **Dockerfile.alternative**: Uses Amazon Corretto (very reliable)
- **Dockerfile.simple**: Uses Ubuntu + OpenJDK (most compatible)

## 🚀 Quick Fix Options

### Option 1: Use Current Fixed Dockerfile
The main `Dockerfile` has been updated and should work now.

### Option 2: Use Amazon Corretto (Recommended)
```bash
cd backend
mv Dockerfile Dockerfile.original
mv Dockerfile.alternative Dockerfile
```

### Option 3: Use Simple Ubuntu-based
```bash
cd backend
mv Dockerfile Dockerfile.original  
mv Dockerfile.simple Dockerfile
```

## 🧪 Test Your Docker Build

Run this to test which Dockerfile works:
```bash
chmod +x test-docker-build.sh
./test-docker-build.sh
```

## 📋 Available Java 21 Images

### ✅ Working Images:
- `eclipse-temurin:21-jdk-jammy` (Ubuntu-based)
- `eclipse-temurin:21-jre-jammy` (Smaller, runtime only)
- `amazoncorretto:21-alpine-jdk` (Alpine-based, smaller)
- `amazoncorretto:21-jdk` (Amazon's OpenJDK)

### ❌ Non-existent Images:
- `openjdk:21-jdk-slim` (Doesn't exist)
- `openjdk:21-jre-slim` (Doesn't exist)

## 🔧 Manual Build Test

Test the build locally:
```bash
cd backend

# Test main Dockerfile
docker build -t librivault-backend .

# Test alternative
docker build -f Dockerfile.alternative -t librivault-backend .

# Test simple
docker build -f Dockerfile.simple -t librivault-backend .
```

## 🚀 Deploy After Fix

1. **Commit the changes:**
   ```bash
   git add .
   git commit -m "Fix: Update Dockerfile to use valid Java 21 image"
   git push origin main
   ```

2. **Monitor GitHub Actions:**
   - The CI/CD pipeline should now build successfully
   - Check the "Build & Push Docker Images" step

3. **If build still fails:**
   - Use one of the alternative Dockerfiles
   - Check GitHub Actions logs for specific errors

## 📊 Image Size Comparison

| Image | Size | Pros | Cons |
|-------|------|------|------|
| eclipse-temurin:21-jdk-jammy | ~400MB | Official, well-maintained | Larger |
| eclipse-temurin:21-jre-jammy | ~200MB | Smaller, runtime-only | JRE only |
| amazoncorretto:21-alpine-jdk | ~180MB | Very small, fast | Alpine-specific issues |

## 🎯 Recommended Solution

Use **Amazon Corretto** for production:
```dockerfile
FROM amazoncorretto:21-alpine-jdk
```

It's:
- ✅ Reliable and well-maintained by AWS
- ✅ Smaller image size
- ✅ Good performance
- ✅ Long-term support