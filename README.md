# Zarinpal Payment Gateway with Spring Boot 💳

This project is a complete integration of the [Zarinpal](https://www.zarinpal.com) payment gateway using Spring Boot. It handles the payment request, verification, and provides user-friendly views for success and failure. Email notifications are sent on successful transactions.

---

## ⚙️ Features

- ✅ Request payments using Zarinpal sandbox API
- ✅ Verify payments
- ✅ Store transactions in MySQL
- ✅ Email notifications on successful payment
- ✅ Success and failure pages using Thymeleaf
- ✅ Liquibase for DB version control

---

## 📦 Tech Stack

- Java 17+
- Spring Boot
- MySQL
- Liquibase
- Thymeleaf
- Spring Mail
- Zarinpal API (Sandbox)
- REST APIs

---

## 🔧 Setup

### 1. Clone the repo

```bash
git clone https://github.com/your-username/spring-boot-zarinpal-payment-gateway.git
```
### 2.Run the app

```bash
mvn spring-boot:run