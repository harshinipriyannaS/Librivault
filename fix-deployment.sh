#!/bin/bash

echo "🔧 LibriVault Deployment Fix Script"
echo "==================================="

# Function to wait for service
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f "$url" >/dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        
        echo "Attempt $attempt/$max_attempts: $service_name not ready yet..."
        sleep 10
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service_name failed to start within timeout"
    return 1
}

# Step 1: Clean up existing deployment
echo "🧹 Cleaning up existing deployment..."
docker-compose down --remove-orphans
docker system prune -f
docker volume prune -f

# Step 2: Pull latest images
echo "📥 Pulling latest Docker images..."
docker-compose pull

# Step 3: Start database first
echo "🗄️ Starting database..."
docker-compose up -d database

# Wait for database to be ready
echo "⏳ Waiting for database to initialize..."
sleep 30

DB_PASSWORD=${DB_PASSWORD:-Harshini@258}
timeout=120
while [ $timeout -gt 0 ]; do
    if docker-compose exec -T database mysqladmin ping -h localhost -u root -p"$DB_PASSWORD" --silent 2>/dev/null; then
        echo "✅ Database is ready!"
        break
    fi
    echo "Database still initializing... ($timeout seconds remaining)"
    sleep 5
    timeout=$((timeout-5))
done

if [ $timeout -le 0 ]; then
    echo "❌ Database failed to start"
    docker-compose logs database
    exit 1
fi

# Step 4: Start backend
echo "🚀 Starting backend..."
docker-compose up -d backend

# Wait for backend to be ready
if ! wait_for_service "Backend" "http://localhost:8080/api/health"; then
    echo "Backend logs:"
    docker-compose logs backend
    exit 1
fi

# Step 5: Start frontend
echo "🌐 Starting frontend..."
docker-compose up -d frontend

# Wait for frontend to be ready
if ! wait_for_service "Frontend" "http://localhost:4200"; then
    echo "Frontend logs:"
    docker-compose logs frontend
    exit 1
fi

# Step 6: Verify all services
echo "🔍 Final verification..."
docker-compose ps

# Test API connectivity
echo "Testing API connectivity..."
if curl -f http://localhost:4200/api/health >/dev/null 2>&1; then
    echo "✅ Frontend can reach backend API"
else
    echo "❌ Frontend cannot reach backend API"
    echo "This might cause 'failed to load resources' errors"
fi

# Set EC2 IP
EC2_IP="3.19.243.138"

echo ""
echo "🎉 Deployment completed!"
echo "📍 Application URLs:"
echo "   Frontend: http://$EC2_IP:4200"
echo "   Backend API: http://$EC2_IP:8080/api"
echo "   Health Check: http://$EC2_IP:8080/api/health"

echo ""
echo "🔍 If you still see 'failed to load resources':"
echo "1. Wait 2-3 minutes for all services to fully initialize"
echo "2. Check browser console (F12) for specific error messages"
echo "3. Try refreshing the page (Ctrl+F5)"
echo "4. Run: ./debug-deployment.sh for detailed diagnostics"

# Final health check
echo ""
echo "🏥 Final health check in 30 seconds..."
sleep 30

if curl -f http://localhost:4200 >/dev/null 2>&1 && curl -f http://localhost:8080/api/health >/dev/null 2>&1; then
    echo "✅ All services are healthy!"
else
    echo "⚠️  Some services may still be starting up"
    echo "Run './debug-deployment.sh' for detailed status"
fi