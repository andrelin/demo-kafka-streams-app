
# create topics
docker run -it \
    --net=kafka-demo \
    --rm confluentinc/cp-kafka:latest \
    bash

kafka-topics --create --topic users --partitions 1 \
--replication-factor 1 --if-not-exists --bootstrap-server kafka:9092

kafka-topics --create --topic posts --partitions 1 \
--replication-factor 1 --if-not-exists --bootstrap-server kafka:9092

kafka-topics --create --topic subscriptions --partitions 1 \
--replication-factor 1 --if-not-exists --bootstrap-server kafka:9092

kafka-topics --create --topic user.subscription-count --partitions 1 \
--replication-factor 1 --if-not-exists --bootstrap-server kafka:9092

kafka-topics --create --topic user.subscribers-count --partitions 1 \
--replication-factor 1 --if-not-exists --bootstrap-server kafka:9092

kafka-topics --create --topic user.info --partitions 1 \
--replication-factor 1 --if-not-exists --bootstrap-server kafka:9092

kafka-topics --create --topic event.mails --partitions 1 \
--replication-factor 1 --if-not-exists --bootstrap-server kafka:9092
