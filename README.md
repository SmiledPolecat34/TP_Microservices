# FitConnect — microservices de réservation sportive

Projet Maven multi-module Spring Boot 3 / Spring Cloud répondant au TP M2-DEV2. Il comprend Eureka, Config Server, API Gateway et quatre services métier autonomes avec une base H2 par service.

## Architecture

| Composant | Port | Rôle |
|---|---:|---|
| Eureka Server | 8761 | découverte de services |
| Config Server | 8888 | configuration centralisée depuis `config-repo` |
| API Gateway | 8080 | point d'entrée et quatre routes `/api/**` |
| class-service | 8091 | cours, recherche, capacité et verrouillage optimiste |
| booking-service | 8092 | réservations, Saga, expiration et rappels |
| payment-service | 8093 | paiements simulés et remboursements |
| notification-service | 8094 | historique et simulation d'envoi |

## Prérequis et démarrage

- Java 17+
- Maven 3.9+
- Docker (facultatif)

Compiler et exécuter les tests :

```powershell
mvn clean verify
```

Démarrage local, dans des terminaux séparés et dans cet ordre :

```powershell
mvn -pl eureka-server spring-boot:run
mvn -pl config-server spring-boot:run
mvn -pl class-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Toutes les requêtes passent alors par `http://localhost:8080`. La console Eureka est disponible sur `http://localhost:8761`.

Avec Docker :

```powershell
mvn clean package
docker compose up --build
```

## Règles métier couvertes

- Le cours porte un champ JPA `@Version`; deux incréments concurrents ne peuvent pas provoquer de surréservation.
- Une réservation capture les informations du cours, réserve les places et commence en `PENDING_PAYMENT` avec une échéance à +1 h.
- Une erreur de persistance compense l'incrément des places.
- Le paiement est accepté sous 100 €, refusé à partir de 100 €. Un refus annule la réservation et restitue les places.
- Une réservation confirmée peut être annulée au plus tard 24 h avant le cours : paiement remboursé, places restituées, notification envoyée.
- Toutes les cinq minutes, le scheduler annule les paiements expirés et envoie les rappels à 24 h (une seule fois grâce à `reminderSent`).
- Les clients Feign utilisent Resilience4j et des fallbacks. Une panne des services critiques renvoie 503; une panne de notification ne casse pas la transaction métier.

## Essais

Importer `postman/FitConnect.postman_collection.json`. La collection crée un cours futur, réserve deux places, paie, annule puis vérifie les états. Les identifiants sont automatiquement enregistrés dans les variables de collection.

Les consoles H2 sont activées sur les ports des services avec l'URL JDBC indiquée dans `config-repo` (utilisateur `sa`, mot de passe vide).
