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
- Les réponses métier des services appelés (404 cours introuvable, 409 plus de places) sont propagées telles quelles au client et n'ouvrent pas le circuit.
- Feign utilise Apache HttpClient 5 (`feign-hc5`) : le client HTTP du JDK ne sait pas émettre de requêtes `PATCH`.

## Codes de retour

| Situation | Code |
|---|---:|
| Réservation créée | 201 |
| Cours ou réservation introuvable | 404 |
| Plus de places, paiement expiré, annulation hors délais, état incohérent | 409 |
| Données invalides (ex. plus de 4 places) | 400 |
| Service critique indisponible (circuit breaker) | 503 |

## Tests

`mvn clean verify` exécute :

- `FitnessClassTest` (class-service) : incrément et `NoSpotsAvailableException`.
- `FitnessClassIntegrationTest` (class-service, `@SpringBootTest` + H2) : création par l'API, incrément/décrément réels des places, 409 quand le cours est complet, et 8 réservations concurrentes sans surréservation.
- `BookingServiceTest` : création, plus de places, paiement expiré, annulation hors délais, annulation avec remboursement.
- `BookingSchedulerTest` : expiration des paiements en attente.
- `BookingFlowIntegrationTest` (`@SpringBootTest` + H2) : parcours complet réservation → paiement → confirmation, et annulation des réservations expirées par le scheduler.
- `PaymentServiceTest` : seuil des 100 €.

## Essais

Importer `postman/FitConnect.postman_collection.json` et lancer le Collection Runner. La collection suit les cinq parties du sujet (cours, réservation, paiement, annulation, scénarios d'erreur) avec des assertions sur chaque requête. Les identifiants sont automatiquement enregistrés dans les variables de collection. En ligne de commande :

```powershell
npx newman run postman/FitConnect.postman_collection.json
```

Attendre une trentaine de secondes après le démarrage que tous les services soient enregistrés dans Eureka.

Le délai de paiement est d'une heure (`booking.payment-deadline-minutes` dans `config-repo/booking-service.yml`). Pour démontrer l'expiration sans attendre :

```powershell
java -jar booking-service/target/booking-service-1.0.0.jar --booking.payment-deadline-minutes=0
```

Toute nouvelle réservation est alors immédiatement expirée : `PATCH /api/bookings/{id}/confirm` répond 409, `GET /api/bookings/expired` la liste et le scheduler l'annule au passage suivant (5 minutes) en restituant les places.

Les consoles H2 sont activées sur les ports des services avec l'URL JDBC indiquée dans `config-repo` (utilisateur `sa`, mot de passe vide).
