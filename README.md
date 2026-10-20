# Impact Backend

Backend Spring Boot pentru lecțiile 1-4, cu autentificare JWT, catalog de produse și cache. Soluțiile pentru lecții și temă sunt în cod, cu comentarii care indică exercițiul.

## Pornire

Ai nevoie de JDK 21 și Maven. Din directorul proiectului:

```sh
mvn spring-boot:run
```

Aplicația este disponibilă la http://localhost:8080/, iar documentația API la http://localhost:8080/swagger-ui/index.html. Oprești serverul cu `Ctrl+C`.

H2 și cache-ul local pornesc automat. Frontend-ul este inclus. Prima rulare descarcă dependențele Maven și necesită internet.

| Acțiune | Windows PowerShell | macOS / Linux |
| --- | --- | --- |
| Pornire | `.\setup.cmd run` | `bash setup.sh run` |
| Teste | `.\setup.cmd check` | `bash setup.sh check` |
| Alt port | `.\setup.cmd run 8081` | `bash setup.sh run 8081` |
| Instalare unelte, dacă lipsesc | `.\setup.cmd install` | `bash setup.sh install` |

`mvn test` rulează testele backend. În IntelliJ, setează Project SDK și Maven Runner JRE la 21. Comanda `mvn -version` arată ce versiune Java folosește Maven.

## Conturi demo

| Rol | Email | Parolă |
| --- | --- | --- |
| ADMIN | `admin@impact.md` | `admin123` |
| USER | `user@impact.md` | `user123` |

La pornire sunt create 3 categorii și 6 produse. Datele H2 se pierd când oprești aplicația. După restart, autentifică-te din nou.

## Redis (opțional)

Implicit, aplicația folosește cache-ul local. Pentru exercițiile cu Redis, instalează-l cu `.\setup.cmd redis` pe Windows (Memurai) sau `bash setup.sh redis` pe macOS / Linux. Dacă ai Docker, îl poți porni cu `docker compose -f database/compose.yaml up -d --wait`. Redis funcționează cu H2, fără PostgreSQL.

Cu Redis pornit:

```sh
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.cache.type=redis"
```

Intrările din Redis expiră după 60 de secunde. Cache-ul local nu are expirare automată.

## Verificări

Pentru compilare, teste și generarea JAR-ului:

```sh
mvn --batch-mode --no-transfer-progress verify
```

Testele pentru scripturile de instalare necesită și Python 3:

```sh
python -m unittest discover -s scripts/tests -v
```

GitHub Actions rulează aceste verificări pe Linux și Windows la fiecare push și pull request. Rapoartele testelor Java sunt păstrate ca artefacte ale rulării.

Dependabot verifică săptămânal dependențele Maven, acțiunile GitHub și imaginea Redis din Docker Compose și deschide pull request-uri pentru actualizări.
