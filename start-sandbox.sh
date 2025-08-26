#!/bin/bash
# Start application with sandbox profile for development
echo "🧪 Starting TechMarket Pro in SANDBOX mode for development testing..."
mvn spring-boot:run -Dspring-boot.run.profiles=sandbox