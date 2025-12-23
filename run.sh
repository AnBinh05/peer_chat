#!/bin/bash

echo "========================================"
echo "  PeerTalk - P2P Chat Application"
echo "========================================"
echo ""

echo "[1/2] Building project..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Build failed!"
    exit 1
fi

echo ""
echo "[2/2] Running application..."
mvn javafx:run

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Application failed to start!"
    echo ""
    echo "Troubleshooting:"
    echo "1. Make sure MySQL is running"
    echo "2. Make sure database 'peertalk' exists"
    echo "3. Check database configuration in Database.java"
    echo ""
fi


