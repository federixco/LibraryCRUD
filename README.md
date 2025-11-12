# LibraryCRUD 

## Guía de Usuario

Este programa permite gestionar usuarios, libros y préstamos en una biblioteca.  
Incluye autenticación, administración de operadores, registro de auditoría y una interfaz gráfica basada en Swing.

### Requisitos previos
- Java 8 o superior instalado  
- Base de datos SQLite configurada con las tablas `usuarios`, `libros`, `prestamos` y `auditoria`  
- Archivo JAR del programa compilado

### Inicio de sesión
1. Al ejecutar el programa se abre el **LoginDialog**  
2. Ingrese su nombre de usuario y contraseña  
3. Si las credenciales son correctas, se abrirá la ventana principal  
4. Los administradores tienen acceso a funciones adicionales de gestión de usuarios

### Gestión de usuarios
Los administradores pueden:
- Registrar nuevos operadores  
- Eliminar usuarios  
- Resetear contraseñas  

Cada operación queda registrada en la tabla de auditoría.

### Gestión de libros
- Desde el menú **Libros** se pueden agregar, modificar o eliminar títulos  
- El stock debe ser mayor a cero para que un libro esté disponible  
- Los cambios se reflejan en la base de datos y pueden visualizarse en la tabla de libros

### Préstamos y devoluciones
- Para registrar un préstamo, seleccione el libro y la cantidad deseada  
- Ingrese el destinatario y confirme la operación  
- Al devolver un libro, se actualiza el stock automáticamente  
- Todas las operaciones quedan registradas en la auditoría con fecha y hora (`LocalDateTime`)

### Auditoría
- El menú **Auditoría** permite consultar todas las acciones realizadas en el sistema  
- Se muestran el operador, la acción, el objeto afectado y la marca temporal  
- Esta información asegura trazabilidad y control de seguridad
