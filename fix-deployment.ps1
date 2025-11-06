# LibriVault Deployment Fix Script (PowerShell)
Write-Host "🔧 LibriVault Deployment Fix Script" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan

# Function to wait for service
function Wait-ForService {
    param(
        [string]$ServiceName,
        [string]$Url,
        [int]$MaxAttempts = 30
    )
    
    Write-Host "Waiting for $ServiceName to be ready..." -ForegroundColor Yellow
    
    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
        try {
            $response = Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host "✅ $ServiceName is ready!" -ForegroundColor Green
                return $true
            }
        }
        catch {
            # Service not ready yet
        }
        
        Write-Host "Attempt $attempt/$MaxAttempts`: $ServiceName not ready yet..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
    }
    
    Write-Host "❌ $ServiceName failed to start within timeout" -ForegroundColor Red
    return $false
}

# Step 1: Clean up existing deployment
Write-Host "🧹 Cleaning up existing deployment..." -ForegroundColor Cyan
docker-compose down --remove-orphans
docker system prune -f

# Step 2: Pull latest images
Write-Host "📥 Pulling latest Docker images..." -ForegroundColor Cyan
docker-compose pull

# Step 3: Start database first
Write-Host "🗄️ Starting database..." -ForegroundColor Cyan
docker-compose up -d database

# Wait for database to be ready
Write-Host "⏳ Waiting for database to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

$dbPassword = $env:DB_PASSWORD
if (-not $dbPassword) { $dbPassword = "Harshini@258" }

$timeout = 120
$dbReady = $false

while ($timeout -gt 0 -and -not $dbReady) {
    try {
        $result = docker-compose exec -T database mysqladmin ping -h localhost -u root -p"$dbPassword" --silent 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Database is ready!" -ForegroundColor Green
            $dbReady = $true
            break
        }
    }
    catch {
        # Database not ready
    }
    
    Write-Host "Database still initializing... ($timeout seconds remaining)" -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    $timeout -= 5
}

if (-not $dbReady) {
    Write-Host "❌ Database failed to start" -ForegroundColor Red
    docker-compose logs database
    exit 1
}

# Step 4: Start backend
Write-Host "🚀 Starting backend..." -ForegroundColor Cyan
docker-compose up -d backend

# Wait for backend to be ready
if (-not (Wait-ForService -ServiceName "Backend" -Url "http://localhost:8080/api/health")) {
    Write-Host "Backend logs:" -ForegroundColor Red
    docker-compose logs backend
    exit 1
}

# Step 5: Start frontend
Write-Host "🌐 Starting frontend..." -ForegroundColor Cyan
docker-compose up -d frontend

# Wait for frontend to be ready
if (-not (Wait-ForService -ServiceName "Frontend" -Url "http://localhost:4200")) {
    Write-Host "Frontend logs:" -ForegroundColor Red
    docker-compose logs frontend
    exit 1
}

# Step 6: Verify all services
Write-Host "🔍 Final verification..." -ForegroundColor Cyan
docker-compose ps

# Test API connectivity
Write-Host "Testing API connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:4200/api/health" -Method Get -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Frontend can reach backend API" -ForegroundColor Green
    }
}
catch {
    Write-Host "❌ Frontend cannot reach backend API" -ForegroundColor Red
    Write-Host "This might cause 'failed to load resources' errors" -ForegroundColor Red
}

# Set EC2 IP
$ec2Ip = "3.19.243.138"

Write-Host ""
Write-Host "🎉 Deployment completed!" -ForegroundColor Green
Write-Host "📍 Application URLs:" -ForegroundColor Cyan
Write-Host "   Frontend: http://$ec2Ip`:4200" -ForegroundColor White
Write-Host "   Backend API: http://$ec2Ip`:8080/api" -ForegroundColor White
Write-Host "   Health Check: http://$ec2Ip`:8080/api/health" -ForegroundColor White

Write-Host ""
Write-Host "🔍 If you still see 'failed to load resources':" -ForegroundColor Yellow
Write-Host "1. Wait 2-3 minutes for all services to fully initialize" -ForegroundColor White
Write-Host "2. Check browser console (F12) for specific error messages" -ForegroundColor White
Write-Host "3. Try refreshing the page (Ctrl+F5)" -ForegroundColor White
Write-Host "4. Run debug script for detailed diagnostics" -ForegroundColor White

# Final health check
Write-Host ""
Write-Host "🏥 Final health check in 30 seconds..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

$frontendHealthy = $false
$backendHealthy = $false

try {
    $response = Invoke-WebRequest -Uri "http://localhost:4200" -Method Get -TimeoutSec 10
    if ($response.StatusCode -eq 200) { $frontendHealthy = $true }
}
catch { }

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -Method Get -TimeoutSec 10
    if ($response.StatusCode -eq 200) { $backendHealthy = $true }
}
catch { }

if ($frontendHealthy -and $backendHealthy) {
    Write-Host "✅ All services are healthy!" -ForegroundColor Green
}
else {
    Write-Host "⚠️  Some services may still be starting up" -ForegroundColor Yellow
    Write-Host "Check individual service status with: docker-compose ps" -ForegroundColor White
}