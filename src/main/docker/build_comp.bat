cd ..
cd ..
cd ..
call gradlew bootJar
move build\libs\sjb-v1-0.0.1.jar src\main\docker\
cd src
cd main
cd docker
docker build -t 172.16.0.183:5000/tlw/sjb-v1:latest .
docker push 172.16.0.183:5000/tlw/sjb-v1:latest