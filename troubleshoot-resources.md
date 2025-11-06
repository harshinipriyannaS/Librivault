# 🚨 "Failed to Load Resources" Troubleshooting Guide

## Common Causes and Solutions

### 1. **Frontend Cannot Reach Backend API**

**Symptoms:**
- Browser shows "Failed to load resources"
- Network tab shows 404 or connection errors for `/api/*` requests
- Console errors about API calls failing

**Solutions:**
```bash
# Check if backend is running
curl http://3.19.243.138:8080/api/health

# Check if API proxy is working
curl http://3.19.243.138:4200/api/health

# If backend is down, restart it
docker-compose restart backend

# If proxy isn't working, restart frontend
docker-compose restart frontend
```

### 2. **CORS (Cross-Origin Resource Sharing) Issues**

**Symptoms:**
- Console shows CORS policy errors
- API calls are blocked by browser
- "Access to fetch at '...' has been blocked by CORS policy"

**Solutions:**
```bash
# Check CORS configuration in backend logs
docker-compose logs backend | grep -i cors

# Verify CORS origins in environment
echo $CORS_ALLOWED_ORIGINS

# Update CORS configuration if needed
# Edit .env file and restart backend
```

### 3. **Services Starting in Wrong Order**

**Symptoms:**
- Frontend loads but shows errors
- Backend starts before database is ready
- Intermittent connection issues

**Solutions:**
```bash
# Use the fix script to start services in correct order
chmod +x fix-deployment.sh
./fix-deployment.sh
```

### 4. **Network Configuration Issues**

**Symptoms:**
- Services can't communicate with each other
- DNS resolution failures
- Connection timeouts

**Solutions:**
```bash
# Check Docker network
docker network ls
docker network inspect librivault-app_librivault-network

# Test internal connectivity
docker-compose exec frontend nslookup backend
docker-compose exec frontend curl http://backend:8080/api/health
```

### 5. **Resource/Memory Issues**

**Symptoms:**
- Services crash or restart frequently
- Slow response times
- Out of memory errors

**Solutions:**
```bash
# Check system resources
free -h
df -h

# Check container resource usage
docker stats

# Restart services if needed
docker-compose restart
```

## Step-by-Step Debugging Process

### Step 1: Check Service Status
```bash
# Run the debug script
chmod +x debug-deployment.sh
./debug-deployment.sh
```

### Step 2: Check Browser Console
1. Open browser (Chrome/Firefox)
2. Press F12 to open Developer Tools
3. Go to Console tab
4. Look for error messages
5. Go to Network tab
6. Refresh page and check failed requests

### Step 3: Test API Endpoints Directly
```bash
# Test backend health
curl -v http://3.19.243.138:8080/api/health

# Test frontend
curl -v http://3.19.243.138:4200

# Test API through frontend proxy
curl -v http://3.19.243.138:4200/api/health
```

### Step 4: Check Service Logs
```bash
# Check all logs
docker-compose logs

# Check specific service logs
docker-compose logs frontend
docker-compose logs backend
docker-compose logs database

# Follow logs in real-time
docker-compose logs -f backend
```

### Step 5: Verify Configuration
```bash
# Check environment variables
docker-compose exec backend env | grep -E "(DB_|API_|CORS_)"
docker-compose exec frontend env

# Check nginx configuration
docker-compose exec frontend nginx -t
```

## Quick Fixes

### Fix 1: Complete Restart
```bash
docker-compose down
docker-compose up -d
```

### Fix 2: Rebuild Images
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Fix 3: Clean Restart
```bash
docker-compose down --volumes --remove-orphans
docker system prune -f
docker-compose up -d
```

### Fix 4: Use Fix Script
```bash
chmod +x fix-deployment.sh
./fix-deployment.sh
```

## Browser-Specific Issues

### Chrome/Edge
- Clear cache: Ctrl+Shift+Delete
- Hard refresh: Ctrl+F5
- Disable cache in DevTools (F12 → Network → Disable cache)

### Firefox
- Clear cache: Ctrl+Shift+Delete
- Hard refresh: Ctrl+F5
- Check CORS settings in about:config

## Security Group / Firewall Issues

If running on AWS EC2, ensure these ports are open:
- Port 4200 (Frontend)
- Port 8080 (Backend API)
- Port 22 (SSH)

```bash
# Check if ports are listening
netstat -tlnp | grep -E ':(4200|8080)'

# Test external connectivity
curl http://3.19.243.138:4200
curl http://3.19.243.138:8080/api/health
```

## Environment-Specific Fixes

### Development Environment
```bash
# Use development docker-compose
docker-compose -f docker-compose.yml up -d
```

### Production Environment
```bash
# Ensure production environment variables are set
export SPRING_PROFILES_ACTIVE=production
export CORS_ALLOWED_ORIGINS=http://3.19.243.138:4200

# Use production configuration
docker-compose up -d
```

## When All Else Fails

1. **Check EC2 instance resources:**
   ```bash
   top
   free -h
   df -h
   ```

2. **Restart EC2 instance:**
   ```bash
   sudo reboot
   ```

3. **Check AWS Security Groups:**
   - Ensure ports 4200 and 8080 are open
   - Verify source IP ranges

4. **Check application logs on EC2:**
   ```bash
   journalctl -u docker
   dmesg | tail
   ```

5. **Contact support with:**
   - Output of `./debug-deployment.sh`
   - Browser console errors
   - Service logs
   - System resource usage