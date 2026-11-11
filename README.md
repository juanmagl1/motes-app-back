# Motes Backend - Instrucciones de Ejecución

## Requisitos
- Java 17 (instalado en: `C:\Program Files\Java\jdk-17`)
- IntelliJ IDEA (recomendado) o Eclipse

## Problema Actual
Maven en línea de comandos tiene problemas con el annotation processor de Lombok. La solución más rápida es usar IntelliJ IDEA.

## Instrucciones para IntelliJ IDEA

### 1. Importar Proyecto
1. Abrir IntelliJ IDEA
2. File → Open
3. Seleccionar la carpeta: `c:\Users\a900987.WW930\Downloads\workspace\motes-backend-with-cors`
4. Esperar a que IntelliJ descargue todas las dependencias Maven

### 2. Configurar Lombok
1. Ir a File → Settings (Ctrl+Alt+S)
2. Buscar "Lombok" en el cuadro de búsqueda
3. Ir a: Build, Execution, Deployment → Compiler → Annotation Processors
4. Marcar: ✅ **Enable annotation processing**
5. Click OK

### 3. Verificar Java 17
1. File → Project Structure (Ctrl+Alt+Shift+S)
2. Project Settings → Project
3. Verificar que Project SDK sea **17 (java version "17.0.7")**
4. Project language level: **17 - Sealed types, always strict floating-point semantics**
5. Click OK

### 4. Instalar Plugin de Lombok (si no está instalado)
1. File → Settings → Plugins
2. Buscar "Lombok"
3. Instalar el plugin oficial "Lombok"
4. Reiniciar IntelliJ

### 5. Rebuild Project
1. Build → Rebuild Project
2. Esperar a que compile sin errores

### 6. Ejecutar Aplicación
1. Buscar la clase: `MotesBackendApplication.java`
   - Ubicación: `src/main/java/com/example/motes/MotesBackendApplication.java`
2. Click derecho → Run 'MotesBackendApplication'

### 7. Verificar que Funciona
La aplicación debe iniciar en el puerto 8080. Deberías ver en la consola:
```
Started MotesBackendApplication in X.XXX seconds
```

## URLs Importantes Después de Iniciar

- **API Base**: http://localhost:8080/api
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/motes_db`
  - Username: `sa`
  - Password: (vacío)

## Usuarios de Prueba (creados automáticamente)

1. **demo@motes.com** / **demo123** (admin)
2. **juan@example.com** / **juan123**
3. **maria@example.com** / **maria123**

## Endpoints Principales

### Autenticación
- `POST /api/auth/register` - Registro
- `POST /api/auth/login` - Login

### Personas
- `GET /api/personas` - Listar
- `POST /api/personas` - Crear

### Motes
- `GET /api/motes` - Listar todos
- `GET /api/motes/top5` - Top 5 más votados
- `POST /api/motes` - Crear
- `PUT /api/motes/{id}` - Actualizar
- `DELETE /api/motes/{id}` - Eliminar

### Votos
- `POST /api/votos/toggle` - Toggle voto
- `GET /api/votos/check/{moteId}` - Verificar voto

## Prueba Rápida con cURL

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@motes.com","password":"demo123"}'

# Guardar el token de la respuesta y usarlo en las siguientes llamadas
# Authorization: Bearer {token}
```

## Troubleshooting

### Error: "Cannot resolve symbol 'getId'"
- Asegúrate de que Lombok plugin está instalado
- Asegúrate de que annotation processing está habilitado
- Rebuild project

### Error: "Package jakarta.persistence does not exist"
- Ir a Maven (panel derecho) → Reload All Maven Projects
- Build → Rebuild Project

### Puerto 8080 ya en uso
Cambiar en `application.yml`:
```yaml
server:
  port: 8081  # Cambiar a otro puerto
```

## Base de Datos H2

La base de datos se guarda en: `./data/motes_db.mv.db`

Para borrar la base de datos y empezar de cero:
1. Detener la aplicación
2. Eliminar la carpeta `data`
3. Reiniciar la aplicación (se recreará con usuarios de prueba)
