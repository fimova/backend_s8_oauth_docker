# Proyecto Semana 7 - Arquitectura asíncrona con JMS

## 1. Objetivo del proyecto

El objetivo de este proyecto es implementar una arquitectura de microservicios orientada a eventos, 
incorporando comunicación asíncrona mediante JMS/ActiveMQ, tolerancia a fallos  con Resilience4j, configuración 
centralizada  y descubrimiento de servicios mediante Eureka.

La solución utiliza 2 microservicios de negocio:
- **transacciones-service**: registra las transacciones y publica eventos cuando se crea una nueva transacción
- **mensajeria-service**: consume los eventos publicados y registra el resultado de su procesamiento

Además, se utilizan componentes de infraestructura:

- **config-server**: centraliza la configuración de los servicios
- **discovery-server**: proporciona registro y descubrimiento mediante Eureka
- **ActiveMQ**: broker de mensajería utilizado para la comunicación asíncrona

## 2. Componentes

### Config-server

Centraliza las propiedades utilizadas por los servicios, incluyendo:

- Configuración de Eureka
- Configuración de ActiveMQ
- Configuración de Oracle
- Configuración de Resilience4j
- Propiedades específicas de cada microservicio

### Discovery-server

Implementa el descubrimiento de servicios mediante Eureka.

Los microservicios de negocio se registran automáticamente en Eureka utilizando las propiedades
centralizadas en `config-server`.

### Transacciones-service

Sus principales responsabilidades son:

- Autenticación mediante JWT
- Gestión de transacciones
- Persistencia de transacciones en Oracle
- Publicación del evento TransaccionCreadaEvent
- Comunicación con ActiveMQ mediante JMS
- Tolerancia a fallos mediante Retry y Circuit Breaker de Resilience4j

### Mensajeria-service

Microservicio consumidor de eventos.

Sus principales responsabilidades son:

- Recepción de eventos mediante JMS
- Procesamiento de TransaccionCreadaEvent
- Registro del resultado del procesamiento en Oracle
- Manejo de errores y redelivery de mensajes
- Control básico de idempotencia para evitar procesar nuevamente eventos ya completados

### ActiveMQ

Broker utilizado para desacoplar al productor del consumidor.

El destino utilizado para los eventos de transacciones es: `transacciones.creadas`

## 3. Arquitectura de eventos

El flujo principal de comunicación es:

``` text
transacciones-service
    │
    │ TransaccionCreadaEvent
    │ 
ActiveMQ    
    │
    │ JMS
    │ 
mensajeria-service
```

El evento contiene únicamente la información necesaria para el procesamiento del consumidor:

- transaccionId
- fecha
- monto
- tipo

`transacciones-service` persiste las transacciones en Oracle y posteriormente publica el 
evento correspondiente. `mensajeria-service` consume el evento y registra el resultado de su 
procesamiento en la tabla `eventos_procesados`, también almacenada en Oracle.

Esta separación permite desacoplar la creación de transacciones de su procesamiento posterior y
facilita la incorporación de nuevos consumidores en futuras versiones.
                                                
## 4. Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.1
- Spring Cloud 2025.1.3
- Spring Data JPA
- Spring JMS
- ActiveMQ Classic 6.3.2
- Eureka
- Spring Cloud Config
- Resilience4j
- Oracle Database 19c
- Oracle Wallet
- JWT
- Maven
- Docker

## 5. Configuración

La configuración común se encuentra centralizada en:

`config-server/src/main/resources/config-repo`

Entre las propiedades centralizadas se encuentran:
- Configuración de Eureka.
- Configuración de ActiveMQ.
- Configuración de Oracle.
- Configuración de Resilience4j.
- Configuración de Actuator.

Las credenciales y datos sensibles se mantienen mediante variables de entorno.

Variables utilizadas:
- DB_URL
- DB_USERNAME
- DB_PASSWORD
- DB_TNS_ADMIN
- JWT_SECRET

La variable JWT_SECRET debe utilizar el mismo valor en los componentes que validan 
los tokens JWT, en este caso, `transacciones-service`.

## 6. Ejecución del proyecto

### 6.1 Requisitos 

Para ejecutar el proyecto se requiere:
- Java 21
- Maven
- Docker
- Oracle Database
- Oracle Wallet configurado
- Variables de entorno configuradas

### 6.2 ActiveMQ

ActiveMQ se ejecuta mediante Docker:

`docker run -d --name activemq -p 61616:61616 -p 8161:8161 symptoma/activemq:latest`

El broker utiliza:

`tcp://127.0.0.1:61616`

La consola de administración se encuentra disponible en:

`http://localhost:8161/admin`

### 6.3 Orden de ejecución

Se recomienda iniciar los componentes en el siguiente orden:

1. ActiveMQ
2. config-server
3. discovery-server
4. transacciones-service
5. mensajeria-service

Para ejecutar cada aplicación, utilizar el siguiente comando desde su carpeta:

`mvn spring-boot:run`

### 6.4 Puertos

Los puertos utilizados son:

| Aplicación | Puerto | Protocolo |
| :---: | :---: | :---: |
| config-server | 8888 | HTTP |
| discovery-server| 8761 | HTTP |
| transacciones-service | 8081 | HTTPS |
| mensajeria-service | 8082 | HTTP |
| ActiveMQ | 61616 | OpenWire/TCP |
| Consola ActiveMQ | 8161 | HTTP |

## 7. Seguridad

La autenticación de usuarios se implementa en `transacciones-service` mediante Spring Security y JWT.

El endpoint de autenticación es:

`POST https://localhost:8081/auth/login`

utilizando las credenciales configuradas para el usuario de prueba:

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

Las operaciones protegidas requieren:

Authorization: Bearer <access-token>

La autenticación de usuarios pertenece al microservicio de transacciones. `mensajeria-service` actúa 
como consumidor interno de eventos JMS y no expone una capa de autenticación de usuarios.

## 8. Consideraciones y mejoras futuras

Durante el desarrollo se consideraron mejoras adicionales relacionadas con 
la evolución de la arquitectura. Entre ellas:

- Revocación y lista negra de refresh tokens
- Expiración forzada de tokens
- Correlación de eventos y trazabilidad distribuida más avanzada
- Logs estructurados
- Health checks y métricas adicionales para dependencias críticas
- Implementación completa de Saga de coreografía con eventos de respuesta y acciones compensatorias

Estas mejoras fueron consideradas dentro de la evolución del proyecto, pero no forman 
parte del alcance funcional de esta actividad.

La implementación actual prioriza los requisitos evaluados: arquitectura orientada a eventos, 
comunicación asíncrona mediante JMS/ActiveMQ y tolerancia a fallos mediante Resilience4j.