<?php
$host = '127.0.0.1';
$user = 'innovfix_ads';
$password = 'InnovfixAds2024!Secure';
$dbname = 'onlycare_admin';

try {
    // Connect without selecting a database
    $pdo = new PDO("mysql:host=$host", $user, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Check if database exists
    $stmt = $pdo->query("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$dbname'");
    if ($stmt->rowCount() > 0) {
        echo "Database '$dbname' already exists.\n";
    } else {
        // Create database
        $pdo->exec("CREATE DATABASE `$dbname` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        echo "Database '$dbname' created successfully.\n";
    }
    
    // List all databases (to verify)
    echo "\nCurrent databases:\n";
    $stmt = $pdo->query("SHOW DATABASES");
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        echo "  - " . $row['Database'] . "\n";
    }
} catch (PDOException $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
