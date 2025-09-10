run-dist:
	./build/install/app/bin/app

build:
	./gradlew build

clean:
	./gradlew clean

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

sonar:
	./gradlew build sonar --info

start:
	./gradlew run

install:
	./gradlew clean install

setup:
	./gradlew wrapper --gradle-version 8.5

run:
	./gradlew run

build-run: build run

.PHONY: build