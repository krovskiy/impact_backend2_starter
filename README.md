# Impact Backend — lecțiile 1–11

Proiect de curs Spring Boot pentru un magazin: API, frontend inclus și exemple pentru lecțiile 1–11. H2 și cache-ul local pornesc automat; nu sunt necesare Docker, PostgreSQL sau Redis pentru exercițiile obișnuite.

## Pornire pe Windows, macOS și Linux

Instalează **JDK 21** și **Maven 3.9+**, apoi, în directorul proiectului, rulează:

```sh
mvn spring-boot:run
```

Deschide http://localhost:8080/ și alege lecția din meniu. Documentația API: http://localhost:8080/swagger-ui/index.html. Oprește aplicația cu Ctrl+C. Prima rulare descarcă dependențele Maven și necesită internet. `mvn -version` arată versiunea Java folosită de Maven. În IntelliJ, setează atât Project SDK, cât și Maven Runner JRE la 21.

| Acțiune | Windows PowerShell | macOS / Linux |
| --- | --- | --- |
| Pornire | `.\setup.cmd run` | `bash setup.sh run` |
| Teste | `.\setup.cmd check` | `bash setup.sh check` |
| Alt port | `.\setup.cmd run 8081` | `bash setup.sh run 8081` |
| Instalare unelte | `.\setup.cmd install` | `bash setup.sh install` |

Instalarea pe Linux este pentru Debian și distribuții compatibile; pe alte distribuții instalează JDK și Maven cu managerul propriu. Alternativ, cu Docker și Docker Compose instalate, `docker compose up --build` pornește aplicația pe portul 8080. La alt port, setează și adresa API în consola frontendului.

| Rol | Email | Parolă |
| --- | --- | --- |
| ADMIN | `admin@impact.md` | `admin123` |
| USER | `user@impact.md` | `user123` |

Datele H2, conturile nou create și refresh tokenurile se pierd la oprire. Cheia JWT locală se regenerează la fiecare pornire; autentifică-te din nou după restart. Aceste conturi sunt doar pentru curs.

## Lecțiile

1. HTTP, controllere și CORS: `/api/practice`.
2. JPA, validare, BCrypt și JWT: catalog și autentificare.
3. Cache, Redis opțional și Swagger.
4. Clean Code și SOLID: separarea serviciilor și a mapperului.
5. Strategy: reducerea configurabilă pe categorie în `pricing/`.
6. Configurare prin variabile de mediu, profiluri și `/api/config/info`.
7. Containerizare: `Dockerfile` multi-stage și `compose.yaml`.
8. Teste unitare cu JUnit și Mockito.
9. Teste de integrare și API cu Spring Boot și MockMvc.
10. Securitate: validare, parole BCrypt, răspunsuri de eroare și acces pe rol.
11. Refresh tokenuri rotite și revocate, autorizare ADMIN și reducere manuală per produs. OAuth2 este prezentat conceptual în prezentare; conectarea la un furnizor nu este activată.

### Exemple pentru lecțiile 5, 6 și 11

Spring injectează strategiile în ordinea `@Order`; prima potrivită calculează prețul. O reducere manuală pozitivă are prioritate:

```java
if (manual != null && manual.signum() > 0) {
    return price.multiply(BigDecimal.ONE.subtract(manual.movePointLeft(2)))
            .setScale(2, RoundingMode.HALF_UP);
}
return strategies.stream().filter(s -> s.appliesTo(category))
        .findFirst().map(s -> s.apply(price)).orElse(price);
```

Categoria și procentul implicite sunt `Electronics` și `15`; se pot schimba prin `DISCOUNT_CATEGORY` și `DISCOUNT_PERCENTAGE`. `APP_DISPLAY_NAME` schimbă numele întors de `/api/config/info`. Profilul `prod` cere explicit `JWT_SECRET` (minimum 32 bytes UTF-8) și `APP_CORS_ALLOWED_ORIGINS` (origini separate prin virgulă):

```properties
jwt.secret=${JWT_SECRET}
app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS}
```

Acesta este un exercițiu de profil, nu o configurație completă de producție: folosește în continuare H2 temporar și date demo. Variabilele se setează în mediul sistemului; activează profilul prin `SPRING_PROFILES_ACTIVE=prod` în acel mediu.

La login sau înregistrare răspunsul conține `token` și `refreshToken`. Trimite `{"refreshToken":"..."}` prin POST la `/api/auth/refresh`: tokenul vechi este revocat, iar răspunsul conține o pereche nouă. Reutilizarea celui vechi întoarce 401. POST `/api/auth/logout` revocă refresh tokenul; un JWT deja emis rămâne valabil până expiră. `POST`, `PUT` și `DELETE /api/products` cer ADMIN: fără autentificare se întoarce 401, iar USER primește 403. La crearea/editarea unui produs, `discountPercentage` acceptă 0–100 sau `null`; modificarea invalidează cache-ul catalogului.

```java
RefreshToken existing = refreshTokens.findLockedByToken(token).orElseThrow(this::invalidRefresh);
if (existing.isRevoked() || !existing.getExpiresAt().isAfter(Instant.now())) throw invalidRefresh();
existing.revoke();
return response(existing.getUser()); // emite o nouă pereche
```

Rulează `mvn test` pentru suita completă, inclusiv testele de rotație, autorizare și discount.

## Frontend

Fișierul inclus `src/main/resources/static/index.html` este copia construită din [impact_2_year_fe](https://github.com/Victoras23/impact_2_year_fe), ramura `main`, commit `b4c14698ec0a0e06513816930eee12bf6b926ba8`. Frontendul poate conține ecrane pentru lecții viitoare; backendul acestui template acoperă până la lecția 11.

Verifică dacă a apărut o versiune nouă cu `java scripts/CheckFrontend.java` din rădăcina proiectului. Comanda necesită Git și internet, verifică atât fișierul local cât și ultimul commit `main` și returnează cod nenul când există o diferență. Pentru actualizare, verifică API-ul noii versiuni, înlocuiește HTML-ul cu `index.html` din acel commit și actualizează revizia și SHA-256 din script și README. Nu se descarcă automat cod nou la pornirea aplicației.

## Redis opțional

Cu Redis disponibil local, pornește cu `mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.cache.type=redis"`. Poți porni Redis cu `docker compose -f database/compose.yaml up -d --wait`; instalarea nativă este disponibilă prin `.\setup.cmd redis` sau `bash setup.sh redis` pe sistemele suportate. Fără Redis, folosește pornirea normală. Cache-ul local nu are TTL; configurația Redis are TTL de 60 de secunde.
