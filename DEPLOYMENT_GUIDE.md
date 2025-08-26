# TechMarket Pro - Deployment Guide

## 🚀 Complete Website Deployment Guide

This guide covers deploying TechMarket Pro as a complete Spring Boot web application with REST API support.

### 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB (optional - H2 is default)
- Git

### 🏗️ Application Architecture

- **Backend**: Spring Boot 3.1.0 with REST API
- **Frontend**: Thymeleaf templates with modern CSS/JavaScript
- **Database**: H2 (default) or MongoDB
- **Payment**: PayPal integration
- **Hosting**: Can be deployed on any platform supporting Java

### ⚙️ Configuration Options

#### Option 1: H2 Database (Default - No Setup Required)
```properties
app.database.type=h2
```

#### Option 2: MongoDB (Requires MongoDB Installation)
```properties
app.database.type=mongodb
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=techmarketpro
```

### 🔧 MongoDB Setup (If Using MongoDB)

#### Install MongoDB on Your Machine:

**Windows:**
1. Download MongoDB Community Server from [MongoDB Download Center](https://www.mongodb.com/try/download/community)
2. Run the installer with default settings
3. Start MongoDB service: `net start MongoDB`

**macOS:**
```bash
# Using Homebrew
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb/brew/mongodb-community
```

**Linux (Ubuntu):**
```bash
# Import the public key
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -

# Create a list file for MongoDB
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list

# Reload local package database
sudo apt-get update

# Install MongoDB
sudo apt-get install -y mongodb-org

# Start MongoDB
sudo systemctl start mongod
sudo systemctl enable mongod
```

#### Switch to MongoDB:
1. Update `application.properties`:
```properties
app.database.type=mongodb
```
2. Restart the application

### 🚀 Local Development

1. **Clone and Setup:**
```bash
git clone <your-repo>
cd marketplace-app
mvn clean install
```

2. **Run the Application:**
```bash
mvn spring-boot:run
```

3. **Access the Website:**
- Main website: http://localhost:8081
- REST API: http://localhost:8081/api/products
- Database console (H2): http://localhost:8081/h2-console

### 🌐 Production Deployment Options

#### Option 1: Heroku Deployment

1. **Create Heroku App:**
```bash
heroku create your-marketplace-app
```

2. **Add MongoDB (if using):**
```bash
heroku addons:create mongolab:sandbox
```

3. **Configure Environment Variables:**
```bash
heroku config:set APP_DATABASE_TYPE=mongodb
heroku config:set SPRING_DATA_MONGODB_URI=<your-mongodb-uri>
```

4. **Deploy:**
```bash
git push heroku main
```

#### Option 2: AWS Deployment

1. **Create application.yml for production:**
```yaml
server:
  port: 5000
app:
  database:
    type: mongodb
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

2. **Build JAR:**
```bash
mvn clean package
```

3. **Deploy to AWS Elastic Beanstalk or EC2**

#### Option 3: Docker Deployment

1. **Create Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/marketplace-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]
```

2. **Create docker-compose.yml:**
```yaml
version: '3.8'
services:
  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
  
  marketplace-app:
    build: .
    ports:
      - "8081:8081"
    environment:
      - APP_DATABASE_TYPE=mongodb
      - SPRING_DATA_MONGODB_HOST=mongodb
    depends_on:
      - mongodb

volumes:
  mongodb_data:
```

3. **Run:**
```bash
docker-compose up -d
```

### 🔗 REST API Endpoints

The application provides a complete REST API:

- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `POST /api/payments/paypal` - Process PayPal payment

### 🎨 Website Features

- **Responsive Design**: Works on desktop, tablet, and mobile
- **Modern UI**: Professional gradient design with animations
- **PayPal Integration**: Secure payment processing
- **REST API**: Full CRUD operations
- **Database Flexibility**: Supports both H2 and MongoDB
- **Admin Interface**: Product management capabilities

### 🔒 Security Considerations

1. **Production Configuration:**
```properties
# Disable H2 console in production
spring.h2.console.enabled=false

# Use environment variables for sensitive data
spring.data.mongodb.username=${MONGO_USERNAME}
spring.data.mongodb.password=${MONGO_PASSWORD}
```

2. **Environment Variables:**
- `MONGODB_URI`
- `PAYPAL_CLIENT_ID`
- `PAYPAL_CLIENT_SECRET`

### 📊 Monitoring and Maintenance

- **Health Check**: http://localhost:8081/actuator/health
- **Database Console**: http://localhost:8081/h2-console (H2 only)
- **API Documentation**: Available at /api/products

### 🆘 Troubleshooting

**MongoDB Connection Issues:**
```bash
# Check MongoDB status
brew services list | grep mongodb  # macOS
sudo systemctl status mongod       # Linux
net start MongoDB                  # Windows
```

**Port Already in Use:**
- Change port in `application.properties`: `server.port=8082`

**Build Issues:**
```bash
mvn clean install -U
```

### 🎯 Next Steps

1. **Custom Domain**: Configure your domain name
2. **SSL Certificate**: Add HTTPS support
3. **CDN**: Implement content delivery network
4. **Monitoring**: Add application monitoring
5. **Backup**: Implement database backup strategy

The application is now ready for production deployment! 🚀