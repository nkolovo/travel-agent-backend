# 1. Start Cloud SQL Proxy on port 5433
Start-Process -NoNewWindow -FilePath "C:\Users\nikol\Documents\Personally\App\cloud-sql-proxy.x64.exe" `
    -ArgumentList "vital-defender-436715-h2:europe-west8:personally-travel-db --port=5433"

# 2. Set Spring profile to prod
$env:SPRING_PROFILES_ACTIVE="prod"

# 3. Run Spring Boot using Maven wrapper
./mvnw spring-boot:run
