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
docker compose up --build --wait
```

`--wait` rend la main quand la gateway répond sur ses quatre routes : la collection Postman peut être lancée immédiatement. Les jars exécutables portent le suffixe `-exec` (ex. `booking-service/target/booking-service-1.0.0-exec.jar`).

## Organisation du code

Chaque service métier suit le même découpage en packages :

| Package | Contenu |
|---|---|
| `controller` | endpoints REST |
| `service` | règles métier, orchestration de la Saga |
| `repository` | accès JPA |
| `model` | entités et enums |
| `dto` | objets d'échange (requêtes, vues des autres services) |
| `client` | clients Feign et leurs fallbacks |
| `exception` | exceptions métier et traduction en codes HTTP |
| `scheduler` | tâches planifiées (booking-service) |

Le code est formaté avec google-java-format (`mvn com.spotify.fmt:fmt-maven-plugin:2.23:format`).

## Règles métier couvertes

- Le cours porte un champ JPA `@Version`; deux incréments concurrents ne peuvent pas provoquer de surréservation. Sur conflit de version, class-service relit et réessaie avant de répondre 409.
- Une réservation capture les informations du cours, réserve les places et commence en `PENDING_PAYMENT` avec une échéance à +1 h (ou au début du cours s'il commence avant).
- Une erreur de persistance compense l'incrément des places.
- Le paiement est accepté sous 100 €, refusé à partir de 100 €. Un refus annule la réservation et restitue les places. `cardLastFour` est obligatoire pour `CREDIT_CARD` et `DEBIT_CARD`.
- Une réservation confirmée peut être annulée au plus tard 24 h avant le cours : paiement remboursé, places restituées, notification envoyée.
- Annuler un cours (`DELETE /api/classes/{id}`) annule toutes ses réservations actives, rembourse les paiements et envoie `CLASS_CANCELLED` à chaque inscrit (class-service appelle `PATCH /api/bookings/class/{classId}/cancel`).
- Toutes les cinq minutes, le scheduler annule les paiements expirés et envoie les rappels à 24 h (une seule fois grâce à `reminderSent`). L'annulation est enregistrée avant la libération des places, pour ne jamais les libérer deux fois.
- Les clients Feign utilisent Resilience4j et des fallbacks. Une panne des services critiques renvoie 503; une panne de notification ne casse pas la transaction métier.
- Les réponses métier des services appelés (404 cours introuvable, 409 plus de places) sont propagées telles quelles au client et n'ouvrent pas le circuit.
- Feign utilise Apache HttpClient 5 (`feign-hc5`) : le client HTTP du JDK ne sait pas émettre de requêtes `PATCH`.
- L'envoi des notifications est simulé (journalisé et enregistré en base avec son statut) : aucun serveur SMTP ni passerelle SMS n'est requis.

## Codes de retour

| Situation | Code |
|---|---:|
| Réservation créée | 201 |
| Cours ou réservation introuvable | 404 |
| Plus de places, paiement expiré, annulation hors délais, état incohérent | 409 |
| Données invalides (ex. plus de 4 places, carte sans `cardLastFour`) | 400 |
| Service critique indisponible (circuit breaker) | 503 |

## Tests

`mvn clean verify` exécute 22 tests :

- `InterServiceIntegrationTest` (booking-service) : **test interservices sans mock**. class-service, payment-service et notification-service sont réellement démarrés (HTTP + H2) et appelés par les clients Feign. Couvre le parcours complet (création du cours, réservation, paiement, places, notifications, annulation et remboursement), le refus quand le cours est complet, l'expiration par le scheduler avec restitution des places, et l'annulation d'un cours.
- `BookingFlowIntegrationTest` (`@SpringBootTest` + H2, clients simulés) : parcours réservation → paiement → confirmation et expiration.
- `BookingServiceTest` : création, plus de places, paiement expiré, annulation hors délais, annulation avec remboursement.
- `BookingSchedulerTest` : expiration des paiements en attente.
- `FitnessClassTest` : incrément et `NoSpotsAvailableException`.
- `FitnessClassIntegrationTest` (`@SpringBootTest` + H2) : création par l'API, incrément/décrément réels des places, 409 quand le cours est complet, 8 réservations concurrentes sans surréservation.
- `NotificationIntegrationTest` : envoi, historique, validation, notifications en attente et nouvel essai.
- `PaymentServiceTest` : seuil des 100 €.

## Essais

Importer `postman/FitConnect.postman_collection.json` et lancer le Collection Runner. La collection suit les cinq parties du sujet (cours, réservation, paiement, annulation, scénarios d'erreur), plus l'annulation d'un cours, avec des assertions sur chaque requête. Les identifiants sont automatiquement enregistrés dans les variables de collection. En ligne de commande :

```powershell
npx newman run postman/FitConnect.postman_collection.json
```

Le scénario « paiement expiré » est automatique : la collection crée un cours qui commence 6 secondes plus tard, le réserve (l'échéance de paiement est alors le début du cours), attend 8 secondes puis tente de payer et obtient 409 « Le délai de paiement est expiré ». `GET /api/bookings/expired` liste ensuite la réservation, que le scheduler annule à son passage suivant.

Le délai de paiement standard est d'une heure (`booking.payment-deadline-minutes` dans `config-repo/booking-service.yml`).

Les consoles H2 sont activées sur les ports des services avec l'URL JDBC indiquée dans `config-repo` (utilisateur `sa`, mot de passe vide).
