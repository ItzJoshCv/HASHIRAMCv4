# HASHIRAMCv2 — Cómo obtener el .jar para Paper 1.21.11

Esto es **código fuente** en Java. Para subirlo a tu host hay que convertirlo en un
`.jar`. Elige UNA de estas 3 formas (de más fácil a más manual):

---
## Forma 1 — Compilar en la nube con GitHub (SIN instalar nada) ⭐ recomendada
1. Crea un repositorio en GitHub (gratis).
2. Sube TODO el contenido de esta carpeta al repo (incluida la carpeta `.github`).
3. Ve a la pestaña **Actions** del repo. Se ejecutará solo el flujo "Build HashiraMCv2 jar".
4. Cuando termine (✓ verde), entra al run → sección **Artifacts** → descarga
   `HashiraMCv2-plugin`. Dentro está tu `.jar` listo.

---
## Forma 2 — Compilar en tu PC con IntelliJ IDEA (sin tocar la terminal)
1. Instala **JDK 21** y **IntelliJ IDEA** (Community sirve).
2. Abre esta carpeta como proyecto → IntelliJ importa Gradle solo.
3. Panel Gradle (derecha) → Tasks → build → doble clic en **build**.
4. El jar aparece en `build/libs/HashiraMCv2-1.0.0.jar`.

---
## Forma 3 — Terminal con Gradle
1. Instala **JDK 21** y **Gradle**.
2. En esta carpeta ejecuta:
   ```bash
   gradle build
   ```
3. El jar queda en `build/libs/HashiraMCv2-1.0.0.jar`.

---
## Instalar en tu host Paper 1.21.11
1. Sube el `.jar` a la carpeta `plugins/`.
2. (Recomendado) instala **Vault** + un plugin de economía (EssentialsX) para que
   funcionen las recompensas de dinero de las misiones. Sin esto el plugin arranca
   igual, pero las misiones de dinero quedan inactivas.
3. Sube el **resource pack** (`HashiraMCv2-ResourcePack.zip`) a un hosting con URL
   directa y pásalo en `server.properties`:
   ```
   resource-pack=https://TU-URL/HashiraMCv2-ResourcePack.zip
   resource-pack-sha1=6364cb73ad56210f5930420123a44b41f5c8cdce
   require-resource-pack=true
   ```
4. Reinicia el servidor. Config en `plugins/HashiraMCv2/config.yml`.
   → Revisa la misión `player-bounty`: cambia `target: NombreDelJugador` por un
     jugador real o quítala.

## Comandos
- `/offhands [give <jugador> <objeto>]`
- `/corazon <give|take|info> <jugador> <cantidad> [permanente|temporal] [minutos]`
- `/misiones`

Permiso admin: `hashiramcv2.admin` (por defecto solo OP).
