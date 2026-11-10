# Evidencias — Taller 11: contenedores y CI

Salidas reales tomadas el 2026-11-10 12:36.

## 1. La imagen de eureka-server (pasos 2 y 3)

```
$ docker images rica/eureka-server
REPOSITORY           TAG       SIZE
rica/eureka-server   0.1       607MB
```

```
$ docker history rica/eureka-server:0.1   # primeras capas
0B	ENTRYPOINT ["java" "-jar" "app.jar"]
0B	EXPOSE [8761/tcp]
60.7MB	COPY /app/target/*.jar app.jar # buildkit
0B	USER rica
41kB	RUN /bin/sh -c useradd --system --no-create-…
8.19kB	WORKDIR /app
```

## 2. GitHub Actions: ejecuciones del workflow CI en la rama taller-contenedores (paso 7)

Capturas: `taller-11-actions-rojo.png` (ejecución `f76725f`, consultas-service en rojo) y `taller-11-actions-lista.png` (lista de ejecuciones en verde y rojo). Estas son las mismas ejecuciones leídas de la API de GitHub; los hashes existen en `git log --oneline`:

```
$ curl -s 'https://api.github.com/repos/usechet1/rica-api/actions/runs?branch=taller-contenedores'   # resumido: commit, resultado y job en rojo
4f92a35 success  leccion 11 
f76725f failure  Prueba: romper la CI a propósito | en rojo: Pruebas de consultas-service
99be3b2 success  Revert "Prueba: romper la CI a propósito" 
384cdd7 success  chore: unificar identidad de git con .mailmap 
bc95ec8 success  Taller 3 y 4: pruebas del dominio y paquete de puertos  
```

