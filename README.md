# Pahana Edu Bookshop Management System

A modern full-stack web application for bookshop management with Java Servlet backend and React frontend.

## Project Structure

```
pahana-edu/
├── backend/                 # Java Servlet REST API
│   ├── src/
│   │   └── main/
│   │       ├── java/com/pahanaedu/
│   │       │   ├── controller/      # REST API endpoints
│   │       │   ├── model/           # Data models
│   │       │   ├── dao/             # Data access layer
│   │       │   ├── service/         # Business logic
│   │       │   └── util/            # Utilities
│   │       ├── resources/           # Configuration files
│   │       └── webapp/WEB-INF/      # Web configuration
│   └── pom.xml                      # Maven dependencies
├── frontend/                # React Vite Application
│   ├── src/
│   │   ├── components/              # Reusable UI components
│   │   ├── pages/                   # Page components
│   │   ├── services/                # API service layer
│   │   ├── utils/                   # Utility functions
│   │   ├── styles/                  # CSS/styling files
│   │   └── App.jsx                  # Main application component
│   └── package.json                 # Frontend dependencies
├── docs/                    # Project documentation
│   ├── api/                         # API documentation
│   ├── setup/                       # Setup guides
│   └── deployment/                  # Deployment guides
├── scripts/                 # Build and deployment scripts
├── docker/                  # Docker configurations (optional)
├── .gitignore              # Git ignore rules
├── package.json            # Monorepo management
└── README.md               # This file
```

## Features

### Customer Management

- User authentication and authorization
- Add, edit, and delete customer accounts
- Unique account number generation
- Customer details management (name, address, phone)

### Item Management

- Add, update, and delete item information
- Item catalog with pricing
- Inventory tracking

### Billing System

- Calculate and generate bills
- Order processing and management
- Bill history and tracking

### User Interface

- Modern React-based web interface
- Responsive design for mobile and desktop
- Real-time data updates
- Intuitive user experience

## Technology Stack

### Frontend

- **React 18** - Modern UI library
- **Vite** - Fast build tool and dev server
- **React Router** - Client-side routing
- **Axios** - HTTP client for API calls
- **Tailwind CSS** - Responsive design

### Backend

- **Java 11+** - Programming language
- **Jakarta EE Servlets** - Web framework
- **Apache Tomcat 10** - Application server
- **Maven** - Build and dependency management
- **Jackson** - JSON processing

### Database

- **MySQL 8.0** - Primary database
- **MySQL Connector/J** - Database driver
- **Connection Pooling** - Optimized database connections

### Development Tools

- **IntelliJ IDEA Ultimate** - IDE
- **GitHub Copilot** - AI-powered coding assistant
- **Git** - Version control
- **npm/Node.js** - Package management

## Prerequisites

- **Java 11 or higher**
- **Node.js 18+ and npm**
- **Apache Tomcat 10.x**
- **MySQL 8.0+**
- **IntelliJ IDEA**
- **Git**

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/pahana-edu.git
cd pahana-edu
```

### 2. Install Dependencies

```bash
# Install all dependencies (root, frontend, backend)
npm run install:all
```

### 3. Database Setup

```sql
-- Create database
CREATE DATABASE pahana_edu_db;

-- Create user
CREATE USER 'pahana_user'@'localhost' IDENTIFIED BY 'pahana_pass123';
GRANT ALL PRIVILEGES ON pahana_edu_db.* TO 'pahana_user'@'localhost';
FLUSH PRIVILEGES;

-- Run schema (execute backend/src/main/resources/schema.sql)
```

### 4. Configure Environment

```bash
# Copy environment template
cp .env.example .env.local

# Update database credentials in .env.local
```

### 5. Start Development Servers

```bash
# Start both frontend and backend servers
npm run dev
```

The application will be available at:

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api

## Available Scripts

### Monorepo Management

```bash
npm run dev              # Start both frontend and backend servers
npm run build            # Build entire project for production
npm run install:all      # Install dependencies for all projects
npm run clean            # Clean all build artifacts and node_modules
```

### Individual Project Scripts

```bash
npm run dev:frontend     # Start only frontend dev server
npm run dev:backend      # Start only backend server
npm run build:frontend   # Build frontend for production
npm run build:backend    # Build backend WAR file
```

### Testing

```bash
npm run test:frontend    # Run frontend tests
npm run test:backend     # Run backend tests
npm run test:all         # Run all tests
```

## Development Workflow

### Branch Strategy

- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - Feature development
- `hotfix/*` - Urgent fixes
- `release/*` - Release preparation

### Commit Conventions

Use conventional commits with scope:

```bash
feat: add customer login component
fix: resolve database connection issue
docs: update API documentation
chore: update React dependencies
```

## API Documentation

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/profile` - Get user profile

### Customer Management

- `GET /api/customers` - Get all customers
- `POST /api/customers` - Create new customer
- `GET /api/customers/:id` - Get customer by ID
- `PUT /api/customers/:id` - Update customer
- `DELETE /api/customers/:id` - Delete customer

### Item Management

- `GET /api/items` - Get all items
- `POST /api/items` - Create new item
- `GET /api/items/:id` - Get item by ID
- `PUT /api/items/:id` - Update item
- `DELETE /api/items/:id` - Delete item

### Order Management

- `GET /api/orders` - Get all orders
- `POST /api/orders` - Create new order
- `GET /api/orders/:id` - Get order by ID
- `PUT /api/orders/:id` - Update order
- `DELETE /api/orders/:id` - Delete order

## Testing

### Frontend Testing

```bash
cd frontend
npm run test        # Run unit tests
npm run test:e2e    # Run end-to-end tests
```

### Backend Testing

```bash
cd backend
mvn test           # Run unit tests
mvn integration-test # Run integration tests
```

## Deployment

### Development Environment

```bash
npm run dev
```

### Production Build

```bash
npm run build
```

This creates:

- `frontend/dist/` - Static frontend files
- `backend/target/*.war` - Backend WAR file

### Docker Deployment (Optional)

```bash
docker-compose up -d
```

---
