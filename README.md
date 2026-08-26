# PSP · Repaso de concurrencia: carrera de 100 m

Reimplementación del ejercicio de repaso de PSP: ocho atletas esperan el pistoletazo y corren 100 m. Se usa `CountDownLatch` como barrera de salida y `ExecutorService` para gestionar los participantes. Los resultados se recopilan de forma segura y se ordenan por tiempo.

Por defecto el `main` usa tiempos acelerados; el modelo admite la escala real de 9–12 segundos de la práctica.

```bash
mvn verify
java -cp target/classes dev.rodrigosambade.sprint.SprintRace
```
