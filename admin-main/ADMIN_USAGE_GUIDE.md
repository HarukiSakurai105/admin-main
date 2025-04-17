# Admin Module Usage Guide

## Introduction
This guide provides an overview and instructions on how to use the Admin module of the Laptop Shop application. The Admin module is a Spring Boot web application that allows administrators to manage laptops, brands, view system logs, and monitor dashboard statistics.

## Running the Admin Application
The admin application is a Spring Boot application. To run it, execute the main class:

```java
com.laptopshop.admin.AdminApplication
```

You can run it from your IDE or by using Maven:

```bash
./mvnw spring-boot:run
```

The application will start a web server accessible at `http://localhost:8080`.

## Features

### Dashboard
- Displays total counts of laptops and brands.
- Shows recent system activities (logs).
- Provides quick access links to manage laptops, brands, logs, and system settings.

### Laptop Management
- View the list of laptops.
- Accessible via the `/laptops` URL.
- Allows administrators to manage laptop records.

### Brand Management
- View the list of laptop brands.
- Accessible via the `/brands` URL.
- Allows administrators to manage brand records.

### Logs
- View system logs and activities.
- Accessible via the `/logs` URL.
- Supports filtering logs by action type.

### Login
- Authentication page for administrators.
- Accessible via the `/login` URL.

## Source Code Structure

- `com.laptopshop.admin.controller`: Contains Spring MVC controllers handling web requests.
- `com.laptopshop.admin.model`: Entity classes representing database tables.
- `com.laptopshop.admin.repository`: Spring Data JPA repositories for data access.
- `com.laptopshop.admin.dto`: Data Transfer Objects used for specific views or data representations.
- `com.laptopshop.admin.AdminApplication`: Main application entry point.

## Templates and Static Resources

- Thymeleaf templates are located in `src/main/resources/templates`.
- Static resources such as CSS are located in `src/main/resources/static`.
- Templates include pages for dashboard, laptops, brands, logs, and login.

## Extending and Customizing

- Add new controllers in the `controller` package.
- Define new entities in the `model` package.
- Create repository interfaces in the `repository` package.
- Add or modify Thymeleaf templates for UI changes.
- Update static resources for styling and scripts.

## Additional Notes

- Ensure your database is configured correctly in `src/main/resources/application.properties`.
- The application uses Spring Boot and Spring Data JPA for rapid development.
- For any changes, rebuild and restart the application to see updates.

---

This guide should help you understand and use the Admin module effectively.
