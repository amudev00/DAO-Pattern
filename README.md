# Project DAO(Data Access Object) with JDBC in Java using MySQL

This project is an implementation of the DAO (Data Access Object) pattern in Java, using JDBC (Java Database Connectivity) to interact with a MySQL database. The DAO pattern is an approach to organize the persistence layer in an application, separating data access operations from business logic.

## About the Project

The project consists of a simple management system for sellers and departments. It allows performing basic CRUD (Create, Read, Update, Delete) operations for sellers and departments, storing the data in a MySQL database.



## Project Structure

The project is organized into packages, with the following structure:

- `src`: Contains the packages and classes of the project.
  - `model.entities`: Contains the entity classes of the system.
    - `Department`: Represents the Department entity.
    - `Seller`: Represents the Seller entity.
  - `model.dao`: Contains the interfaces for the DAO classes.
    - `DepartmentDao`: Interface for the Department DAO.
    - `SellerDao`: Interface for the Seller DAO.
  - `model.dao.impl`: Contains the implementations of the DAO interfaces.
    - `DepartmentDaoJDBC`: Implementation of the Department DAO using JDBC.
    - `SellerDaoJDBC`: Implementation of the Seller DAO using JDBC.
  - `db`: Contains the classes related to the database connection.
    - `DB`: Utility class for configuring the database connection.
    - `DbException`: Custom exception for handling database errors.
    - `DaoFactory`: Class for instantiating DAOs.
  - `db.properties`: Configuration file with the database access information.
  - `Demo.java`: Demonstration class with examples of using the functionalities.

## How to Use the Project

1. Clone the project repository to your local environment.
2. Configure the `db.properties` file with the access information to your MySQL database.
3. Import the project into your preferred Java IDE (such as Eclipse or IntelliJ).
4. Ensure that the dependencies are correctly configured (e.g., MySQLConnector
5. 
## Overview of the DAO Pattern

<!-- <img src="https://github.com/jbrun0r/assets/blob/main/Java-DAO-JDBC/data-access-object.png?raw=true" width="100%"> -->

![](https://github.com/jbrun0r/assets/blob/main/Java-DAO-JDBC/data-access-object.png?raw=true)



