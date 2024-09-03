# 멀티 스테이지 빌드 방법 사용

# 첫번째 스테이지
FROM openjdk:11 as stage1
WORKDIR /app
# workdir에 copy 하는 것
# /app/gradlew로 파일로 생성
COPY gradlew .
# app/gradle 폴더로 생성
COPY gradle gradle
# app/src
COPY src src
COPY build.gradle .
COPY settings.gradle .

#  실행권한 없다고 에러 뜨는 경우 많음
RUN chmod +x gradlew
RUN ./gradlew bootJar

# 그냥 첫번째것만 하면 용량이 너무 크니까 두번째 스테이지에서는 jar 파일만 받는 것
# 두번째 스테이지
FROM openjdk:11
WORKDIR /app
# stage1에 있는 jar를(*를 붙임으로써 어떤 jar파일이라는 것) stage2의 app.jar라는 이름으로 copy
COPY --from=stage1 /app/build/libs/*.jar app.jar

#CMD 또는 ENTRYPOINT를 통해 컨테이너를 실행 (CMD보다 ENTRYPOINT를 더 많이 사용)
ENTRYPOINT ["java", "-jar", "app.jar"]

# docker 컨테이너 내에서 밖의 전체 host를 지칭하는 도메인 : host.docker.internal
# 도커 실행.
# docker run -d -p 8080:8082 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/board ordersystem:latest

# docker컨테이너 실행시에 볼륨을 설정할 때에는 -v 옵션을 사용
# docker run -d -p 8081:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/board -v C:\Users\anflw\Desktop\BeyondCamp\tmp_logs:/app/logs spring_test:latest
