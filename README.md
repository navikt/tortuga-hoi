Tortuga HOI
===========


## Hoi

Hoi skal **H**ente **O**pplysninger om **I**nntekt til personer og tilby disse på en Kafka-topic.

## Installasjon og kjøring

### Bygging

For å bygge JAR og tilhørende Docker images:

```
make
```

Man kan også bygge med maven:

```
mvn package
```

### Testing

HOI kan startes med:

```
docker run \
    -e KAFKA_BOOTSTRAP_SERVERS=broker:9092 \
    -e SCHEMA_REGISTRY_URL=http://schema_registry:8081 \
    -e KAFKA_SASL_JAAS_CONFIG= \
    -e KAFKA_SASL_MECHANISM= \
    -e KAFKA_SECURITY_PROTOCOL= \
    -e SKATT_API_URL=http://testapi:8080/ekstern/skatt/datasamarbeid/api/formueinntekt/beregnetskatt/ \
    --network=tortuga_default \
    repo.adeo.no:5443/tortuga-hoi
```

Dette forutsetter at du har startet et Kafka testkluster via docker-compose fra https://github.com/navikt/tortuga-hiv. 
Navnet som angis til `--network` kan variere og du bør sjekke opp i dette og evt. endre i kommandoen ovenfor.
---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* David Steinsland, david.steinsland@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #peon.
