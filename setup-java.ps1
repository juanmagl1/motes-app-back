# Script para configurar JAVA_HOME y ejecutar tests
# Ejecutar este script antes de correr mvn test

Write-Host "=== Configurando Java 17 para Maven ===" -ForegroundColor Green

# Configurar JAVA_HOME para la sesión actual
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "C:\Program Files\Java\jdk-17\bin;$env:Path"

Write-Host "JAVA_HOME configurado: $env:JAVA_HOME" -ForegroundColor Cyan

# Verificar versión de Java
Write-Host "`nVerificando Java:" -ForegroundColor Yellow
java -version

# Verificar versión de javac (compilador)
Write-Host "`nVerificando Java Compiler:" -ForegroundColor Yellow
javac -version

Write-Host "`n=== Listo para ejecutar tests ===" -ForegroundColor Green
Write-Host "Ahora puedes ejecutar: mvn clean test" -ForegroundColor White
