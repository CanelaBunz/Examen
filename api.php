<?php
// Configuración básica para permitir peticiones desde cualquier origen
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET");
header("Access-Control-Allow-Headers: Content-Type");

// Datos de conexión a la base de datos
$servername = "localhost";
$username = "root";
$password = "onepiecedeidad";
$dbname = "datos_app";

// Función para enviar respuesta JSON
function enviarRespuesta($codigo, $exito, $mensaje, $datos = null) {
    http_response_code($codigo);
    $respuesta = array(
        "success" => $exito,
        "message" => $mensaje
    );

    if ($datos !== null) {
        $respuesta = array_merge($respuesta, $datos);
    }

    echo json_encode($respuesta);
    exit;
}

// Conectar a la base de datos
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    enviarRespuesta(500, false, "Error de conexión: " . $conn->connect_error);
}

// Procesar según el método de la solicitud
$metodo = $_SERVER["REQUEST_METHOD"];

// Obtener todos los registros (GET)
if ($metodo == "GET") {
    $sql = "SELECT * FROM usuarios ORDER BY fecha DESC";
    $result = $conn->query($sql);

    if (!$result) {
        enviarRespuesta(500, false, "Error al obtener registros: " . $conn->error);
    }

    $registros = array();
    while ($fila = $result->fetch_assoc()) {
        $registros[] = $fila;
    }

    enviarRespuesta(200, true, "Registros obtenidos correctamente", array("records" => $registros));
}

// Guardar un nuevo registro (POST)
if ($metodo == "POST") {
    // Obtener y decodificar datos JSON
    $datos_json = file_get_contents("php://input");
    $datos = json_decode($datos_json, true);

    // Verificar si el JSON es válido
    if ($datos === null) {
        enviarRespuesta(400, false, "Error en formato JSON: " . json_last_error_msg());
    }

    // Verificar campos requeridos
    if (!isset($datos['name']) || !isset($datos['age']) || !isset($datos['message'])) {
        enviarRespuesta(400, false, "Faltan campos requeridos (name, age, message)");
    }

    // Limpiar y validar datos
    $nombre = trim($datos['name']);
    $edad = trim($datos['age']);
    $mensaje = trim($datos['message']);

    // Validar que no estén vacíos
    if (empty($nombre) || empty($edad) || empty($mensaje)) {
        enviarRespuesta(400, false, "Todos los campos son obligatorios");
    }

    // Validar edad
    if (!is_numeric($edad) || $edad < 0 || $edad > 120) {
        enviarRespuesta(400, false, "La edad debe ser un número entre 0 y 120");
    }

    // Insertar en la base de datos
    $fecha = date("Y-m-d H:i:s");
    $stmt = $conn->prepare("INSERT INTO usuarios (nombre, edad, mensaje, fecha) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("siss", $nombre, $edad, $mensaje, $fecha);

    if (!$stmt->execute()) {
        enviarRespuesta(500, false, "Error al guardar datos: " . $stmt->error);
    }

    // Datos guardados correctamente
    $datos_respuesta = array(
        "data" => array(
            "name" => $nombre,
            "age" => $edad,
            "message" => $mensaje,
            "timestamp" => $fecha,
            "server_info" => "PHP " . phpversion()
        )
    );

    $stmt->close();
    enviarRespuesta(200, true, "Datos guardados correctamente", $datos_respuesta);
}

// Si llegamos aquí, el método no está soportado
enviarRespuesta(405, false, "Método no permitido");
?>
