#!/bin/bash
# Start application with merchant profile for production
echo "🏪 Starting TechMarket Pro in MERCHANT mode for production..."
echo "⚠️  WARNING: This will use live PayPal integration if configured!"
mvn spring-boot:run -Dspring-boot.run.profiles=merchant