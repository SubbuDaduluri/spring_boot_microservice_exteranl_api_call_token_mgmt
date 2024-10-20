# Spring Boot Microservice External API call with Token Management
Spring Boot Microservice that utilizes OAuth2 Client Credentials Grant for making external API calls and Refresh Token Caching

## Run Redis Stack on Docker
To start Redis Stack server using the redis-stack-server image, run the following command in your terminal:
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 -e REDIS_ARGS="--requirepass admin"  redis/redis-stack:latest

docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest


## Connect with redis-cli
$ docker exec -it redis-stack redis-cli

https://redis.io/docs/latest/operate/oss_and_stack/install/install-stack/docker/
