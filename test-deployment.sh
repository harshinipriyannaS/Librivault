#!/bin/bash

echo "🧪 LibriVault Deployment Test Script"
echo "===================================="
echo "Testing deployment at IP: 3.19.243.138"
echo ""

# Test frontend
echo "1. Testing Frontend (http://3.19.243.138:4200)..."
if curl -f -s http://3.19.243.138:4200 > /dev/null; then
    echo "✅ Frontend is accessible"
else
    echo "❌ Frontend is not accessible"
fi

# Test backend API
echo ""
echo "2. Testing Backend API (http://3.19.243.138:8080/api/health)..."
if curl -f -s http://3.19.243.138:8080/api/health > /dev/null; then
    echo "✅ Backend API is accessible"
    echo "Health check response:"
    curl -s http://3.19.243.138:8080/api/health | head -3
else
    echo "❌ Backend API is not accessible"
fi

# Test API proxy through frontend
echo ""
echo "3. Testing API Proxy (http://3.19.243.138:4200/api/health)..."
if curl -f -s http://3.19.243.138:4200/api/health > /dev/null; then
    echo "✅ API proxy is working"
else
    echo "❌ API proxy is not working - this will cause 'failed to load resources'"
fi

# Test CORS
echo ""
echo "4. Testing CORS configuration..."
CORS_RESPONSE=$(curl -s -H "Origin: http://3.19.243.138:4200" \
                     -H "Access-Control-Request-Method: GET" \
                     -H "Access-Control-Request-Headers: Content-Type" \
                     -X OPTIONS \
                     http://3.19.243.138:8080/api/health)

if echo "$CORS_RESPONSE" | grep -q "Access-Control-Allow-Origin"; then
    echo "✅ CORS is configured correctly"
else
    echo "❌ CORS may not be configured correctly"
fi

echo ""
echo "🌐 Application URLs:"
echo "   Frontend: http://3.19.243.138:4200"
echo "   Backend API: http://3.19.243.138:8080/api"
echo "   Health Check: http://3.19.243.138:8080/api/health"

echo ""
echo "📝 Next steps:"
echo "1. Open http://3.19.243.138:4200 in your browser"
echo "2. Check browser console (F12) for any errors"
echo "3. If you see 'failed to load resources', run ./debug-deployment.sh"