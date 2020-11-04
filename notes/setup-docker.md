# nettverk
docker network create kafka-demo

# zookeper
docker run -d \
  --net=kafka-demo \
  --name=zookeeper \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  confluentinc/cp-zookeeper:5.0.1

docker logs zookeeper

# kafka broker
docker run -d \
  --net=kafka-demo \
  --name=kafka \
  -p 9094:9094 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=DOCKER://kafka:9092,LOCAL://localhost:9094  \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=DOCKER:PLAINTEXT,LOCAL:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=DOCKER \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:5.0.1

docker logs kafka
