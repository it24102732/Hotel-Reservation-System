<div align="center">

# 🏨 Hotel Reservation System

[![GitHub repo size](https://img.shields.io/github/repo-size/it24102732/Hotel-Reservation-System?style=for-the-badge&color=FF6B6B)](https://github.com/it24102732/Hotel-Reservation-System)
[![GitHub stars](https://img.shields.io/github/stars/it24102732/Hotel-Reservation-System?style=for-the-badge&color=FFD93D)](https://github.com/it24102732/Hotel-Reservation-System/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/it24102732/Hotel-Reservation-System?style=for-the-badge&color=6BCB77)](https://github.com/it24102732/Hotel-Reservation-System/network/members)
[![GitHub issues](https://img.shields.io/github/issues/it24102732/Hotel-Reservation-System?style=for-the-badge&color=4D96FF)](https://github.com/it24102732/Hotel-Reservation-System/issues)
[![License](https://img.shields.io/badge/License-Academic-blue?style=for-the-badge)](LICENSE)

<p align="center">
  <b>A modern, full-stack hotel reservation system built with Spring Boot & Thymeleaf</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" />
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white" />
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" />
</p>

---

### 📖 Overview

A comprehensive **Year 2 Semester 1 Software Engineering** project demonstrating modern web application development practices. This system streamlines hotel operations and enhances the guest booking experience through an intuitive interface and robust backend infrastructure.

[Quick Start](#-quick-start) • [Features](#-key-features) • [Tech Stack](#-technology-stack) • [Project Structure](#-project-structure) • [Installation](#-installation-guide) • [API Documentation](#-api-endpoints) • [Contributing](#-contributing)

</div>

---

## 🎯 About This Project

The **Hotel Reservation System** is a full-stack web application that demonstrates core concepts of software engineering including:
- ✅ Object-oriented design principles
- ✅ MVC architecture pattern
- ✅ Database design and optimization
- ✅ RESTful API principles
- ✅ User authentication & authorization
- ✅ Form validation & error handling
- ✅ Email notifications
- ✅ PDF report generation

<details>
<summary><b>📋 Expand to see project learning objectives</b></summary>

**Learning Outcomes:**
- Apply software engineering best practices in a real-world scenario
- Implement a complete web application lifecycle
- Work with relational databases and ORM frameworks
- Handle user authentication and session management
- Integrate third-party services (email, PDF generation)
- Write maintainable and scalable code
- Implement business logic for a multi-user system

</details>

---

## ✨ Key Features

### 👥 **Customer Features**

| Feature | Description |
|---------|-------------|
| 🔐 **User Authentication** | Secure registration, login, and password recovery with email verification |
| 👤 **Profile Management** | Update personal information, manage preferences, view booking history |
| 🔍 **Advanced Search** | Filter rooms by date range, type, price range, and amenities |
| 📅 **Real-time Availability** | Live room availability checking with interactive calendar |
| 🛏️ **Easy Booking** | Simple 3-step booking process with instant confirmation |
| 📧 **Email Notifications** | Booking confirmations, reminders, and cancellation updates |
| 💳 **Secure Payments** | Integrated payment processing with transaction history |
| 📄 **Invoice Generation** | PDF download of booking receipts and invoices |
| ✏️ **Booking Management** | Modify, view, or cancel bookings with flexible policies |

### 👨‍💼 **Administrator Features**

| Feature | Description |
|---------|-------------|
| 📊 **Dashboard** | Real-time overview of bookings, revenue, and occupancy metrics |
| 🏨 **Room Management** | Add, edit, delete rooms with pricing and availability control |
| 👥 **User Management** | View customer profiles, manage access, and monitor activity |
| 📈 **Analytics & Reports** | Revenue reports, occupancy trends, and customer insights |
| 💰 **Payment Management** | Track transactions, refunds, and financial summaries |
| 🎛️ **System Configuration** | Manage room types, amenities, and booking policies |

---

## 🛠️ Technology Stack

### **Backend**
| Component | Technology | Version |
|-----------|-----------|---------|
| 🔥 Runtime | **Java** | **21 LTS** |
| 🚀 Framework | **Spring Boot** | **3.5.6** |
| 💾 ORM | **Spring Data JPA** | Included |
| 🗄️ Database | **MySQL** | 5.7+ |
| 🔐 Security | **Spring Security** | Included |
| 📧 Email | **Spring Mail** | Included |

### **Frontend**
| Component | Technology | Purpose |
|-----------|-----------|---------|
| 🖇️ Template Engine | **Thymeleaf** | Server-side template rendering |
| 🎨 Markup | **HTML5** | Semantic structure |
| 💅 Styling | **CSS3** | Responsive design & animations |
| ⚙️ Interactivity | **JavaScript (ES6+)** | Client-side logic & AJAX calls |

### **Additional Libraries**
| Library | Purpose |
|---------|---------|
| **Validation** | JSR-380 Bean Validation |
| **PDF Generation** | OpenHTML to PDF (openhtmltopdf-pdfbox 1.0.10) |
| **DevTools** | Live reload during development |
| **Actuator** | Application monitoring & health checks |

---

## 🚀 Quick Start

### Prerequisites
```bash
✓ Java Development Kit (JDK) 21 or higher
✓ Maven 3.6.0 or higher
✓ MySQL 5.7 or higher
✓ Git
```

### Installation Guide

#### **Step 1: Clone the Repository**
```bash
git clone https://github.com/it24102732/Hotel-Reservation-System.git
cd Hotel-Reservation-System
```

#### **Step 2: Database Setup**

Create the database and user:
```sql
CREATE DATABASE hotel_reservation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'hotel_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON hotel_reservation_db.* TO 'hotel_user'@'localhost';
FLUSH PRIVILEGES;
```

#### **Step 3: Configure Application Properties**

Create `src/main/resources/application.properties`:
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_reservation_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=hotel_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Application Configuration
spring.application.name=Hotel Reservation System
spring.profiles.active=dev

# Mail Configuration (for email notifications)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Logging
logging.level.root=INFO
logging.level.com.hotelmanagement=DEBUG
```

#### **Step 4: Build & Run**
```bash
# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run

# Alternative: Run JAR file
java -jar target/system-0.0.1-SNAPSHOT.jar
```

#### **Step 5: Access the Application**
```
🌐 Homepage: http://localhost:8080
👤 Login: http://localhost:8080/login
👨‍💼 Admin Panel: http://localhost:8080/admin (admin credentials)
```

---

## 📁 Project Structure

```
Hotel-Reservation-System/
│
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/com/hotelmanagement/
│   │   │   ├── 📂 controllers/       # Spring MVC Controllers
│   │   │   │   ├── BookingController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── RoomController.java
│   │   │   │   └── AdminController.java
│   │   │   │
│   │   │   ├── 📂 models/            # JPA Entity Classes
│   │   │   │   ├── User.java
│   │   │   │   ├── Room.java
│   │   │   │   ├── Booking.java
│   │   │   │   └── Payment.java
│   │   │   │
│   │   │   ├── 📂 repository/        # Spring Data JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoomRepository.java
│   │   │   │   └── BookingRepository.java
│   │   │   │
│   │   │   ├── 📂 service/           # Business Logic Layer
│   │   │   │   ├── BookingService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── PaymentService.java
│   │   │   │
│   │   │   ├── 📂 dto/               # Data Transfer Objects
│   │   │   │   ├── BookingDTO.java
│   │   │   │   └── UserDTO.java
│   │   │   │
│   │   │   ├── 📂 config/            # Spring Configurations
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   │
│   │   │   ├── 📂 exception/         # Custom Exceptions
│   │   │   │   ├── BookingException.java
│   │   │   │   └── ValidationException.java
│   │   │   │
│   │   │   └── 📄 HotelManagementApplication.java
│   │   │
│   │   ├── 📂 resources/
│   │   │   ├── 📄 application.properties
│   │   │   ├── 📄 application-dev.properties
│   │   │   ├── 📂 templates/         # Thymeleaf Templates
│   │   │   │   ├── index.html
│   │   │   │   ├── login.html
│   │   │   │   ├── booking.html
│   │   │   │   ├── admin/
│   │   │   │   │   ├── dashboard.html
│   │   │   │   │   └── room-manage.html
│   │   │   │   └── fragments/
│   │   │   │       ├── header.html
│   │   │   │       └── footer.html
│   │   │   │
│   │   │   ├── 📂 static/
│   │   │   │   ├── 📂 css/
│   │   │   │   │   ├── style.css
│   │   │   │   │   └── responsive.css
│   │   │   │   ├── 📂 js/
│   │   │   │   │   ├── app.js
│   │   │   │   │   └── booking.js
│   │   │   │   └── 📂 images/
│   │   │   │
│   │   │   └── 📂 data/
│   │   │       └── schema.sql
│   │   │
│   │   └── 📂 webapp/
│   │       └── 📂 WEB-INF/
│   │
│   └── 📂 test/                    # Unit & Integration Tests
│       └── 📂 java/com/hotelmanagement/
│           ├── BookingServiceTest.java
│           └── UserServiceTest.java
│
├── 📂 database/                    # Database Scripts
│   ├── 📄 schema.sql               # Initial schema
│   └── 📄 init-data.sql            # Sample data
│
├── 📄 pom.xml                      # Maven Configuration
├── 📄 README.md                    # This file
├── 📄 .gitignore                   # Git ignore rules
└── 📄 LICENSE                      # Academic License

```

---

## 💻 Usage Guide

### **For Customers**

#### 1. Register an Account
```
1. Navigate to http://localhost:8080
2. Click "Sign Up" button
3. Fill in email, password, and personal details
4. Verify email (check inbox)
5. You're ready to book!
```

#### 2. Search Available Rooms
```
1. Go to "Search Rooms"
2. Enter check-in and check-out dates
3. Select room type and price range
4. Click "Search"
5. View available options
```

#### 3. Make a Reservation
```
1. Click on desired room
2. Review room details and amenities
3. Fill in guest information
4. Select payment method
5. Confirm booking
6. Receive confirmation email
```

#### 4. Manage Your Bookings
```
1. Login to your account
2. Go to "My Bookings"
3. View booking details
4. Modify dates (if available)
5. Cancel booking (if applicable)
6. Download invoice as PDF
```

### **For Administrators**

#### 1. Access Admin Dashboard
```
Login → Admin Panel → Dashboard
```

#### 2. Manage Rooms
```
Admin Panel → Room Management:
  • Add new room (type, capacity, price)
  • Set availability calendar
  • Configure amenities
  • Update pricing policies
```

#### 3. View Analytics
```
Admin Panel → Reports:
  • Occupancy rate
  • Revenue analytics
  • Booking trends
  • Guest insights
```

#### 4. Manage Bookings
```
Admin Panel → Bookings:
  • View all reservations
  • Modify booking status
  • Process refunds
  • Generate reports
```

---

## 🔐 API Endpoints

### **Authentication**
```
POST   /api/auth/register          # Register new user
POST   /api/auth/login             # User login
POST   /api/auth/logout            # Logout user
POST   /api/auth/refresh-token     # Refresh JWT token
```

### **Rooms**
```
GET    /api/rooms                  # Get all rooms
GET    /api/rooms/{id}             # Get room details
GET    /api/rooms/search           # Search available rooms
POST   /api/rooms                  # Create room (Admin only)
PUT    /api/rooms/{id}             # Update room (Admin only)
DELETE /api/rooms/{id}             # Delete room (Admin only)
```

### **Bookings**
```
GET    /api/bookings               # Get user's bookings
GET    /api/bookings/{id}          # Get booking details
POST   /api/bookings               # Create new booking
PUT    /api/bookings/{id}          # Modify booking
DELETE /api/bookings/{id}          # Cancel booking
GET    /api/bookings/{id}/invoice  # Download invoice
```

### **Users**
```
GET    /api/users/profile          # Get user profile
PUT    /api/users/profile          # Update profile
GET    /api/users/bookings         # Get user bookings
POST   /api/users/password         # Change password
```

### **Admin**
```
GET    /api/admin/dashboard        # Dashboard metrics
GET    /api/admin/reports          # Booking & revenue reports
POST   /api/admin/rooms            # Manage rooms
GET    /api/admin/users            # List all users
```

---

## 🧪 Testing

### Running Unit Tests
```bash
mvn test
```

### Running Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

---

## 🔄 Build & Deployment

### Development Build
```bash
mvn clean install -DskipTests
```

### Production Build
```bash
mvn clean install -Pproduction
```

### Create Docker Image
```bash
docker build -t hotel-reservation-system .
docker run -p 8080:8080 hotel-reservation-system
```

---

## 🐛 Troubleshooting

### Common Issues

**Issue:** Cannot connect to MySQL
```
Solution: 
1. Verify MySQL is running: systemctl status mysql
2. Check credentials in application.properties
3. Ensure database exists: CREATE DATABASE hotel_reservation_db;
```

**Issue:** Port 8080 already in use
```
Solution:
1. Change port in application.properties: server.port=8081
2. Or kill process: lsof -ti :8080 | xargs kill -9
```

**Issue:** Email notifications not sending
```
Solution:
1. Enable "Less secure app access" for Gmail
2. Use App Passwords for Gmail accounts
3. Verify SMTP settings in configuration
```

---

## 📦 Dependencies Overview

| Dependency | Purpose | Version |
|-----------|---------|---------|
| spring-boot-starter-data-jpa | Database ORM | Latest |
| spring-boot-starter-web | Web MVC framework | Latest |
| spring-boot-starter-thymeleaf | Template engine | Latest |
| spring-boot-starter-mail | Email sending | Latest |
| spring-boot-starter-validation | Form validation | Latest |
| spring-boot-starter-actuator | Monitoring | Latest |
| mysql-connector-j | MySQL driver | Latest |
| openhtmltopdf-pdfbox | PDF generation | 1.0.10 |

---

## 🗺️ Development Roadmap

### ✅ Completed
- [x] Basic CRUD operations
- [x] User authentication
- [x] Room management
- [x] Booking system
- [x] Admin dashboard
- [x] Email notifications
- [x] PDF invoice generation

### 🔄 In Progress
- [ ] Payment gateway integration (Stripe/PayPal)
- [ ] SMS notifications
- [ ] Multi-language support
- [ ] Advanced search filters

### 📋 Future Enhancements
- [ ] Mobile application (React Native)
- [ ] Loyalty program
- [ ] AI-powered price optimization
- [ ] Real-time chat support
- [ ] Review & rating system
- [ ] Social media integration

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

### 1. Fork the Repository
```bash
Click "Fork" button on GitHub
```

### 2. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### 3. Make Your Changes
```bash
git add .
git commit -m "Add meaningful commit message"
```

### 4. Push to Your Branch
```bash
git push origin feature/your-feature-name
```

### 5. Open a Pull Request
- Describe your changes clearly
- Reference any related issues
- Ensure all tests pass

---

## 📝 Code Style Guidelines

### Java Conventions
- Use camelCase for variables and methods
- Use UPPER_CASE for constants
- Write meaningful variable names
- Add Javadoc for public methods
- Keep methods focused and concise

### Naming Conventions
```java
// Controllers
class UserController { }

// Services
class UserService { }

// Repositories
interface UserRepository { }

// Models
class User { }

// DTOs
class UserDTO { }
```

---

## 📄 License

This project is part of a **Year 2 Semester 1 Software Engineering** academic program.

```
Academic Project License
Usage restricted to educational and academic purposes.
See LICENSE file for more details.
```

---

## 👨‍💻 Author & Contributors

<div align="center">

### **Developer**
**IT24102732**

[![GitHub Profile](https://img.shields.io/badge/GitHub-Profile-black?style=for-the-badge&logo=github)](https://github.com/it24102732)

### **Contributions**
Contributions from the software engineering community are welcome!

</div>

---

## 🙏 Acknowledgments

- 🎓 University Faculty & Supervisors
- 📚 Spring Boot & Java Community
- 🤝 Open Source Contributors
- 💡 Peer Reviewers & Testers

---

## 📧 Support & Contact

For questions or issues:
- 📝 [Open an Issue](https://github.com/it24102732/Hotel-Reservation-System/issues)
- 💬 [Start a Discussion](https://github.com/it24102732/Hotel-Reservation-System/discussions)
- 📧 Email: it24102732@example.com

---

## 📊 Repository Statistics

![Project Statistics](https://github-profile-trophy.vercel.app/?username=it24102732&theme=nord&column=3)

<div align="center">

---

### ⭐ **If this project helped you, please give it a star!**

**Made with ❤️ for Software Engineering Education**

![Language](https://img.shields.io/github/languages/top/it24102732/Hotel-Reservation-System?style=flat-square)
![Last Commit](https://img.shields.io/github/last-commit/it24102732/Hotel-Reservation-System?style=flat-square)
![Repo Size](https://img.shields.io/github/repo-size/it24102732/Hotel-Reservation-System?style=flat-square)

</div>
