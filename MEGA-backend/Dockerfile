# Imagem base com Java 21
FROM eclipse-temurin:21-jdk

# Diretório onde a app vai ficar dentro do container
WORKDIR /app

# Copiar o .jar compilado pelo Maven
COPY target/*.jar app.jar

# Porta exposta pelo Spring Boot
EXPOSE 8080

# Comando para arrancar o backend
ENTRYPOINT ["java", "-jar", "app.jar"]
