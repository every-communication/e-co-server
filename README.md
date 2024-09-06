# e-co-server

## FE 실행 방법

### 선행해야하는 내용

- docker desktop 다운로드
  - [windows](https://docs.docker.com/desktop/install/windows-install/)
  - [mac](https://docs.docker.com/desktop/install/mac-install/)

### 실행 방법

```sh
docker compose up -d --build
```

### 종료 방법

```sh
docker compose down
```

- 실행 이후 http://localhost:8080/swagger-ui/index.html 를 통해 스웨거 접근 가능.
- DB의 경우 localhost:3305
