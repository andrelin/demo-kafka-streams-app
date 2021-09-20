#!/bin/sh

cd python/create-user || exit
docker build -t create-user .

cd ../create-post || exit
docker build -t create-post .

cd ../create-subscription || exit
docker build -t create-subscription .
