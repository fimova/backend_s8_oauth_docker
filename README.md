# Proyecto Semana 8 - OAuth 2.0, Docker y Arquitectura de Microservicios

## 1. Objetivo del proyecto

El objetivo de este proyecto es implementar una arquitectura de microservicios que 
incorpore autenticación mediante OAuth 2.0, comunicación asíncrona mediante JMS/ActiveMQ, 
tolerancia a fallos mediante Resilience4j, configuración centralizada, descubrimiento de servicios y
ejecución mediante Docker Compose.

La solución utiliza 2 microservicios de negocio:

- **transacciones-service**: gestiona las transacciones, valida los tokens OAuth 2.0/JWT y publica
eventos cuando se crea una nueva transacción
- **mensajeria-service**: consume los eventos publicados mediante JMS y registra el resultado de su procesamiento

Además, se utilizan los siguientes componentes de infraestructura:

- **config-server**: centraliza la configuración de los servicios
- **discovery-server**: proporciona registro y descubrimiento de servicios mediante Eureka
- **auth-server**: servidor centralizado de autenticación y emisión de tokens JWT. Integra autenticación
mediante GitHub OAuth 2.0
- **ActiveMQ**: broker de mensajería utilizado para la comunicación asíncrona mediante JMS
- **Oracle Database**: base de datos utilizada para la persistencia de las transacciones y eventos procesados
- **Docker Compose**: permite ejecutar los componentes de la solución como servicios independientes dentro de
una misma red

## 2. Estructura del proyecto

El proyecto está organizado como una solución de múltiples aplicaciones Spring Boot:

``` text
C:.
│
├───auth-server
│   ├───src/
│   ├───Dockerfile
│   ├───pom.xml
│
├───config-server
│   ├───src/
│   ├───config-repo
│   ├───Dockerfile
│   ├───pom.xml
│
├───discovery-server
│   ├───src/
│   ├───Dockerfile
│   ├───pom.xml
│
├───mensajeria-service
│   ├───src/
│   ├───Dockerfile
│   ├───pom.xml
│
├───transacciones-service
│   ├───src/
│   ├───Dockerfile
│   ├───pom.xml
│
├───docker-compose.yml
│
└───README.md
```

### Responsabilidad de cada componente

| Aplicación | Responsabilidad |
| :---: | :---: |
| auth-server | Autenticación OAuth 2.0 mediante GitHub y emisión de JWT |
| config-server | Configuración centralizada |
| discovery-server| Registro y descubrimiento mediante Eureka |
| transacciones-service | API de transacciones, validación JWT y publicación JMS |
| mensajeria-service | Consumo y procesamiento de eventos JMS |
| ActiveMQ | Broker de mensajería | 
| Oracle | Persistencia de datos |

## 3. Arquitectura de eventos

El flujo principal de comunicación es:

``` text
GitHub
    │
    │ OAuth 2.0
    │ 
auth-server :9000
    │
    │ JWT
    │ 
transacciones-service :8081
    │
    │ TransaccionCreadaEvent
    │ 
ActiveMQ :61616
    │
    │ JMS
    │ 
mensajeria-service :8082
    │
    │ JMS
    │ 
Oracle DB
```

Los microservicios también utilizan:

``` text
config-server :8888
    │
    │ Configuración
    │ 
auth-server 
transacciones-service
mensajeria-service
```
``` text
discovery-server :8761
    │
    │ Registro
    │ 
auth-server 
transacciones-service
mensajeria-service
```
## 4. Flujo principal de una transacción

El flujo es el siguiente:

1. El usuario inicia sesión mediante GitHub utilizando OAuth 2.0
2. `auth-server` obtiene la información del usuario y genera un access token JWT
3. El cliente utiliza el JWT para acceder a `transacciones-service`
4. `transacciones-service` valida el JWT utilizando la clave pública expuesta por `auth-server`
5. Si el token es válido y contiene el rol requerido, se procesa la transacción
6. La transacción se persiste en Oracle
7. `transacciones-service` publica un `TransaccionCreadaEvent` en ActiveMQ
8. `mensajeria-service` recibe el evento mediante JMS
9. El evento es procesado y registrado en Oracle
10. Se verifica la idempotencia para evitar procesar nuevamente un evento ya completado

El evento contiene únicamente la información necesaria para el procesamiento del consumidor:

- transaccionId
- fecha
- monto
- tipo

El destino JMS utilizado es `transacciones.creadas`
                                                
## 5. Tecnologías utilizadas

- Java 21
- Maven
- Spring Boot 
- Spring Cloud
- Spring Security
- OAuth 2.0
- Spring Authorization Server
- GitHub OAuth
- JWT
- Spring Data JPA
- Spring JMS
- ActiveMQ Classic 
- Eureka
- Spring Cloud Config
- Resilience4j
- Spring Boot Actuator
- Oracle Database
- Oracle Wallet
- Docker
- Docker Compose

## 6. Configuración

La configuración común se encuentra centralizada en `config-server`.

Los archivos de configuración se encuentran en `config-server/config-repo/`

Entre las propiedades centralizadas se encuentran:
- Configuración de Eureka
- Configuración de ActiveMQ
- Configuración de Oracle
- Configuración de Resilience4j
- Configuración de Actuator
- Configuración del Resource Server OAuth 2.0
- Configuración específica de cada microservicio

Las URLs utilizadas entre contenedores se resuelven mediante nombres de servicio de Docker, por ejemplo:

- http://config-server:8888
- http://discovery-server:8761/eureka/
- http://auth-server:9000 
- tcp://activemq:61616

Las configuraciones permiten utilizar valores diferentes cuando las aplicaciones se ejecutan fuera de
Docker mediante valores por defecto definidos en las propiedades.

## 7. Variables de entorno

Las credenciales y datos sensibles no se almacenan directamente en el código fuente.
Antes de ejecutar Docker Compose se deben configurar las siguientes variables de entorno:

### 7.1 Oracle

- `DB_URL`: URL de conexión a Oracle
- `DB_USERNAME`: usuario de la base de datos
- `DB_PASSWORD`: contraseña de la base de datos
- `DB_TNS_ADMIN`: ruta local donde se encuentra Oracle Wallet

El wallet se monta dentro de los contenedores en /wallet y Docker Compose utiliza la variable
`DB_TNS_ADMIN` para realizar este montaje. 

### 7.2 GitHub OAuth 2.0

- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`

Corresponden a las credenciales de una aplicación OAuth registrada en GitHub. Estas credenciales son
utilizadas por `auth-server` para realizar el flujo de autenticación OAuth 2.0.

### 7.3 Configuración variables de entorno

En PowerShell o la terminal utilizada, las variables pueden configurarse para la sesión actual mediante:

- $env:DB_URL="valor_de_la_conexion"
- $env:DB_USERNAME="usuario_oracle"
- $env:DB_PASSWORD="password_oracle"
- $env:DB_TNS_ADMIN="C:\ruta\al\wallet"

- $env:GITHUB_CLIENT_ID="client_id"
- $env:GITHUB_CLIENT_SECRET="client_secret"

Una vez configuradas, se puede ejecutar Docker Compose desde la misma terminal.

## 8. Requisitos previos a ejecución 

Para ejecutar el proyecto se requiere:
- Java 21
- Maven
- Docker Desktop
- Oracle Database
- Oracle Wallet configurado
- Aplicación OAuth registrada en GitHub
- Variables de entorno configuradas

Las aplicaciones se ejecutan dentro de contenedores Docker, por lo que **no es necesario** iniciar 
cada microservicio manualmente mediante `mvn spring-boot:run`.

## 9. Ejecución con Docker Compose

### 9.1 Construcción de las imágenes

Desde la carpeta raíz del proyecto, se deben generar los archivos JAR mediante 
`mvn clean package -DskipTests` para cada aplicación.

Luego, se construyen las imágenes Docker correspondientes mediante:

- `docker build -t config-server:1.0 .\config-server`
- `docker build -t discovery-server:1.0 .\discovery-server`
- `docker build -t auth-server:1.0 .\auth-server`
- `docker build -t transacciones-service:1.0 .\transacciones-service`
- `docker build -t mensajeria-service:1.0 .\mensajeria-service`

ActiveMQ se obtiene mediante la imagen:

- symptoma/activemq:latest

### 9.2 Inicio de los servicios

Con las variables de entorno configuradas realizar `docker compose up -d` en la terminal para inicializar
el proyecto y los servicios.

Para comprobar el estado de los contenedores: `docker compose ps`.

Para revisar logs de un servicio: `docker compose logs -f (nombre-servicio)`

Para detenerlo: `docker compose down`.

### 9.3 Puertos

Los puertos utilizados son:

| Aplicación | Puerto | Protocolo |
| :---: | :---: | :---: |
| config-server | 8888 | HTTP |
| discovery-server| 8761 | HTTP |
| auth-server| 9000 | HTTP |
| transacciones-service | 8081 | HTTPS |
| mensajeria-service | 8082 | HTTP |
| ActiveMQ | 61616 | OpenWire/TCP |
| Consola ActiveMQ | 8161 | HTTP |

## 10. Seguridad

La autenticación se centraliza en `auth-server`, dejando a GitHub como proveedor 
OAuth 2.0.

Para iniciar el proceso de autenticación:

`http://localhost:9000/oauth2/authorization/github`

Después de completar el inicio de sesión, `auth-server` genera un access token JWT.

### Endpoints principales en transacciones-service

1. Consulta todas las transacciones

`GET  https://localhost:8081/api/transacciones`

2. Registra una transacción y publica el evento JMS

`POST https://localhost:8081/api/transacciones`

Ejemplo de body para POST:

``` json
{
    "fecha": "2024-08-15",
    "monto": 5000,
    "tipo": "CREDITO"
}
```

Ambos endpoints requieren:
Authorization: Bearer <access-token>

Los endpoints de transacciones están protegidos mediante Spring Security y requieren 
el rol correspondiente incluido en el JWT. Finalmente, el servicio valida los tokens 
utilizando el endpoint JWK de auth-server:

`http://localhost:9000/.well-known/jwks.json`

## 11. Consideraciones

Los secretos y credenciales utilizados para Oracle y GitHub deben mantenerse 
fuera del código fuente y proporcionarse mediante variables de entorno.

La configuración de las URLs de comunicación entre servicios se adapta al entorno de ejecución.
Dentro de Docker Compose se utilizan los nombres de los servicios, permitiendo que los 
microservicios se comuniquen dentro de la red Docker sin depender de localhost.

El proyecto está orientado a demostrar los principales componentes solicitados para la actividad:
autenticación OAuth 2.0, arquitectura de microservicios, configuración centralizada, descubrimiento, 
comunicación asíncrona, tolerancia a fallos y contenerización mediante Docker.

## 12. Mejoras futuras

Como posibles mejoras para futuras versiones del proyecto se consideran:

- Implementación de refresh tokens con mecanismos de revocación y expiración
- Incorporación de logs estructurados y trazabilidad distribuida
- Incorporación de métricas y monitoreo más avanzado
- Implementación de una Saga de coreografía completa con eventos de compensación
- Automatización del proceso de construcción y despliegue mediante CI/CD