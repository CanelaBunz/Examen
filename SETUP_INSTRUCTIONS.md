# Instrucciones de Configuración

Este documento proporciona instrucciones detalladas para configurar un servidor Linux Mint en VirtualBox desde Windows 11, y la aplicación Android para el intercambio de información utilizando PHP y MySQL.

## Configuración de VirtualBox y Linux Mint

### Requisitos
- Windows 11
- VirtualBox 6.1 o superior
- Imagen ISO de Linux Mint (última versión)
- Al menos 20 GB de espacio libre en disco
- Al menos 4 GB de RAM disponible para la máquina virtual

### Instalación de VirtualBox

1. **Descargar VirtualBox**:
   - Visita [virtualbox.org](https://www.virtualbox.org/wiki/Downloads)
   - Descarga la versión para Windows hosts
   - Ejecuta el instalador y sigue las instrucciones

2. **Descargar Linux Mint**:
   - Visita [linuxmint.com/download.php](https://linuxmint.com/download.php)
   - Descarga la versión Cinnamon (recomendada)

### Crear la Máquina Virtual

1. **Crear una nueva máquina virtual**:
   - Abre VirtualBox y haz clic en "Nuevo"
   - Nombre: "Linux Mint Server"
   - Tipo: Linux
   - Versión: Ubuntu (64-bit)
   - Asigna al menos 2 GB de RAM
   - Crea un disco duro virtual (VDI) de al menos 20 GB

2. **Configurar la red**:
   - Selecciona la máquina virtual y haz clic en "Configuración"
   - Ve a "Red"
   - Adaptador 1: Selecciona "Adaptador puente"
   - Selecciona tu adaptador de red principal (WiFi o Ethernet)
   - Esto permitirá que la VM tenga su propia dirección IP en tu red local

3. **Instalar Linux Mint**:
   - Selecciona la máquina virtual y haz clic en "Iniciar"
   - Cuando se te pida, selecciona la imagen ISO de Linux Mint
   - Sigue el asistente de instalación de Linux Mint
   - Selecciona "Instalar Linux Mint" y sigue las instrucciones
   - Crea un usuario y contraseña (anótalos, los necesitarás más tarde)
   - Espera a que termine la instalación y reinicia cuando se te indique

## Configuración del Servidor en Linux Mint

### Requisitos del Servidor
- Apache2
- PHP 7.0 o superior
- MySQL o MariaDB
- Módulos PHP: mysqli, json

### Pasos de Configuración

1. **Instalar los paquetes necesarios**:
   - Abre una terminal en Linux Mint (Ctrl+Alt+T)
   ```bash
   sudo apt update
   sudo apt install apache2 php php-mysqli mysql-server
   ```

2. **Iniciar y habilitar los servicios**:
   ```bash
   sudo systemctl start apache2
   sudo systemctl enable apache2
   sudo systemctl start mysql
   sudo systemctl enable mysql
   ```

3. **Obtener la dirección IP de la máquina virtual**:
   ```bash
   ip addr show
   ```
   - Busca la dirección IP (algo como 192.168.x.x) en la interfaz "enp0s3" o similar
   - Anota esta dirección IP, la necesitarás para acceder al servidor desde Windows

4. **Transferir archivos desde Windows a la VM**:
   - **Opción 1: Usando Carpetas Compartidas de VirtualBox**
     - En VirtualBox, con la VM apagada, ve a "Configuración" > "Carpetas compartidas"
     - Agrega una nueva carpeta compartida:
       - Ruta de carpeta: Selecciona la carpeta en Windows donde están los archivos del proyecto
       - Nombre de carpeta: "proyecto_compartido"
       - Marca "Automontar" y "Hacer permanente"
     - Inicia la VM
     - En Linux Mint, abre una terminal y ejecuta:
       ```bash
       sudo adduser $USER vboxsf
       ```
     - Reinicia la VM
     - La carpeta compartida estará disponible en /media/sf_proyecto_compartido

   - **Opción 2: Usando un servidor FTP temporal en Windows**
     - Descarga e instala FileZilla Server en Windows
     - Configura un usuario y una carpeta compartida
     - Desde Linux Mint, usa el cliente FTP para descargar los archivos:
       ```bash
       sudo apt install filezilla
       ```
     - Abre FileZilla y conéctate a la IP de tu PC Windows

   - **Opción 3: Usando una unidad USB**
     - Copia los archivos a una unidad USB
     - Conecta la unidad USB a la VM (Dispositivos > USB > Tu unidad)
     - Monta la unidad en Linux Mint y copia los archivos

5. **Configurar Archivos PHP y Compartir Carpetas entre Windows y Linux**:
   Este paso es crítico para poder editar tus archivos PHP desde Windows y que se ejecuten en el servidor Apache de tu máquina virtual Linux.

   - **Parte A: Crear Carpeta Compartida en VirtualBox**
     - Objetivo: Tener una carpeta en Windows (ej: C:\php_projects) que esté sincronizada automáticamente con /var/www/html en Linux (donde Apache sirve los archivos).
     - Pasos:
       1. Apaga tu VM Linux Mint.
       2. Crea la carpeta C:\php_projects en Windows.
       3. En VirtualBox, ve a Configuración > Carpetas compartidas.
       4. Haz clic en el icono "+" para añadir una nueva carpeta compartida.
       5. Ruta de la carpeta: C:\php_projects
       6. Nombre de la carpeta: php_projects (así la identificaremos en Linux).
       7. Marca "Auto-montar" y "Hacer permanente".
       8. Inicia tu VM Linux Mint.

   - **Parte B: Montar la Carpeta Compartida en Linux**
     - Objetivo: Vincular la carpeta de Windows (C:\php_projects) con la carpeta de Apache (/var/www/html).
     - Pasos:
       1. Crea un punto de montaje en Linux:
          ```bash
          sudo mkdir -p /var/www/html
          ```
       2. Monta la carpeta compartida manualmente (para pruebas):
          ```bash
          sudo mount -t vboxsf php_projects /var/www/html
          ```
       3. Montaje automático (para que persista después de reiniciar):
          ```bash
          sudo nano /etc/fstab
          ```
          Agrega esta línea al final:
          ```
          php_projects /var/www/html vboxsf defaults 0 0
          ```
          Guarda con Ctrl + O, luego Ctrl + X.
       4. Verifica que el montaje funcione:
          ```bash
          ls /var/www/html  # Deberías ver archivos si hay alguno en C:\php_projects
          ```

   - **Parte C: Crear Archivos PHP desde Windows**
     - Objetivo: Editar archivos PHP en Windows y que Apache los ejecute desde Linux.
     - Pasos:
       1. Abre el Explorador de Archivos en Windows y ve a C:\php_projects.
       2. Crea los archivos PHP aquí (se sincronizarán con /var/www/html en Linux).
       3. Archivos de ejemplo:
          - demo_file.php (JSON básico):
            ```php
            <?php
            $data = [
                "name" => "John",
                "age" => 30,
                "city" => "New York"
            ];
            header('Content-Type: application/json');
            echo json_encode($data);
            ?>
            ```
          - demo_db.php (Conexión a MySQL):
            ```php
            <?php
            header('Content-Type: application/json');
            $conn = new mysqli("localhost", "devuser", "password123", "myDB");
            if ($conn->connect_error) {
                die(json_encode(["error" => "Connection failed: " . $conn->connect_error]));
            }
            $result = $conn->query("SELECT * FROM MyGuests");
            $data = $result->fetch_all(MYSQLI_ASSOC);
            echo json_encode($data);
            $conn->close();
            ?>
            ```

   - **Parte D: Permisos y Propiedad de Archivos**
     - Problema común: Apache no puede leer/escribir archivos porque los permisos son incorrectos.
     - Pasos:
       1. Asignar permisos correctos en Linux:
          ```bash
          sudo chown -R www-data:www-data /var/www/html
          sudo chmod -R 755 /var/www/html
          ```
       2. Verifica los permisos:
          ```bash
          ls -l /var/www/html
          ```
          Deberías ver algo como:
          ```
          -rwxr-xr-x 1 www-data www-data 123 Jun 10 10:00 demo_file.php
          ```

   - **Parte E: Probar los Archivos desde Windows**
     - Abre tu navegador en Windows y visita:
       - Para el JSON básico: http://192.168.1.84/api.php
         (Deberías ver: {"name":"John","age":30,"city":"New York"})
       - Para la conexión a MySQL: http://192.168.1.84/demo_db.php
         (Deberías ver los registros de la tabla MyGuests)
     - Nota: Reemplaza 192.168.1.84 con la dirección IP de tu VM Linux Mint.

6. **Configurar la base de datos**:
   - Copia el archivo `setup_database.sql` a la VM usando uno de los métodos anteriores
   - Ejecuta el script SQL para crear la base de datos y la tabla:
     ```bash
     sudo mysql -u root -p < setup_database.sql
     ```
   - Si te pide contraseña y no la has configurado, simplemente presiona Enter
   - Nota: Asegúrate de modificar las credenciales en el script SQL si es necesario

7. **Configurar el archivo PHP**:
   - Copia el archivo `api.php` al directorio web de Apache:
     ```bash
     sudo cp api.php /var/www/html/
     ```
   - Editar el archivo para actualizar las credenciales de la base de datos:
     ```bash
     sudo nano /var/www/html/api.php
     ```
   - Modificar las siguientes líneas con tus credenciales:
     ```php
     $servername = "localhost";
     $username = "usuario_db"; // Reemplazar con tu usuario de MySQL
     $password = "password_db"; // Reemplazar con tu contraseña de MySQL
     $dbname = "datos_app"; // Reemplazar con el nombre de tu base de datos
     ```

8. **Configurar permisos**:
   ```bash
   sudo chown www-data:www-data /var/www/html/api.php
   sudo chmod 644 /var/www/html/api.php
   ```

9. **Verificar la instalación**:
   - Desde Windows, abre un navegador y accede a `http://IP-DE-TU-VM/api.php`
     (reemplaza IP-DE-TU-VM con la dirección IP que anotaste en el paso 3)
   - Deberías ver un mensaje de error JSON indicando que faltan campos requeridos, lo que significa que el script está funcionando correctamente

## Configuración de la Aplicación Android

1. **Abrir el proyecto en Android Studio**:
   - Abre Android Studio en tu PC Windows
   - Selecciona "Open an Existing Project" y navega hasta la carpeta del proyecto

2. **Actualizar la URL del servidor**:
   - Abrir el archivo `app/src/main/java/com/example/examen_2ndo/PhpActivity.kt`
   - Localizar la línea:
     ```kotlin
     val url = URL("http://tu-servidor-linux-apache2.com/api.php")
     ```
   - Reemplazar con la dirección IP de tu máquina virtual Linux Mint:
     ```kotlin
     val url = URL("http://192.168.x.x/api.php")
     ```
     (Reemplaza 192.168.x.x con la dirección IP que anotaste durante la configuración del servidor)

3. **Verificar la configuración de red**:
   - Asegúrate de que tu dispositivo Android esté conectado a la misma red WiFi que tu PC Windows y la VM
   - Si estás usando un emulador, asegúrate de que pueda acceder a la red del host

4. **Corregir posibles errores**:
   - Si encuentras un error en la línea 146 que menciona "emailEditText", cámbialo a "ageEditText"

5. **Compilar y ejecutar la aplicación**:
   - Conecta un dispositivo Android o usa un emulador
   - Haz clic en "Run" en Android Studio

## Pruebas

1. **Probar la aplicación**:
   - Abre la aplicación en tu dispositivo Android
   - Navega a la actividad "Intercambio PHP"
   - Completa los campos de nombre, edad y mensaje
   - Haz clic en "Enviar Datos"
   - Deberías recibir una respuesta exitosa del servidor

2. **Verificar los datos en la base de datos**:
   - En la máquina virtual Linux Mint, abre una terminal y ejecuta:
   ```bash
   sudo mysql -u root -p
   USE datos_app;
   SELECT * FROM usuarios;
   ```
   - Deberías ver los datos que enviaste desde la aplicación

## Solución de Problemas

### Problemas de Conexión
- **Verificar la conectividad entre Windows y la VM**:
  - Desde Windows, abre una terminal (cmd o PowerShell) y ejecuta:
    ```
    ping 192.168.x.x
    ```
    (Reemplaza con la IP de tu VM)
  - Si no hay respuesta, revisa la configuración de red de VirtualBox

- **Verificar el firewall de Linux Mint**:
  - En la VM, ejecuta:
    ```bash
    sudo ufw status
    ```
  - Si está activo, permite el puerto 80:
    ```bash
    sudo ufw allow 80/tcp
    ```

- **Verificar el firewall de Windows**:
  - Asegúrate de que Windows Firewall no esté bloqueando la conexión
  - Ve a Panel de control > Sistema y seguridad > Firewall de Windows Defender
  - Verifica que las conexiones entrantes y salientes estén permitidas para VirtualBox

- **Verificar que Apache esté funcionando**:
  - En la VM, ejecuta:
    ```bash
    sudo systemctl status apache2
    ```
  - Si no está activo, inicia el servicio:
    ```bash
    sudo systemctl start apache2
    ```

### Problemas de Base de Datos
- Verifica las credenciales de la base de datos en api.php
- Asegúrate de que el usuario tenga permisos suficientes
- Verifica que la tabla exista y tenga la estructura correcta:
  ```bash
  sudo mysql -u root -p
  USE datos_app;
  DESCRIBE usuarios;
  ```

### Errores en la Aplicación
- Revisa los logs de la aplicación en Android Studio (Logcat)
- Verifica que los campos se estén enviando correctamente
- Asegúrate de que la aplicación tenga permisos de Internet en el AndroidManifest.xml

## Notas Adicionales

- **Apagar correctamente la VM**:
  - Cuando termines de usar la VM, apágala correctamente desde el menú de Linux Mint
  - No cierres VirtualBox directamente sin apagar la VM primero

- **Crear un snapshot de la VM**:
  - Una vez que todo esté configurado correctamente, crea un snapshot de la VM:
    - En VirtualBox, selecciona la VM
    - Ve a "Máquina" > "Tomar instantánea"
    - Nombra la instantánea (por ejemplo, "Configuración inicial completa")
    - Esto te permitirá volver a este estado si algo sale mal en el futuro

- **Consideraciones de seguridad**:
  - Esta configuración es básica y para entornos de desarrollo
  - Para entornos de producción, considera:
    - Implementar HTTPS para conexiones seguras
    - Configurar autenticación para proteger el API
    - Usar contraseñas fuertes para MySQL
    - Configurar correctamente los firewalls
