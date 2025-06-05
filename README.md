# Restaurant Management System

A Spring Boot-based restaurant management system that provides features for managing restaurant operations, reservations, and user management.

## Features

* User authentication and authorization
* Restaurant menu management
* Table reservation system
* Point system for customers
* Admin dashboard for restaurant management

## Tech Stack

* Java 17
* Spring Boot 3.x
* Spring Security
* Spring Data JPA
* MariaDB
* Thymeleaf
* Bootstrap 5

## Prerequisites

* Java 17 or higher
* Maven
* MariaDB

## Setup

1. Clone the repository:
```bash
git clone https://github.com/psymania0000/restaurant.git
```

2. Configure the database:
* Create a MariaDB database named `restaurant_db`
* Update the database credentials in `src/main/resources/application.yml`

3. Build and run the application:
```bash
mvn clean install
mvn spring-boot:run
```

4. Access the application:
* Open your browser and navigate to `http://localhost:8080`

## Default Admin Account

* Username: admin
* Password: admin123

## License

This project is licensed under the MIT License. 