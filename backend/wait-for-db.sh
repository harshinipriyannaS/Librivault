#!/bin/bash

# Wait for database to be ready
echo "Waiting for database to be ready..."

# Default values
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_USERNAME=${DB_USERNAME:-root}
DB_PASSWORD=${DB_PASSWORD:-password}

# Maximum wait time in seconds
MAX_WAIT=120
WAIT_TIME=0

while [ $WAIT_TIME -lt $MAX_WAIT ]; do
    if mysqladmin ping -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" --silent; then
        echo "Database is ready!"
        break
    fi
    
    echo "Database not ready yet. Waiting... ($WAIT_TIME/$MAX_WAIT seconds)"
    sleep 5
    WAIT_TIME=$((WAIT_TIME + 5))
done

if [ $WAIT_TIME -ge $MAX_WAIT ]; then
    echo "Database failed to become ready within $MAX_WAIT seconds"
    exit 1
fi

echo "Starting Spring Boot application..."

# Add a small delay to ensure database is fully ready for connections
echo "Waiting additional 10 seconds for database to be fully ready..."
sleep 10

exec java -jar app.jar