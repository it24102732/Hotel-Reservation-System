# 🏨 Hotel Reservation System

<div align="center">

![Typing SVG](https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&duration=3000&pause=1500&color=FF6B6B&center=true&vCenter=true&width=700&lines=Welcome+to+Hotel+Reservation+System;Book+Your+Perfect+Stay+in+Seconds)

[![GitHub repo size](https://img.shields.io/github/repo-size/it24102732/Hotel-Reservation-System?style=for-the-badge&color=FF6B6B)](https://github.com/it24102732/Hotel-Reservation-System)
[![Stars](https://img.shields.io/github/stars/it24102732/Hotel-Reservation-System?style=for-the-badge&color=FFD93D)](https://github.com/it24102732/Hotel-Reservation-System/stargazers)
[![Forks](https://img.shields.io/github/forks/it24102732/Hotel-Reservation-System?style=for-the-badge&color=6BCB77)](https://github.com/it24102732/Hotel-Reservation-System/network)
[![Issues](https://img.shields.io/github/issues/it24102732/Hotel-Reservation-System?style=for-the-badge&color=4D96FF)](https://github.com/it24102732/Hotel-Reservation-System/issues)

</div>

---

A modern full-stack hotel reservation system built for a Year 2 Software Engineering project. Backend is powered by Spring Boot (Java 21) with Thymeleaf templates for server-side rendering. This README includes animated elements and clear steps to set up, run, and demo the application.

Animated demo (replace with your own recording/gif):
<p align="center">
  <img src="https://media.giphy.com/media/3o7btNhMBytxAM6YBa/giphy.gif" alt="demo-gif" width="800" />
</p>

Tip: Replace the GIF above with a short recorded walkthrough of your app (3–10s) to make the README more engaging.

Table of contents
- Project overview
- Key features (customer & admin)
- Animated elements included in this README
- Tech stack
- Quick start
- Configuration
- Project structure
- API overview
- How to add your own animation / GIF / demo
- Contributing
- License & credits

---

## 🎯 Project overview

This project demonstrates a fully functional hotel reservation system with:
- User registration & authentication
- Room search and availability checking
- Booking flow with booking history
- Administrative dashboard for room and booking management
- Email notifications and PDF invoice generation (OpenHTMLToPDF)

---

## ✨ Key features

For customers:
- Registration / login / profile
- Search rooms by date/type/price
- Book, modify, cancel bookings
- Receive booking confirmation emails
- Download invoice (PDF)

For administrators:
- Dashboard with occupancy and revenue insight
- Manage rooms (add/edit/delete)
- View and manage bookings
- Generate reports and invoices

---

## 🔮 Animated elements added to README

1. Header typing animation (readme-typing-svg) — gives a dynamic first impression.
2. Animated demo GIF — demonstrates the application in action.
3. Animated badges and shields — visually call out stack & repository metrics.
4. Guidance for adding your own animated SVG or Lottie (GIF fallback recommended).

Why these choices?
- GitHub strips most JavaScript from READMEs so GIFs/SVGs/badges are reliable, cross-platform animations.
- Typing SVG is an external service and widely used for dynamic headers.
- GIFs are simple to create and replace; they show a real app in motion.

---

## 🛠️ Technology stack

- Java 21
- Spring Boot 3.5.6
- Spring Data JPA (Hibernate)
- Thymeleaf
- MySQL (Connector/J)
- OpenHTMLToPDF (pdf generation)
- Spring Mail (email)
- Maven

See pom.xml for exact dependencies.

---

## 🚀 Quick start

Prerequisites:
- Java 21+
- Maven 3.6+
- MySQL
- Git

Steps:
1. Clone
   git clone https://github.com/it24102732/Hotel-Reservation-System.git
   cd Hotel-Reservation-System

2. Create database
   CREATE DATABASE hotel_reservation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

3. Create and edit application.properties (see Configuration section below).

4. Build & run
   mvn clean install
   mvn spring-boot:run
   or
   java -jar target/system-0.0.1-SNAPSHOT.jar

5. Open http://localhost:8080

---

## ⚙️ Configuration (application.properties template)

Create src/main/resources/application.properties (example):

```
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_reservation_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=hotel_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Notes:
- For production, never store credentials in plain properties; use environment variables or a secrets manager.
- For Gmail SMTP, prefer App Passwords instead of enabling less secure apps.

---

## 📁 Project structure (high-level)

- src/main/java/com/hotelmanagement
  - controllers/  (MVC controllers)
  - models/       (JPA entities)
  - repository/   (Spring Data JPA repos)
  - service/      (business logic)
  - config/       (security & web config)
- src/main/resources
  - templates/    (Thymeleaf)
  - static/       (css, js, images)
  - application.properties

See the repository for exact filenames.

---

## 🔌 API overview (common endpoints)

Authentication
- POST /api/auth/register
- POST /api/auth/login

Rooms
- GET /api/rooms
- GET /api/rooms/search
- POST /api/admin/rooms (admin)

Bookings
- POST /api/bookings
- GET /api/bookings/{id}
- GET /api/users/{id}/bookings

Admin
- GET /api/admin/dashboard
- GET /api/admin/reports

---

## 🎞️ How to add your own animation or demo GIF

1. Record a short walkthrough (3–10 seconds):
   - macOS: QuickTime -> File → New Screen Recording → export to GIF via GIF Brewery or use ffmpeg.
   - Windows: Xbox Game Bar or OBS -> export -> convert to GIF with ffmpeg.
   - Linux: Peek or OBS -> export to GIF.

2. Optimize the GIF (keep file size < 2 MB for quick loading):
   - ffmpeg example:
     ffmpeg -i demo.mp4 -vf "fps=12,scale=800:-1:flags=lanczos" -lossless 1 demo.gif

3. Add the GIF to the repo:
   - Place in docs/demo.gif or assets/demo.gif
   - Commit & push.

4. Update README image link:
   <img src="docs/demo.gif" alt="demo" width="800" />

Alternative: host the GIF externally (Imgur, GitHub Releases, raw GitHub URL) and replace the URL.

Adding an animated SVG banner
- Inline SVG with SMIL/CSS animation works if added as an image (data URI or raw file). Test on GitHub — some SVG features may be blocked.

Lottie animations
- Lottie (JSON) doesn't run on GitHub READMEs. Export Lottie to GIF or an animated SVG fallback.

---

## 🧪 Tests

Run:
- mvn test

For coverage:
- mvn jacoco:report

---

## 🤝 Contributing

1. Fork the repo
2. Create a branch: git checkout -b feature/your-feature
3. Commit changes: git commit -m "Add feature"
4. Push & open a pull request

Please follow standard Java code style and include unit tests for new logic.

---

## 📜 License & credits

This project was created as a Year 2 Semester 1 Software Engineering academic project. See LICENSE for details (academic use).

---

## 🙏 Acknowledgements

Thanks to faculty, teammates, and the open-source community. If you like this project, please star ⭐ the repo.

---

If you want, I can:
- produce a short 6–8s demo GIF from a screen recording if you upload a short mp4,
- or push this README update directly to your repo on a new branch.
