#!/bin/sh

cd python/create-user
docker build -t create-user .

cd ../create-post
docker build -t create-post .

cd ../create-subscription
docker build -t create-subscription .
