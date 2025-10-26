# 🏨 Hotel Reservation System

<div align="center">

![Hero](https://capsule-render.vercel.app/api?type=waving&color=FF6B6B&height=150&section=header&text=Hotel%20Reservation%20System&fontSize=40&animation=twinkling)

![Typing SVG](https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&duration=3200&pause=1300&color=FFFFFF&center=true&width=820&lines=Welcome+to+Hotel+Reservation+System;Book+Your+Perfect+Stay+in+Seconds)

<p align="center">
  <a href="https://github.com/it24102732/Hotel-Reservation-System"><img alt="repo size" src="https://img.shields.io/github/repo-size/it24102732/Hotel-Reservation-System?style=for-the-badge&color=FF6B6B"></a>
  <a href="https://github.com/it24102732/Hotel-Reservation-System/stargazers"><img alt="stars" src="https://img.shields.io/github/stars/it24102732/Hotel-Reservation-System?style=for-the-badge&color=FFD93D"></a>
  <a href="https://github.com/it24102732/Hotel-Reservation-System/network"><img alt="forks" src="https://img.shields.io/github/forks/it24102732/Hotel-Reservation-System?style=for-the-badge&color=6BCB77"></a>
  <a href="https://github.com/it24102732/Hotel-Reservation-System/issues"><img alt="issues" src="https://img.shields.io/github/issues/it24102732/Hotel-Reservation-System?style=for-the-badge&color=4D96FF"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk" /> 
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=flat-square&logo=springboot" /> 
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql" />
  <img src="https://img.shields.io/badge/OpenHTMLToPDF-1.0.10-4B5563?style=flat-square" />
</p>

</div>

---

A polished, full-stack hotel booking web application built as a Year 2 Software Engineering project. The backend uses Spring Boot (Java 21) and JPA; frontend uses Thymeleaf with responsive CSS and JavaScript. This README is crafted to be attractive, informative and easy to follow — with animated elements for a modern feel.

Contents
- Quick demo
- Key highlights
- Beautiful features (customer & admin)
- Tech stack & nice badges
- Quick setup (with config template)
- Project layout
- How to add your own animation / GIF / Lottie fallback
- Contributing & contact

---

## 🎬 Live demo (replace with your own)

<p align="center">
  <!-- Replace this GIF with your own short demo (3–8s). -->
  <img src="https://media.giphy.com/media/3o7btNhMBytxAM6YBa/giphy.gif" alt="demo" width="820" style="border-radius:12px;box-shadow:0 10px 30px rgba(0,0,0,0.1)"/>
</p>

Tip: A 5–7s clip showing search → select → book → invoice gives the best impression.

---

## ✨ Key highlights

- Modern Spring Boot backend (Java 21) with JPA and MySQL
- Server-side rendered UI using Thymeleaf for SEO & accessibility
- Booking lifecycle: search → reserve → pay → invoice (PDF)
- Admin dashboard: rooms, bookings, analytics and reports
- Email notifications and PDF invoice generation (OpenHTMLToPDF)
- Styled responsive UI + helpful client-side interactions

---

## 🧭 Beautiful features (quick glance)

Customer
- 🔐 Secure registration & login (session-based)
- 🔎 Smart search by date, room type, price, amenities
- 🛏️ Simple booking flow + booking history
- 📧 Email confirmations & booking reminders
- 📄 Downloadable invoice (PDF)

Admin
- 📊 Dashboard with revenue & occupancy metrics
- 🏷️ Room management (add/edit/remove)
- 📋 Booking management & reporting
- 🔁 Manage policies, pricing and availability

---

## 🛠️ Tech stack (visual)

<p align="center">
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original-wordmark.svg" width="36" alt="java"/> Java 21 &nbsp;
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/spring/spring-original.svg" width="36" alt="spring"/> Spring Boot 3.5.6 &nbsp;
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/mysql/mysql-original.svg" width="36" alt="mysql"/> MySQL &nbsp;
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/thymeleaf/thymeleaf-original.svg" width="36" alt="thymeleaf"/> Thymeleaf
</p>

---

## 🚀 Quick start (copy-paste)

Prereqs: Java 21, Maven, MySQL, Git

1. Clone
```bash
git clone https://github.com/it24102732/Hotel-Reservation-System.git
cd Hotel-Reservation-System
```

2. Create DB
```sql
CREATE DATABASE hotel_reservation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Add configuration: create `src/main/resources/application.properties` (example below).

4. Build & run
```bash
mvn clean install
mvn spring-boot:run
# or
java -jar target/system-0.0.1-SNAPSHOT.jar
```

5. Open: http://localhost:8080

---

## ⚙️ application.properties (example)

Create `src/main/resources/application.properties` and adapt to your environment:

```
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_reservation_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=hotel_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Security / Profiles
spring.profiles.active=dev
```

Security note: Use environment variables or a secrets manager for credentials in production.

---

## 📁 Project layout (high level)

```
Hotel-Reservation-System/
├─ src/main/java/com/hotelmanagement/
│  ├─ controllers/      # MVC controllers
│  ├─ models/           # JPA entities
│  ├─ repository/       # Spring Data JPA repos
│  ├─ service/          # Business logic
│  ├─ config/           # Security & Web config
│  └─ HotelManagementApplication.java
├─ src/main/resources/
│  ├─ templates/        # Thymeleaf HTML templates
│  ├─ static/           # CSS, JS, images
│  └─ application.properties
├─ database/             # SQL schema & seed data
├─ pom.xml
└─ README.md
```

---

## 🔌 Common API endpoints

Authentication
- POST /api/auth/register
- POST /api/auth/login

Rooms
- GET /api/rooms
- GET /api/rooms/search
- POST /api/admin/rooms (admin only)

Bookings
- POST /api/bookings
- GET /api/bookings/{id}
- GET /api/users/{id}/bookings

Admin
- GET /api/admin/dashboard
- GET /api/admin/reports

(Adjust endpoints to match your controllers; add docs if you have a Swagger/OpenAPI setup.)

---

## 🎨 Make the README even more beautiful (animations & GIFs)

Tips to add attractive motion:
1. Typing header — already included (readme-typing-svg).
2. Animated hero — use Capsule Render (used above) for an animated banner.
3. Demo GIF — replace the placeholder GIF with a short 4–8s screen recording.
   - Record with OBS/QuickTime/Peek; convert & optimize with ffmpeg:
     ffmpeg -i demo.mp4 -vf "fps=12,scale=820:-1:flags=lanczos" -loop 0 demo.gif
4. Lottie → GIF fallback: export Lottie as GIF (JSON won't run in README).
5. Animated SVGs: host static SVG with SMIL/CSS animation — test on GitHub (some features are blocked).

Where to store media:
- Add small GIFs under `/docs/` or `/assets/` in repo to use relative links and track versions.

---

## 🧪 Tests & quality

Run unit tests:
```bash
mvn test
```

Generate coverage (if configured):
```bash
mvn jacoco:report
```

---

## 🤝 Contributing

1. Fork → Create branch: git checkout -b feature/awesome
2. Commit with clear messages
3. Push & open PR
4. Add tests and update README/docs for big changes

Please follow Java conventions and add Javadoc for public APIs.

---

## 📬 Contact & support

- Report issues: https://github.com/it24102732/Hotel-Reservation-System/issues
- Developer: it24102732 (GitHub)
- Email (example): it24102732@example.com

---

## ❤️ Acknowledgements & license

Made as a Year 2 Semester 1 Software Engineering academic project. See LICENSE for details. Thanks to course instructors, teammates and the open-source community.

---

If you'd like, I can:
- replace the demo GIF with a crisp 5–7s walkthrough if you upload an mp4,
- or adapt this README's headings, colors, or hero banner wording to match your personal style and branding, and then push the file to a new branch and open a PR for you.
