#!/bin/bash

echo "🔍 LibriVault Deployment Debug Script"
echo "======================================"

# Get EC2 instance IP
EC2_IP="3.19.243.138"
echo "🌍 EC2 Public IP: $EC2_IP"

# Check Docker services
echo ""
echo "📦 Checking Docker containers..."
docker-compose ps

echo ""
echo "🌐 Testing service connectivity..."

# Test database first
echo "1. Testing database..."
if docker-compose exec -T database mysqladmin ping -h localhost -u root -p${DB_PASSWORD:-Harshini@258} --silent 2>/dev/null; then
    echo "✅ Database is responding"
else
    echo "❌ Database is not responding"
    echo "Database logs (last 10 lines):"
    docker-compose logs --tail=10 database
fi

# Test backend
echo ""
echo "2. Testing backend health endpoint..."
if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "✅ Backend is responding"
    echo "Backend health check:"
    curl -s http://localhost:8080/api/health | head -5
else
    echo "❌ Backend is not responding"
    echo "Backend logs (last 15 lines):"
    docker-compose logs --tail=15 backend
    
    # Check if backend container is running
    if docker-compose ps backend | grep -q "Up"; then
        echo "Backend container is running but not responding to health checks"
    else
        echo "Backend container is not running properly"
    fi
fi

# Test frontend
echo ""
echo "3. Testing frontend..."
if curl -f http://localhost:4200 > /dev/null 2>&1; then
    echo "✅ Frontend is responding"
else
    echo "❌ Frontend is not responding"
    echo "Frontend logs (last 10 lines):"
    docker-compose logs --tail=10 frontend
    
    # Check nginx configuration
    echo "Checking nginx configuration:"
    docker-compose exec frontend nginx -t 2>/dev/null || echo "❌ Nginx config test failed"
fi

echo ""
echo "🔍 Network connectivity tests..."

# Test internal network connectivity
echo "Testing internal Docker network..."
if docker-compose exec frontend nslookup backend >/dev/null 2>&1; then
    echo "✅ Frontend can resolve backend hostname"
else
    echo "❌ Frontend cannot resolve backend hostname"
fi

# Test API proxy
echo "Testing API proxy from frontend to backend..."
if docker-compose exec frontend curl -f http://backend:8080/api/health >/dev/null 2>&1; then
    echo "✅ Frontend can reach backend directly"
else
    echo "❌ Frontend cannot reach backend directly"
fi

echo ""
echo "🌍 External connectivity tests..."

# Test external access
echo "Testing external access to services..."
echo "Frontend URL: http://$EC2_IP:4200"
echo "Backend URL: http://$EC2_IP:8080/api/health"

if curl -f http://$EC2_IP:4200 >/dev/null 2>&1; then
    echo "✅ Frontend accessible externally"
else
    echo "❌ Frontend not accessible externally"
fi

if curl -f http://$EC2_IP:8080/api/health >/dev/null 2>&1; then
    echo "✅ Backend accessible externally"
else
    echo "❌ Backend not accessible externally"
fi

echo ""
echo "📊 Resource usage..."
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"

echo ""
echo "🔧 Common fixes to try:"
echo "1. Restart all services: docker-compose down && docker-compose up -d"
echo "2. Pull latest images: docker-compose pull && docker-compose up -d"
echo "3. Check specific logs: docker-compose logs [service-name]"
echo "4. Rebuild images: docker-compose build --no-cache && docker-compose up -d"
echo "5. Check disk space: df -h"
echo "6. Check memory: free -h"

echo ""
echo "🚨 If frontend shows 'failed to load resources':"
echo "1. Check browser console (F12) for specific errors"
echo "2. Verify API calls in Network tab"
echo "3. Check CORS errors"
echo "4. Ensure backend is fully started before frontend"

# Check for common issues
echo ""
echo "🔍 Checking for common issues..."

# Check disk space
DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -gt 90 ]; then
    echo "⚠️  WARNING: Disk usage is ${DISK_USAGE}% - this may cause issues"
fi

# Check memory
MEM_AVAILABLE=$(free | grep Mem | awk '{print ($7/$2) * 100.0}')
if [ "$(echo "$MEM_AVAILABLE < 10" | bc -l 2>/dev/null || echo 0)" -eq 1 ]; then
    echo "⚠️  WARNING: Low memory available"
fi

# Check if ports are in use
echo "Checking port usage..."
netstat -tlnp 2>/dev/null | grep -E ':(4200|8080|3308)' || echo "Port check failed - netstat not available"

echo ""
echo "✅ Debug script completed!"