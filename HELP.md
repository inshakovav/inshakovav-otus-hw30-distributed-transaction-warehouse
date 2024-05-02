### Deployment
```bash
mvn package
docker image build -t alxinsh/docker-java-hw30-warehouse:1.0.5 .
docker push alxinsh/docker-java-hw30-warehouse:1.0.5
```