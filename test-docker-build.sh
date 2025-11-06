#!/bin/bash

echo "🐳 Testing Docker Build Options for LibriVault Backend"
echo "===================================================="

cd backend

echo ""
echo "1. Testing main Dockerfile (Eclipse Temurin JDK)..."
if docker build -t librivault-backend-test . 2>/dev/null; then
    echo "✅ Main Dockerfile works!"
    docker rmi librivault-backend-test 2>/dev/null
else
    echo "❌ Main Dockerfile failed"
    
    echo ""
    echo "2. Testing alternative Dockerfile (Amazon Corretto)..."
    if docker build -f Dockerfile.alternative -t librivault-backend-test . 2>/dev/null; then
        echo "✅ Alternative Dockerfile works!"
        echo "💡 Consider using: docker build -f Dockerfile.alternative"
        docker rmi librivault-backend-test 2>/dev/null
    else
        echo "❌ Alternative Dockerfile failed"
        
        echo ""
        echo "3. Testing simple Dockerfile (Ubuntu + OpenJDK)..."
        if docker build -f Dockerfile.simple -t librivault-backend-test . 2>/dev/null; then
            echo "✅ Simple Dockerfile works!"
            echo "💡 Consider using: docker build -f Dockerfile.simple"
            docker rmi librivault-backend-test 2>/dev/null
        else
            echo "❌ All Dockerfiles failed"
            echo "🔧 Try updating Docker or checking internet connection"
        fi
    fi
fi

cd ..

echo ""
echo "📝 To use alternative Dockerfile:"
echo "1. Rename current Dockerfile: mv backend/Dockerfile backend/Dockerfile.original"
echo "2. Use alternative: mv backend/Dockerfile.alternative backend/Dockerfile"
echo "3. Or use simple: mv backend/Dockerfile.simple backend/Dockerfile"