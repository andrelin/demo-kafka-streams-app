#!/bin/sh

cd python/create-user
docker build -t create-user .

cd ../create-post
docker build -t create-posts .

cd ../create-subscriptions
docker build -t create-subscriptions .
