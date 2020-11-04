
# create topics
docker run -it \
--net=kafka-demo \
--rm confluentinc/cp-kafka:5.4.3 \
bash

kafka-topics --create --topic users --partitions 1 \
--replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

kafka-topics --create --topic posts --partitions 1 \
--replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

kafka-topics --create --topic subscriptions --partitions 1 \
--replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

kafka-topics --create --topic user.subscription-count --partitions 1 \
--replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

kafka-topics --create --topic user.subscribers-count --partitions 1 \
--replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

kafka-topics --create --topic user.info --partitions 1 \
--replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

kafka-topics --create --topic event.mails --partitions 1 \
--replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
