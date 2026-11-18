# Java4Shell / CVE-2026-114938

The vulnerable decoder is in affected **HotSpot JVM versions**, not this
project's Java code or Maven dependencies. GitHub Actions temporarily disables
the Base64 intrinsic in its Java 21 JVM, but this does not protect other
machines running this software.

**Update to a vendor-confirmed patched JDK/JRE** wherever this Java project
is built or executed. Until that is possible, set
`JAVA_TOOL_OPTIONS=-XX:-UseBASE64Intrinsics` before launching *every*
affected JVM, then restart Java processes and verify
`UseBASE64Intrinsics = false` with `java -XX:+PrintFlagsFinal -version`.

On Windows PowerShell use
`$env:JAVA_TOOL_OPTIONS = '-XX:-UseBASE64Intrinsics'`.

Sources: [CERT-EU](https://www.cert.europa.eu/publications/security-advisories/2026-021/),
[Adoptium](https://adoptium.net/news/2026/11/temurin-november-2026-cspu-java4shell/).
