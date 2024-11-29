
DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS OrderItems;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS CartItems;
DROP TABLE IF EXISTS DiscountCodes;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Users;

CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    role ENUM('user', 'admin') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE CartItems (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

CREATE TABLE Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('pending', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('Credit Card', 'Debit Card', 'PayPal') NOT NULL,
    shipping_address TEXT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE OrderItems (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

CREATE TABLE Payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('Credit Card', 'Debit Card', 'PayPal') NOT NULL,
    transaction_id VARCHAR(255),
    payment_status ENUM('pending', 'completed', 'failed') DEFAULT 'pending',
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);

CREATE TABLE DiscountCodes (
    discount_code VARCHAR(50) PRIMARY KEY,
    discount_percent DECIMAL(5, 2) NOT NULL,
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL
);

INSERT INTO Products (name, category, price, stock_quantity) 
VALUES 
('Laptop', 'Electronics', 1200.00, 10),
('Smartphone', 'Electronics', 800.00, 20),
('Coffee Maker', 'Home Appliances', 50.00, 15),
('Headphones', 'Accessories', 89.99, 200),
('Tablet', 'Electronics', 299.99, 80);


INSERT INTO DiscountCodes (discount_code, discount_percent, valid_from, valid_until) 
VALUES
('SAVE20', 20, '2024-11-01', '2024-12-01'),
('SPRING15',  15.00, '2024-03-01', '2024-05-31'),
('SUMMER20', 20.00, '2024-06-01', '2024-08-31'),
('FALL5', 5.00, '2024-09-01', '2024-10-31'),
('NEWYEAR25', 25.00, '2024-12-25', '2025-01-05');

DROP PROCEDURE IF EXISTS AddProduct;
CREATE PROCEDURE AddProduct(
    IN p_name VARCHAR(255),
    IN p_category VARCHAR(100),
    IN p_price DECIMAL(10, 2),
    IN p_stock_quantity INT
)
BEGIN
END //
DELIMITER ;

DELIMITER //
DROP PROCEDURE IF EXISTS UpdateProduct;
CREATE PROCEDURE UpdateProduct(
    IN p_product_id INT,
    IN p_name VARCHAR(255),
    IN p_category VARCHAR(100),
    IN p_price DECIMAL(10, 2),
    IN p_stock_quantity INT
)
BEGIN
    UPDATE Products
    SET 
        name = COALESCE(NULLIF(p_name, ''), name),
        category = COALESCE(NULLIF(p_category, ''), category),
        price = IF(p_price = -1, price, p_price),
        stock_quantity = IF(p_stock_quantity = -1, stock_quantity, p_stock_quantity)
    WHERE product_id = p_product_id;
END //
DELIMITER ;

DELIMITER //
DROP PROCEDURE IF EXISTS DeleteProduct;
CREATE PROCEDURE DeleteProduct(
    IN p_product_id INT
)
BEGIN
    DELETE FROM Products WHERE product_id = p_product_id;
END //
DELIMITER ;
