# 1. Set Spring profile to dev
$env:SPRING_PROFILES_ACTIVE="dev"

# 2. Set the path to your Google Cloud service account key file
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\Users\nikol\Documents\GitHub\travel-agent-backend\vital-defender-436715-h2-99742208a90d.json"

# 3. Run Spring Boot using Maven wrapper
./mvnw spring-boot:run
