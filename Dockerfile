FROM eclipse-temurin:17-jre
WORKDIR /app

# GitHub Actions(또는 로컬 Gradle 빌드) 결과물 jar를 컨테이너에 복사
COPY build/libs/*.jar app.jar

EXPOSE 8080

# docker-compose에서 JAVA_OPTS를 넘기면 그대로 적용됨
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
