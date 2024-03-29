# Run setup-dockers.sh

## Start console with kafka cli
docker run -it \
--net=kafka-demo \
--rm confluentinc/cp-kafka:latest \
bash

# docker build pyton producers (in corresponding folders)
docker build -t create-user .
docker build -t create-subscription .
docker build -t create-post .

# Step 1
docker run -it \
  --net=kafka-demo \
  create-user

kafka-console-consumer --bootstrap-server kafka:9092 --topic users --property print.key=true --from-beginning

kafka-console-consumer --bootstrap-server kafka:9092 --topic blog.user.info --property print.key=true --from-beginning

# Step 2
docker run -it \
  --net=kafka-demo \
  create-subscription

kafka-console-consumer --bootstrap-server kafka:9092 --topic subscriptions --property print.key=true --from-beginning

kafka-console-consumer --bootstrap-server kafka:9092 --topic blog.user.subscription.count \
--property print.key=true --from-beginning --value-deserializer=org.apache.kafka.common.serialization.LongDeserializer

kafka-console-consumer --bootstrap-server kafka:9092 --topic blog.user.subscribers.count \
--property print.key=true --from-beginning --value-deserializer=org.apache.kafka.common.serialization.LongDeserializer

# Step 3

# Step 4
docker run -it \
  --net=kafka-demo \
  create-post

kafka-console-consumer --bootstrap-server kafka:9092 --topic blog.events.mail --property print.key=true --from-beginning

# Do steps in docker-teardown.txt
