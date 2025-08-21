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
│   ├── public/                      # Static assets
│   ├── src/
│   │   ├── components/              # Reusable UI components
│   │   ├── pages/                   # Page components
│   │   ├── layouts/                 # Layout components
│   │   ├── services/                # API service layer
│   │   ├── utils/                   # Utility functions
│   │   ├── hooks/                   # Custom React hooks
│   │   ├── lib/                     # Third-party libraries and configs
│   │   └── App.jsx                  # Main application component
│   └── package.json                 # Frontend dependencies
├── docs/                    # Project documentation
├── docker/                  # Docker configurations
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

### Help Section

- User documentation and guides
- FAQ and troubleshooting
- System administration support

### User Interface

- Modern React-based web interface
- Responsive design for mobile and desktop
- Real-time data updates
- Intuitive user experience

## Technology Stack

### Frontend

- **React 19** - Modern UI library
- **Vite** - Fast build tool and dev server
- **React Router** - Client-side routing
- **Axios** - HTTP client for API calls
- **Tailwind CSS** - Responsive design
- **Sonner** - Toast notification system

### Backend

- **Java 11+** - Programming language
- **Java EE Servlets** - Web framework
- **Apache Tomcat 7** - Application server
- **Maven** - Build and dependency management
- **Gson** - JSON processing
- **JUnit** - Testing framework

### Database

- **MySQL 8.0** - Primary database
- **MySQL Connector/J** - Database driver
- **Connection Pooling** - Optimized database connections

### Development Tools

- **IntelliJ IDEA** - IDE
- **GitHub Copilot** - AI-powered coding assistant
- **Git** - Version control
- **Maven & npm** - Package management

## Prerequisites

- **Java 11 or higher**
- **Node.js 18+ and npm**
- **Apache Tomcat 10.x**
- **MySQL 8.0+**

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/DevAnuhas/pahana-edu.git
cd pahana-edu
```

### 2. Install Dependencies

#### Frontend
```bash
cd frontend
npm install
```

#### Backend
```bash
cd backend
mvn clean install
```

### 3. Configure Database Connection

Edit the [application.properties](backend/src/main/resources/application.properties) file with your database credentials:

```properties
app.datasource.url=jdbc:mysql://localhost:3306/pahana_bookshop?createDatabaseIfNotExist=true&allowMultiQueries=true
app.datasource.username=root
app.datasource.password=root1234
```

### 4. Database Setup

The database and tables will be automatically created at application startup through the [schema.sql](backend/src/main/resources/schema.sql) file. 

### 5. Start Development Servers

#### Frontend
```bash
cd frontend
npm run dev
```

#### Backend
```bash
cd backend
mvn tomcat:run
```

The application will be available at:

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api


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

For detailed API documentation, please refer to the [API Documentation](/docs/api-documentation.md) file.

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
mvn test             # Run unit tests
mvn integration-test # Run integration tests
```

## Deployment

### Production Build

#### Frontend
```bash
cd frontend
npm run build
```

#### Backend
```bash
cd backend
mvn package
```

This creates:

- `frontend/dist/` - Static frontend files
- `backend/target/*.war` - Backend WAR file

### Docker Deployment (Optional)

```bash
docker-compose up -d
```

---
