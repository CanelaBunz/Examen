-- Script para crear la base de datos y la tabla de usuarios
-- Este script debe ejecutarse en el servidor MySQL

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS datos_app;

-- Usar la base de datos
USE datos_app;

-- Crear la tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    edad INT NOT NULL,
    mensaje TEXT NOT NULL,
    fecha DATETIME NOT NULL
);

-- Crear un usuario para la aplicación (opcional, pero recomendado por seguridad)
-- Reemplazar 'usuario_db' y 'password_db' con credenciales seguras
CREATE USER IF NOT EXISTS 'usuario_db'@'localhost' IDENTIFIED BY 'password_db';

-- Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON datos_app.* TO 'usuario_db'@'localhost';

-- Aplicar los cambios de permisos
FLUSH PRIVILEGES;

-- Nota: Este script debe ejecutarse como usuario root o un usuario con permisos administrativos
-- Ejemplo de ejecución desde la línea de comandos:
-- mysql -u root -p < setup_database.sql