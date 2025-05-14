<?php
$servername="localhost";
$username="root";
$password="onepiecedeidad";
$dbname="datos_app";

$conn=new mysqli($servername,$username,$password,$dbname);

if($conn->connect_error){
    die("Error de conexion:".$conn->connect_error);
}else{
    echo "Conexion exitosa a MySQL";
}
$conn->close();
?>
