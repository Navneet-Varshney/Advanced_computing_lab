#!/bin/bash

# Faculty Database Web Application - Build and Run Script
# Usage: ./run.sh

set -e

echo "============================================"
echo "Faculty Database Web Application"
echo "============================================"
echo ""

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo "❌ Java compiler (javac) is not installed"
    echo "Please install Java Development Kit (JDK)"
    exit 1
fi

echo "✅ Java compiler found: $(javac -version 2>&1)"
echo ""

# Create output directory
mkdir -p backend/bin
mkdir -p backend/lib

echo "📦 Compiling Java backend..."

# Download GSON library if not present
if [ ! -f backend/lib/gson-2.10.json ]; then
    echo "📥 Downloading GSON library..."
    cd backend/lib
    # Using curl to download gson
    curl -L -o gson-2.10.1.jar "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" 2>/dev/null || {
        echo "⚠️  Could not download GSON automatically"
        echo "Please download gson from: https://github.com/google/gson/releases"
        echo "Save it as: backend/lib/gson-2.10.1.jar"
        cd ../..
    }
fi

cd backend

# Compile Java files with GSON in classpath
if [ -f lib/gson-2.10.1.jar ]; then
    CLASSPATH="lib/gson-2.10.1.jar:."
else
    echo "⚠️  GSON library not found, compiling without it may fail"
    CLASSPATH="."
fi

javac -d bin -cp "$CLASSPATH" src/com/faculty/*.java

echo "✅ Compilation successful!"
echo ""

# Create run script
echo "🚀 Starting server on http://localhost:8080"
echo ""
echo "Login credentials:"
echo "  Admin:  admin / admin123"
echo "  Faculty: prof1 / prof123"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Run the server
cd ..
java -cp "backend/bin:backend/lib/gson-2.10.1.jar" com.faculty.Server
