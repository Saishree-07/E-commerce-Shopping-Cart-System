package ECommerceShoppingCartSystem;

import java.sql.*;
import java.util.Scanner;

public class ECommerceShoppingCart {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Saishree@2707#";
    private static Connection connection;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database successfully.");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Register");
                System.out.println("2. Login");
                System.out.println("3. Browse Products");
                System.out.println("4. Add Product to Cart");
                System.out.println("5. View Cart");
                System.out.println("6. Checkout and Pay");
                System.out.println("7. Add Product (Admin Only)");
                System.out.println("8. Update Product (Admin Only)");
                System.out.println("9. Delete Product (Admin Only)");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> registerUser(scanner);
                    case 2 -> loginUser(scanner);
                    case 3 -> browseProducts();
                    case 4 -> addToCart(scanner);
                    case 5 -> viewCart();
                    case 6 -> checkoutAndPay(scanner);
                    case 7 -> addProduct(scanner);
                    case 8 -> updateProduct(scanner);
                    case 9 -> deleteProduct(scanner);
                    case 0 -> {
                        connection.close();
                        System.out.println("Disconnected from the database.");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void registerUser(Scanner scanner) throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.next();

        String checkUserSql = "SELECT * FROM Users WHERE username = ?";
        PreparedStatement checkUserStmt = connection.prepareStatement(checkUserSql);
        checkUserStmt.setString(1, username);
        ResultSet rs = checkUserStmt.executeQuery();

        if (rs.next()) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.next();
        System.out.print("Enter full name: ");
        String name = scanner.next();
        System.out.print("Enter email: ");
        String email = scanner.next();
        System.out.print("Enter address: ");
        String address = scanner.next();

        String sql = "INSERT INTO Users (username, password, name, email, address, role, created_at) VALUES (?, ?, ?, ?, ?, 'user', NOW())";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3, name);
        preparedStatement.setString(4, email);
        preparedStatement.setString(5, address);

        int rows = preparedStatement.executeUpdate();
        if (rows > 0) {
            System.out.println("User registered successfully!");
        } else {
            System.out.println("User registration failed.");
        }
    }

    private static void loginUser(Scanner scanner) throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();

        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            System.out.println("Login successful! Welcome, " + resultSet.getString("name"));
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    private static void browseProducts() throws SQLException {
        String sql = "SELECT * FROM Products";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        System.out.println("\nProduct List:");
        while (resultSet.next()) {
            System.out.printf("ID: %d, Name: %s, Category: %s, Price: %.2f, Stock: %d\n",
                    resultSet.getInt("product_id"), resultSet.getString("name"),
                    resultSet.getString("category"), resultSet.getDouble("price"),
                    resultSet.getInt("stock_quantity"));
        }
    }

    private static void addToCart(Scanner scanner) throws SQLException {
        System.out.print("Enter Product ID to add to cart: ");
        int productId = scanner.nextInt();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();

        String sql = "SELECT * FROM Products WHERE product_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, productId);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            if (resultSet.getInt("stock_quantity") >= quantity) {
                sql = "INSERT INTO CartItems (user_id, product_id, quantity, added_at) VALUES (?, ?, ?, NOW())";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, 1);
                preparedStatement.setInt(2, productId);
                preparedStatement.setInt(3, quantity);

                int rows = preparedStatement.executeUpdate();
                if (rows > 0) {
                    System.out.println("Product added to cart successfully.");
                }
            } else {
                System.out.println("Insufficient stock available.");
            }
        } else {
            System.out.println("Product not found.");
        }
    }

    private static void viewCart() throws SQLException {
        String sql = "SELECT Products.name, CartItems.quantity, Products.price " +
                "FROM CartItems JOIN Products ON CartItems.product_id = Products.product_id WHERE CartItems.user_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, 1);

        ResultSet resultSet = preparedStatement.executeQuery();
        System.out.println("\nCart Contents:");
        while (resultSet.next()) {
            System.out.printf("Product: %s, Quantity: %d, Price: %.2f\n",
                    resultSet.getString("name"), resultSet.getInt("quantity"),
                    resultSet.getDouble("price"));
        }
    }

    private static void checkoutAndPay(Scanner scanner) throws SQLException {
        int userId = 1;

        String sql = "SELECT SUM(Products.price * CartItems.quantity) AS total " +
                     "FROM CartItems JOIN Products ON CartItems.product_id = Products.product_id " +
                     "WHERE CartItems.user_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            double totalPrice = resultSet.getDouble("total");
            System.out.printf("Total Price: %.2f\n", totalPrice);

            System.out.print("Enter discount code (or press Enter to skip): ");
            String discountCode = scanner.nextLine().trim();
            if (!discountCode.isEmpty()) {
                totalPrice = applyDiscount(discountCode, totalPrice);
            }

            System.out.print("Enter payment method (Credit Card / Debit Card / PayPal): ");
            String paymentMethod = scanner.nextLine().trim();

            String orderSql = "INSERT INTO Orders (user_id, total_price, status, order_date, payment_method, shipping_address) " +
                              "VALUES (?, ?, 'pending', NOW(), ?, 'Default Address')";
            preparedStatement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, userId);
            preparedStatement.setDouble(2, totalPrice);
            preparedStatement.setString(3, paymentMethod);
            int rows = preparedStatement.executeUpdate();

            if (rows > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    String orderItemsSql = "INSERT INTO OrderItems (order_id, product_id, quantity, price) " +
                                           "SELECT ?, CartItems.product_id, CartItems.quantity, Products.price " +
                                           "FROM CartItems JOIN Products ON CartItems.product_id = Products.product_id " +
                                           "WHERE CartItems.user_id = ?";
                    preparedStatement = connection.prepareStatement(orderItemsSql);
                    preparedStatement.setInt(1, orderId);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.executeUpdate();

                    makePayment(orderId, totalPrice, paymentMethod);
                }
            }
        } else {
            System.out.println("Your cart is empty.");
        }
    }

    private static double applyDiscount(String discountCode, double totalPrice) throws SQLException {
        String sql = "SELECT discount_percent, valid_from, valid_until FROM DiscountCodes WHERE discount_code = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, discountCode);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            double discountPercent = resultSet.getDouble("discount_percent");
            Date validFrom = resultSet.getDate("valid_from");
            Date validUntil = resultSet.getDate("valid_until");
            Date currentDate = new Date(System.currentTimeMillis());

            if (!currentDate.before(validFrom) && !currentDate.after(validUntil)) {
                totalPrice *= (1 - discountPercent / 100);
                System.out.printf("Discount applied! New total: %.2f\n", totalPrice);
            } else {
                System.out.println("Discount code is not valid at this time.");
            }
        } else {
            System.out.println("Invalid discount code.");
        }

        return totalPrice;
    }

    private static void makePayment(int orderId, double amount, String paymentMethod) throws SQLException {
        String insertPaymentSql = "INSERT INTO Payments (order_id, amount, payment_method, payment_status, transaction_id) " +
                                   "VALUES (?, ?, ?, 'pending', ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertPaymentSql);
        preparedStatement.setInt(1, orderId);
        preparedStatement.setDouble(2, amount);
        preparedStatement.setString(3, paymentMethod);
        preparedStatement.setString(4, "TX" + System.currentTimeMillis()); 

        int rows = preparedStatement.executeUpdate();
        if (rows > 0) {
            System.out.println("Payment record inserted successfully.");
        } else {
            System.out.println("Failed to insert payment record.");
        }

        updatePaymentStatus(orderId);
    }

    private static void updatePaymentStatus(int orderId) throws SQLException {
        String sql = "UPDATE Payments SET payment_status = 'completed' WHERE order_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, orderId);

        int rows = preparedStatement.executeUpdate();
        if (rows > 0) {
            System.out.println("Payment status updated to 'completed' in the database.");
        } else {
            System.out.println("Failed to update payment status.");
        }
    }


    private static void addProduct(Scanner scanner) throws SQLException {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter product category: ");
        String category = scanner.nextLine();
        System.out.print("Enter product price: ");
     
        double price = scanner.nextDouble();
        System.out.print("Enter stock quantity: ");
        int stockQuantity = scanner.nextInt();
        scanner.nextLine();

        String sql = "INSERT INTO Products (name, category, price, stock_quantity) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, category);
        preparedStatement.setDouble(3, price);
        preparedStatement.setInt(4, stockQuantity);

        int rows = preparedStatement.executeUpdate();
        if (rows > 0) {
            System.out.println("Product added successfully!");
        } else {
            System.out.println("Failed to add product.");
        }
    }

    private static void updateProduct(Scanner scanner) throws SQLException {
        System.out.print("Enter Product ID to update: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter new product name (or leave blank to keep current): ");
        String name = scanner.nextLine();
        System.out.print("Enter new product category (or leave blank to keep current): ");
        String category = scanner.nextLine();
        System.out.print("Enter new product price (or -1 to keep current): ");
        double price = scanner.nextDouble();
        System.out.print("Enter new stock quantity (or -1 to keep current): ");
        int stockQuantity = scanner.nextInt();
        scanner.nextLine();

        String sql = "UPDATE Products SET " +
                     "name = COALESCE(NULLIF(?, ''), name), " +
                     "category = COALESCE(NULLIF(?, ''), category), " +
                     "price = CASE WHEN ? = -1 THEN price ELSE ? END, " +
                     "stock_quantity = CASE WHEN ? = -1 THEN stock_quantity ELSE ? END " +
                     "WHERE product_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, category);
        preparedStatement.setDouble(3, price);
        preparedStatement.setDouble(4, price);
        preparedStatement.setInt(5, stockQuantity);
        preparedStatement.setInt(6, stockQuantity);
        preparedStatement.setInt(7, productId);

        int rows = preparedStatement.executeUpdate();
        if (rows > 0) {
            System.out.println("Product updated successfully!");
        } else {
            System.out.println("Failed to update product.");
        }
    }

    private static void deleteProduct(Scanner scanner) throws SQLException {
        System.out.print("Enter Product ID to delete: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        String sql = "DELETE FROM Products WHERE product_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, productId);

        int rows = preparedStatement.executeUpdate();
        if (rows > 0) {
            System.out.println("Product deleted successfully!");
        } else {
            System.out.println("Failed to delete product.");
        }
    }
}
