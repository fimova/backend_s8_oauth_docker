# Proyecto Semana 6 - Spring Cloud

## 1. Objetivo del proyecto

El objetivo de este proyecto es implementar una arquitectura basada en microservicios utilizando Spring Boot, incorporando
mecanismos de configuración centralizada, descubrimiento de servicios, comunicación segura entre componentes, autenticación 
mediante JWT y tolerancia a fallos.

El proyecto implementa un microservicio de transacciones, utilizando uno de los archivos CSV proporcionados,
y un Backend for Frontend (BFF), apoyados por un servidor de configuración centralizada y
un servidor de descubrimiento de servicios.

La arquitectura permite que el BFF reciba las solicitudes del cliente y se comunique de forma segura con el microservicio de 
transacciones, mientras que Eureka permite localizar dinámicamente los servicios y Resilience4j permite manejar la
indisponibilidad temporal del microservicio.

## 2. Arquitectura y estructura del proyecto

### 2.1 Arquitectura

El proyecto está compuesto por cuatro aplicaciones independientes:

- **config-server**: centraliza la configuración de los servicios.
- **discovery-server**: proporciona el descubrimiento y registro de servicios mediante Eureka.
- **transacciones-service**: microservicio encargado de gestionar las transacciones y conectarse con Oracle.
- **web-bff**: punto de entrada para el cliente y encargado de comunicarse con el microservicio de transacciones.

El flujo principal es:

``` text
Cliente
    │
    │ HTTPS
    │ 
web-bff :8443     
    │
    │ REST + HTTPS 
    │ 
    │─── discovery-server (Eureka) :8761
    │─── transacciones-service :8081
                       │
                    Oracle DB
```

Durante el inicio de las aplicaciones, tanto `web-bff` como `transacciones-service` obtienen su configuración desde
`config-server` y se registran en `discovery-server`.

La comunicación entre el cliente y el BFF, y entre el BFF y el microservicio de transacciones, utiliza HTTPS. La autenticación 
se implementa mediante JWT y el token de acceso es validado tanto en el BFF como en el microservicio.

El BFF incorpora un Circuit Breaker mediante Resilience4j. Cuando el microservicio de transacciones no está disponible, se 
ejecuta un método de fallback que entrega una respuesta controlada al cliente.
                                                
### 2.2 Estructura del código

``` text
C:.
│   README.md
│
├───.vscode
│       settings.json
│
├───config-server
│   │   .gitattributes
│   │   .gitignore
│   │   HELP.md
│   │   mvnw
│   │   mvnw.cmd
│   │   pom.xml
│   │
│   ├───.mvn
│   │   └───wrapper
│   │           maven-wrapper.properties
│   │
│   ├───.vscode
│   │       settings.json
│   │
│   ├───config-repo
│   │       application.properties
│   │       transacciones-service.properties
│   │       web-bff.properties
│   │
│   ├───src
│   │   ├───main
│   │   │   ├───java
│   │   │   │   └───com
│   │   │   │       └───duoc
│   │   │   │           └───config_server
│   │   │   │               │   ConfigServerApplication.java
│   │   │   │               │
│   │   │   │               └───config
│   │   │   │                       SecurityConfig.java
│   │   │   │
│   │   │   └───resources
│   │   │           application.properties
│   │   │
│
├───discovery-server
│   │   .gitattributes
│   │   .gitignore
│   │   HELP.md
│   │   mvnw
│   │   mvnw.cmd
│   │   pom.xml
│   │
│   ├───.mvn
│   │   └───wrapper
│   │           maven-wrapper.properties
│   │
│   ├───.vscode
│   │       settings.json
│   │
│   ├───src
│   │   ├───main
│   │   │   ├───java
│   │   │   │   └───com
│   │   │   │       └───duoc
│   │   │   │           └───discovery_server
│   │   │   │               │   DiscoveryServerApplication.java
│   │   │   │               │
│   │   │   │               └───config
│   │   │   │                       SecurityConfig.java
│   │   │   │
│   │   │   └───resources
│   │   │           application.properties
│     
│
├───transacciones-service
│   │   .gitattributes
│   │   .gitignore
│   │   HELP.md
│   │   mvnw
│   │   mvnw.cmd
│   │   pom.xml
│   │
│   ├───.mvn
│   │   └───wrapper
│   │           maven-wrapper.properties
│   │
│   ├───.vscode
│   │       settings.json
│   │
│   ├───src
│   │   ├───main
│   │   │   ├───java
│   │   │   │   └───com
│   │   │   │       └───duoc
│   │   │   │           └───transacciones_service
│   │   │   │               │   TransaccionesServiceApplication.java
│   │   │   │               │
│   │   │   │               ├───config
│   │   │   │               │       DataInitializer.java
│   │   │   │               │       SecurityConfig.java
│   │   │   │               │
│   │   │   │               ├───controller
│   │   │   │               │       AuthController.java
│   │   │   │               │       TransaccionController.java
│   │   │   │               │
│   │   │   │               ├───dto
│   │   │   │               │       ErrorResponse.java
│   │   │   │               │       LoginRequest.java
│   │   │   │               │       LoginResponse.java
│   │   │   │               │       RefreshTokenRequest.java
│   │   │   │               │       TransaccionRequest.java
│   │   │   │               │       TransaccionResponse.java
│   │   │   │               │
│   │   │   │               ├───entity
│   │   │   │               │       Transaccion.java
│   │   │   │               │
│   │   │   │               ├───enums
│   │   │   │               │       TipoTransaccion.java
│   │   │   │               │
│   │   │   │               ├───exception
│   │   │   │               │       GlobalExceptionHandler.java
│   │   │   │               │       TransaccionNotFoundException.java
│   │   │   │               │
│   │   │   │               ├───repository
│   │   │   │               │       TransaccionRepository.java
│   │   │   │               │
│   │   │   │               ├───security
│   │   │   │               │       JwtAuthenticationFilter.java
│   │   │   │               │
│   │   │   │               └───service
│   │   │   │                       JwtService.java
│   │   │   │                       TransaccionImportService.java
│   │   │   │                       TransaccionService.java
│   │   │   │
│   │   │   └───resources
│   │   │       │   application.properties
│   │   │       │   https.p12
│   │   │       │   transacciones.csv
│   │   │       │
│   │   │       ├───static
│   │   │       └───templates
│   
│
└───web-bff
    │   .gitattributes
    │   .gitignore
    │   HELP.md
    │   mvnw
    │   mvnw.cmd
    │   pom.xml
    │
    ├───.mvn
    │   └───wrapper
    │           maven-wrapper.properties
    │
    ├───.vscode
    │       settings.json
    │
    ├───src
    │   ├───main
    │   │   ├───java
    │   │   │   └───com
    │   │   │       └───duoc
    │   │   │           └───web_bff
    │   │   │               │   WebBffApplication.java
    │   │   │               │
    │   │   │               ├───config
    │   │   │               │       RestClientConfig.java
    │   │   │               │       SecurityConfig.java
    │   │   │               │
    │   │   │               ├───controller
    │   │   │               │       WebBffController.java
    │   │   │               │
    │   │   │               ├───dto
    │   │   │               │       TransaccionResponse.java
    │   │   │               │
    │   │   │               ├───security
    │   │   │               │       JwtAuthenticationFilter.java
    │   │   │               │       JwtService.java
    │   │   │               │
    │   │   │               └───service
    │   │   │                       WebBffService.java
    │   │   │
    │   │   └───resources
    │   │       │   application.properties
    │   │       │   https.crt
    │   │       │   https.p12
    │   │       │   truststore.p12
    │   │       │
    │   │       ├───static
    │   │       └───templates
``` 
Cada aplicación posee su propio proyecto Maven, pero para ejecutar la arquitectura completa
los cuatro servicios deben iniciarse y mantenerse disponibles.

## 3. Requisitos

Para ejecutar el proyecto se requiere:

- Java 21
- Maven
- Oracle Database
- Oracle Wallet configurado para la conexión a la base de datos
- Cuatro terminales para ejecutar los servicios simultáneamente
- Variables de entorno necesarias para la conexión a Oracle y la clave JWT

Las aplicaciones utilizan Spring Boot 4.1.1 y Spring Cloud 2025.1.3.

## 4. Configuración

La configuración común de los servicios se encuentra centralizada en `config-server/config-repo`.

Entre las propiedades centralizadas se encuentran:

- Puerto de los servicios.
- Configuración de Eureka.
- Configuración de JWT.
- Configuración de Resilience4j para el BFF.

Las credenciales de la base de datos y la clave secreta utilizada para JWT se mantienen mediante variables de entorno.

Ejemplo de variables utilizadas:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- DB_TNS_ADMIN
- JWT_SECRET

La variable `JWT_SECRET` debe utilizar el mismo valor en los servicios que validan los tokens JWT.

## 5. Ejecución del proyecto

### 5.1 Orden de ejecución

Para ejecutar correctamente el proyecto, se recomienda iniciar los servicios en el siguiente orden:

1. config-server
2. discovery-server
3. transacciones-service
4. web-bff

Desde la carpeta de cada proyecto se puede ejecutar:

`mvn spring-boot:run`

Luego se repite el proceso para cada aplicación.

Los puertos utilizados son:

| Aplicación | Puerto | Protocolo |
| :---: | :---: | :---: |
| config-server | 8888 | HTTP |
| discovery-server| 8761 | HTTP |
| transacciones-service | 8081 | HTTPS |
| web-bff | 8443 | HTTPS|

### 5.2 Verificación de servicios

Una vez iniciadas las aplicaciones, se puede verificar el funcionamiento de cada componente mediante:

#### Config Server

http://localhost:8888

#### Eureka

http://localhost:8761

En el panel de Eureka deben aparecer registrados:

- `TRANSACCIONES-SERVICE`
- `WEB-BFF`

El microservicio de transacciones expone sus operaciones mediante:

https://localhost:8081/api/transacciones

El acceso del cliente se realiza a través del BFF mediante:

https://localhost:8443/api/web/transacciones

La autenticación se realiza mediante JWT. Primero se obtiene un token de acceso desde:

POST https://localhost:8081/auth/login

utilizando las credenciales configuradas para el usuario WEB.

| Usuario | Contraseña | Rol |
| :---: | :---: | :---: |
| usuarioWeb | web123 | ROLE_WEB |

Ejemplo:

```json
{
    "username": "usuarioWeb",
    "password": "web123"
}
```

Posteriormente, el token debe enviarse en las solicitudes protegidas mediante el encabezado:

Authorization: Bearer <access-token>

El BFF valida el token recibido y posteriormente lo propaga al microservicio de transacciones.

Si `transacciones-service` deja de estar disponible, el Circuit Breaker del BFF permite ejecutar el fallback configurado
evitando que la indisponibilidad del microservicio se propague directamente al cliente, mostrando un HTTP Status Code 200
y una lista vacía.