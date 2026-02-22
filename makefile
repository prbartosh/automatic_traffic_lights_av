.PHONY: build test run docker-build docker-run clean

build:
	mvn clean package -DskipTests -B

test:
	mvn clean verify -B

run: build
	java -jar target/simulation-1.0-jar-with-dependencies.jar example/input.json output.json

docker-build:
	docker build -t traffic-simulation:latest .

docker-run: docker-build
	docker run -v $(PWD)/example:/data traffic-simulation:latest /data/input.json /data/output.json

clean:
	mvn clean
	rm -f output.json