// MongoDB initialization script
db = db.getSiblingDB('techmarketpro');

// Create collections
db.createCollection('products');

// Insert initial data
db.products.insertMany([
  {
    name: "Premium Headphones",
    price: NumberDecimal("99.99"),
    description: "High-quality wireless headphones with noise cancellation",
    imageUrl: "https://via.placeholder.com/300x200",
    paypalButtonId: "962SY9YFS2WD4"
  },
  {
    name: "Smart Watch",
    price: NumberDecimal("199.99"),
    description: "Feature-rich smartwatch with health monitoring",
    imageUrl: "https://via.placeholder.com/300x200",
    paypalButtonId: "962SY9YFS2WD4"
  },
  {
    name: "Laptop Bag",
    price: NumberDecimal("49.99"),
    description: "Durable and stylish laptop bag for professionals",
    imageUrl: "https://via.placeholder.com/300x200",
    paypalButtonId: "962SY9YFS2WD4"
  },
  {
    name: "Wireless Mouse",
    price: NumberDecimal("29.99"),
    description: "Ergonomic wireless mouse with long battery life",
    imageUrl: "https://via.placeholder.com/300x200",
    paypalButtonId: "962SY9YFS2WD4"
  }
]);

// Create indexes for better performance
db.products.createIndex({ name: "text", description: "text" });
db.products.createIndex({ price: 1 });

print("Database initialized successfully!");