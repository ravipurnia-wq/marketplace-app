# 🛒 TechMarket Pro - Complete E-commerce Platform

A modern, full-featured e-commerce platform built with Spring Boot, offering both traditional web interface and REST API capabilities.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.0-green)
![Database](https://img.shields.io/badge/Database-H2%20%7C%20MongoDB-blue)
![PayPal](https://img.shields.io/badge/Payment-PayPal%20Integration-lightblue)

## ✨ Features

### 🖥️ Complete Website
- **Modern UI/UX**: Professional gradient design with animations
- **Responsive Design**: Optimized for desktop, tablet, and mobile
- **Multi-page Navigation**: Home, Products, About, Contact, Admin
- **Interactive Elements**: Smooth scrolling, hover effects, loading animations

### 🔌 REST API
- **Full CRUD Operations**: Create, Read, Update, Delete products
- **PayPal Integration**: Secure payment processing
- **JSON Responses**: Standard REST API with proper status codes
- **CORS Support**: Cross-origin resource sharing enabled

### 💾 Database Flexibility
- **H2 Database**: Default in-memory database (no setup required)
- **MongoDB Support**: Production-ready NoSQL database option
- **Automatic Switching**: Configure via properties
- **Data Initialization**: Sample products loaded automatically

### 🎯 Key Pages
- **Home**: Product showcase with hero section
- **Products**: Advanced filtering, sorting, and search
- **About**: Company information and mission
- **Contact**: Contact form with validation
- **Admin**: Product management interface

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Optional: MongoDB for production

### Run Locally (H2 Database)
```bash
git clone <your-repository>
cd marketplace-app
mvn spring-boot:run
```

Access the application:
- **Website**: http://localhost:8081
- **REST API**: http://localhost:8081/api/products
- **Admin Panel**: http://localhost:8081/admin
- **Database Console**: http://localhost:8081/h2-console

### Run with MongoDB
1. Install and start MongoDB
2. Update `application.properties`:
```properties
app.database.type=mongodb
```
3. Restart the application

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    TechMarket Pro                           │
├─────────────────────────────────────────────────────────────┤
│  Frontend (Thymeleaf + CSS/JS)    │   REST API (Spring)    │
├─────────────────────────────────────────────────────────────┤
│              Service Layer (Business Logic)                 │
├─────────────────────────────────────────────────────────────┤
│  Repository Layer  │  JPA/Hibernate  │  MongoDB            │
├─────────────────────────────────────────────────────────────┤
│     H2 Database    │                 │  MongoDB Atlas      │
└─────────────────────────────────────────────────────────────┘
```

## 📱 Screenshots & Features

### Home Page
- Hero section with gradient background
- Featured products grid
- Animated product cards
- PayPal integration buttons

### Admin Panel
- Add/Edit/Delete products
- Real-time form validation
- Database statistics
- REST API testing

### Products Page
- Advanced search and filtering
- Price range filtering
- Multiple sorting options
- Responsive product grid

## 🔧 Configuration

### Database Configuration
```properties
# Use H2 (Default)
app.database.type=h2

# Use MongoDB
app.database.type=mongodb
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=techmarketpro
```

### Production Configuration
```properties
spring.profiles.active=prod
server.port=${PORT:8081}
spring.jpa.show-sql=false
spring.h2.console.enabled=false
```

## 🐳 Docker Deployment

### Quick Start with Docker Compose
```bash
# Start with MongoDB
docker-compose up -d

# Start with H2 only
docker run -p 8081:8081 techmarket-app
```

### Build Docker Image
```bash
docker build -t techmarket-app .
```

## 🌐 Cloud Deployment

### Heroku
```bash
heroku create your-app-name
heroku addons:create mongolab:sandbox
git push heroku main
```

### AWS/Azure/GCP
- Use the provided Dockerfile
- Configure environment variables
- Set up load balancer and SSL

## 📊 API Documentation

### Products API
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Payment API
- `POST /api/payments/paypal` - Process PayPal payment

### Example API Usage
```bash
# Get all products
curl http://localhost:8081/api/products

# Create new product
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Product",
    "price": 99.99,
    "description": "Amazing new product",
    "imageUrl": "https://example.com/image.jpg",
    "paypalButtonId": "YOUR_BUTTON_ID"
  }'
```

## 🔒 Security Features

- **Input Validation**: Bean validation on all forms
- **CORS Protection**: Configurable cross-origin support
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **XSS Protection**: Thymeleaf template escaping
- **Production Headers**: Security headers in Nginx config

## 🧪 Testing

### Manual Testing
1. **Website Navigation**: Test all pages and links
2. **Product Management**: Add, edit, delete products via admin
3. **API Testing**: Use curl or Postman to test endpoints
4. **Payment Flow**: Test PayPal integration (sandbox)

### Automated Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## 📈 Performance Optimizations

- **Static Resource Caching**: 1-year cache for CSS/JS/images
- **Gzip Compression**: Enabled for text content
- **Database Indexing**: Optimized queries
- **Connection Pooling**: HikariCP for database connections
- **Lazy Loading**: JPA lazy loading for relationships

## 🛠️ Development

### Project Structure
```
src/
├── main/
│   ├── java/com/marketplace/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # Web and REST controllers
│   │   ├── model/          # Entity models
│   │   ├── repository/     # Data access layer
│   │   └── service/        # Business logic
│   └── resources/
│       ├── static/         # CSS, JS, images
│       ├── templates/      # Thymeleaf templates
│       └── application.properties
└── test/                   # Test files
```

### Adding New Features
1. Create model classes
2. Add repository interfaces
3. Implement service layer
4. Create REST controllers
5. Add web pages (optional)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

- **Documentation**: Check the DEPLOYMENT_GUIDE.md
- **Issues**: Create an issue on GitHub
- **Email**: contact@techmarketpro.com

## 🎯 Roadmap

- [ ] User authentication and authorization
- [ ] Shopping cart functionality
- [ ] Order management system
- [ ] Email notifications
- [ ] Advanced analytics dashboard
- [ ] Mobile app (React Native)
- [ ] Multi-vendor support

---

Built with ❤️ using Spring Boot | © 2024 TechMarket Pro