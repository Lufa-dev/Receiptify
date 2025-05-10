# Receiptify

Receiptify is a modern recipe management application that allows users to create, save, and share their favorite recipes. Built with Angular 18 for the frontend and Spring Boot 3 for the backend, it offers a seamless experience for cooking enthusiasts.

## Features

- User authentication and profile management
- Create and manage recipes with ingredients, steps, and images
- Organize recipes into collections
- Rate and comment on recipes
- Advanced search functionality with filtering options
- Shopping list generation from recipes
- Seasonality tracking for ingredients
- Admin dashboard for content moderation

## Technology Stack

### Frontend
- Angular 18
- Bootstrap 5
- RxJS
- Angular Material

### Backend
- Spring Boot 3.3
- Spring Security with JWT authentication
- Spring Data JPA
- PostgreSQL database
- MinIO for image storage
- Caffeine for caching

### DevOps
- Docker and Docker Compose for containerization
- Shell scripts for environment management

## Development Setup

### Prerequisites
- Node.js (v20+)
- Java 17
- Docker and Docker Compose
- Git

### Clone the Repository
```bash
git clone https://github.com/Lufa-dev/Receiptify.git
cd Receiptify
```

### Running with Docker (Recommended)
The easiest way to run the application is using Docker Compose:

#### Development Environment
```bash
# Run in development mode
./run-dev.sh
```

#### Production-like Environment
```bash
# Run in production-like mode locally
./run-prod-local.sh
```

### Manual Setup

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm start
```

### Access the Application
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui/index.html
- MinIO Console: http://localhost:9001 (username: minioadmin, password: minioadmin)

## Project Structure

```
receiptify/
├── backend/               # Spring Boot backend
│   ├── src/               # Java source files
│   ├── pom.xml            # Maven configuration
│   └── Dockerfile         # Backend Docker configuration
│
├── frontend/              # Angular frontend
│   ├── src/               # TypeScript source files
│   │   ├── app/           # Application components
│   │   └── shared/        # Shared components, services, and models
│   ├── angular.json       # Angular configuration
│   └── Dockerfile         # Frontend Docker configuration
│
├── docker-compose.yaml    # Docker Compose configuration
└── run-*.sh               # Helper scripts for deployment
```

## Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
