<?php
// Configuración de cabeceras para permitir CORS y especificar tipo de contenido
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Verificar el método de la solicitud
$request_method = $_SERVER["REQUEST_METHOD"];

// Si es una solicitud GET, devolver todos los registros
if($request_method == "GET") {
    // Conectar a la base de datos MySQL
    $servername = "localhost";
    $username = "root"; // Reemplazar con tu usuario de MySQL
    $password = "Eduardo2003"; // Reemplazar con tu contraseña de MySQL
    $dbname = "datos_app"; // Reemplazar con el nombre de tu base de datos

    // Crear conexión
    $conn = new mysqli($servername, $username, $password, $dbname);

    // Verificar conexión
    if ($conn->connect_error) {
        http_response_code(500); // Internal Server Error
        echo json_encode(array(
            "success" => false,
            "message" => "Error de conexión a la base de datos: " . $conn->connect_error
        ));
        exit;
    }

    // Preparar la consulta SQL para obtener todos los registros
    $sql = "SELECT * FROM usuarios ORDER BY fecha DESC";
    $result = $conn->query($sql);

    if ($result) {
        $records = array();
        while($row = $result->fetch_assoc()) {
            $records[] = $row;
        }

        // Enviar respuesta
        http_response_code(200); // OK
        echo json_encode(array(
            "success" => true,
            "message" => "Registros obtenidos correctamente",
            "records" => $records
        ));
    } else {
        // Error al obtener registros
        http_response_code(500); // Internal Server Error
        echo json_encode(array(
            "success" => false,
            "message" => "Error al obtener registros: " . $conn->error
        ));
    }

    // Cerrar la conexión
    $conn->close();
    exit;
}

// Si es una solicitud POST, procesar los datos enviados
// Obtener el contenido JSON enviado en la solicitud
$input_data = file_get_contents("php://input");

// Decodificar el JSON a un array asociativo de PHP
$data = json_decode($input_data, true);

// Verificar si se pudo decodificar el JSON
if ($data === null) {
    // Error al decodificar el JSON
    http_response_code(400); // Bad Request
    echo json_encode(array(
        "success" => false,
        "message" => "Error al decodificar el JSON. Verifique el formato.",
        "error" => json_last_error_msg()
    ));
    exit;
}

// Verificar que todos los campos requeridos estén presentes
if (!isset($data['name']) || !isset($data['age']) || !isset($data['message'])) {
    http_response_code(400); // Bad Request
    echo json_encode(array(
        "success" => false,
        "message" => "Faltan campos requeridos (name, age, message)."
    ));
    exit;
}

// Validar los datos recibidos
$name = trim($data['name']);
$age = trim($data['age']);
$message = trim($data['message']);

// Validar que los campos no estén vacíos
if (empty($name) || empty($age) || empty($message)) {
    http_response_code(400); // Bad Request
    echo json_encode(array(
        "success" => false,
        "message" => "Todos los campos son obligatorios y no pueden estar vacíos."
    ));
    exit;
}

// Validar rango de edad
if (!is_numeric($age) || $age < 0 || $age > 120) {
    http_response_code(400); // Bad Request
    echo json_encode(array(
        "success" => false,
        "message" => "La edad debe ser un número entre 0 y 120."
    ));
    exit;
}

// En este punto, los datos son válidos
// Conectar a la base de datos MySQL
$servername = "localhost";
$username = "root"; // Reemplazar con tu usuario de MySQL
$password = "Eduardo2003"; // Reemplazar con tu contraseña de MySQL
$dbname = "datos_app"; // Reemplazar con el nombre de tu base de datos

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    http_response_code(500); // Internal Server Error
    echo json_encode(array(
        "success" => false,
        "message" => "Error de conexión a la base de datos: " . $conn->connect_error
    ));

    exit;
}

// Preparar la consulta SQL para insertar datos
$stmt = $conn->prepare("INSERT INTO usuarios (nombre, edad, mensaje, fecha) VALUES (?, ?, ?, ?)");
$timestamp = date("Y-m-d H:i:s");

// Vincular parámetros
$stmt->bind_param("siss", $name, $age, $message, $timestamp);

// Ejecutar la consulta
if ($stmt->execute()) {
    // Procesamiento exitoso
    $response = array(
        "success" => true,
        "message" => "Datos guardados correctamente en la base de datos",
        "data" => array(
            "name" => $name,
            "age" => $age,
            "message" => $message,
            "timestamp" => $timestamp,
            "server_info" => "PHP " . phpversion()
        )
    );
} else {
    // Error al guardar en la base de datos
    http_response_code(500); // Internal Server Error
    echo json_encode(array(
        "success" => false,
        "message" => "Error al guardar datos en la base de datos: " . $stmt->error
    ));
    $stmt->close();
    $conn->close();
    exit;
}

// Cerrar la conexión
$stmt->close();
$conn->close();

// Enviar respuesta
http_response_code(200); // OK
echo json_encode($response);
?>
