#!/bin/bash

# E-Commerce Demo Application Stop Script
# This script stops all running microservices

echo "Stopping E-Commerce Demo Application..."
echo "======================================="

if [ -f .pids ]; then
    while IFS= read -r pid; do
        if ps -p $pid > /dev/null 2>&1; then
            echo "Stopping process $pid..."
            kill $pid
        else
            echo "Process $pid is not running"
        fi
    done < .pids
    
    rm .pids
    echo ""
    echo "All services stopped successfully!"
else
    echo "No PID file found. Services may not be running."
    echo "Attempting to find and stop services by port..."
    
    # Try to find and kill processes by port
    for port in 8080 8081 8082 8083; do
        PID=$(lsof -ti:$port)
        if [ ! -z "$PID" ]; then
            echo "Stopping process on port $port (PID: $PID)..."
            kill $PID
        fi
    done
fi

echo "======================================="

