# Flarecord

[![Build](https://img.shields.io/github/actions/workflow/status/firesmasher-c6/flarecord/gradle.yml?branch=dev%2F1.0.0&label=build)](https://github.com/firesmasher-c6/flarecord/actions/workflows/gradle.yml?query=branch%3Adev%2F1.0.0)
[![Minecraft proxy](https://img.shields.io/badge/Minecraft-proxy-3c8527?logo=minecraft&logoColor=white)](https://github.com/firesmasher-c6/flarecord)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/firesmasher-c6/flarecord?style=social)](https://github.com/firesmasher-c6/flarecord/stargazers)

**Flarecord is a Minecraft proxy based on Velocity, with readable YAML
configuration and a guided first start.** It is for server owners who want a
familiar Velocity-based proxy without beginning with a large TOML file.

Flarecord keeps Velocity's proxy foundation and focuses its own additions on
easier setup, clearer configuration, recognizable Flarecord branding, and a
small set of built-in connection protections. It is not a replacement for a
network-wide Redis system or a dedicated DDoS mitigation service.

> Flarecord is an early release. Read [Feature availability](#feature-availability)
> before choosing it for a production network.

## What Flarecord offers

- **Guided first start.** Answer a few prompts in a terminal, or provide
  environment variables when running under a game panel or container.
- **YAML configuration.** Configure proxy listeners and backend servers in
  `velocity.yml`; Flarecord options live in `flarecord.yml`.
- **Velocity foundation.** Keep using the established Velocity proxy model and
  test your existing Velocity plugins with Flarecord before switching a live
  network.
- **Paper forwarding support.** Configure modern forwarding with a shared secret
  to securely pass player identity and address information to Paper backends.
- **Basic connection-abuse controls.** Flarecord limits concurrent login
  connections from an address and temporarily blocks an address after it crosses
  the configured limit. Velocity's packet limiter is also available.
- **Performance-conscious defaults.** Netty chooses its worker count based on
  the available processors unless you explicitly set an override.
- **Flarecord identity.** The proxy reports the Flarecord name in the proxy
  version/brand information.

## Choosing a proxy

| | [Velocity](https://github.com/PaperMC/Velocity) | [Velocity-CTD](https://github.com/SendableMetatype/Velocity-CTD) | Flarecord |
|---|---|---|---|
| Best fit | A stable, general-purpose proxy backed by the upstream project | Networks wanting the CTD project's additional network and queue features | Owners who prefer guided setup and YAML-first configuration |
| Configuration | Upstream `velocity.toml` format | Velocity-derived configuration; consult CTD's documentation for its options | `velocity.yml`, `flarecord.yml`, and `animations.yml` |
| First-run experience | Generates the standard Velocity configuration | Generates the standard Velocity-derived configuration | Interactive setup when a console is available; environment-driven defaults in headless hosting |
| Multi-proxy / Redis | Not a built-in Redis cluster service | Redis and cross-proxy features are a central part of CTD | No built-in Redis cluster or cross-proxy player service |
| Queueing | Not a full built-in admission queue | Includes a mature configurable queue system and related commands | A queue foundation is present, but automatic player admission and queue commands are not yet available |
| Connection protection | Standard Velocity connection and packet controls | CTD-specific features and inherited Velocity controls | Inherited packet controls plus a per-IP concurrent login limit and temporary block |
| Project focus | Upstream Velocity development and compatibility | An all-in-one Velocity-derived feature set | A simpler setup and configuration experience on a Velocity foundation |

These projects have different goals. This table is a high-level guide, not a
benchmark or a promise that every plugin or configuration is interchangeable.
Read the documentation for the specific version you plan to run:
[Velocity](https://github.com/PaperMC/Velocity) ·
[Velocity-CTD](https://github.com/SendableMetatype/Velocity-CTD) ·
[Flarecord](https://github.com/firesmasher-c6/flarecord).

## Download and install

Flarecord builds are published by
[GitHub Actions](https://github.com/firesmasher-c6/flarecord/actions/workflows/gradle.yml?query=branch%3Adev%2F1.0.0).
Open a successful run on the `dev/1.0.0` branch and download its build artifact.
GitHub Actions artifacts are temporary; there is not yet a permanent Flarecord
release download.

1. Install a supported Java runtime. Java 21 or newer is recommended.
2. Put the Flarecord proxy JAR in its own server directory.
3. Start it with an appropriate memory limit, for example:

   ```text
   java -Xms1G -Xmx2G -jar flarecord.jar
   ```

4. Complete the first-run setup or configure the environment variables described
   below.
5. Configure the proxy's backend servers and the forwarding mode on each backend.
6. Restart Flarecord after changing configuration.

Choose memory settings for your machine and player load. The example is not a
performance guarantee and should not be copied as a substitute for monitoring
your server.

## First start and hosting panels

When no proxy configuration exists, Flarecord creates its configuration files.
If the process has an interactive terminal, the setup wizard asks for the bind
address, an optional backend address, and an optional public hostname. Press
Enter to accept a displayed default. You may leave the backend address blank and
add backends later.

Many game panels and containers do not provide an interactive Java console. In
that case, Flarecord uses safe defaults and reads these optional environment
variables:

| Variable | Example | Purpose |
|---|---|---|
| `FLARECORD_BIND` | `0.0.0.0:25565` | Address and port for the proxy listener |
| `FLARECORD_BACKEND_NAME` | `lobby` | Name for the configured backend |
| `FLARECORD_BACKEND_ADDRESS` | `127.0.0.1:30066` | Backend address in `host:port` form |
| `FLARECORD_PUBLIC_HOST` | `play.example.com` | Optional hostname routed to the configured backend |

Set panel variables in the server's startup/environment settings, not in the
Java command's arguments. If no backend is supplied, the proxy can start, but
players cannot join a backend until one is added to `velocity.yml`.

To run setup again on an existing installation, set `FLARECORD_SETUP=1` for one
startup. Flarecord backs up the existing `velocity.yml` before writing a new
one. Remove the variable afterwards; otherwise setup will run again each time.

## Configure backends

`velocity.yml` contains the proxy's listener, authentication, forwarding,
backend, routing, and packet-limiter settings. A minimal backend setup looks
like this:

```yaml
bind: "0.0.0.0:25565"
player-info-forwarding-mode: "MODERN"
forwarding-secret-file: "forwarding.secret"

servers:
  lobby: "127.0.0.1:30066"
  try:
    - "lobby"

forced-hosts: {}
```

Replace the example address with the actual address and port of your Paper
server. Add more entries under `servers` for more backends. `try` is the ordered
list of servers Flarecord should try when a player first connects or needs a
fallback. A `forced-hosts` entry is optional; add one only when its server name
exists under `servers`.

For example, route `factions.example.com` to an existing `factions` backend with:

```yaml
servers:
  lobby: "127.0.0.1:30066"
  factions: "127.0.0.1:30067"
  try:
    - "lobby"

forced-hosts:
  "factions.example.com":
    - "factions"
```

The hostname must point to the proxy in your DNS provider, and the backend name
must match exactly. An unknown backend in `forced-hosts` makes the configuration
invalid.

## Connect Paper servers

Flarecord supports Paper backends. The message “This server requires you to
connect with Velocity” usually means the proxy forwarding mode and Paper's
forwarding settings do not match; it does not mean Paper is unsupported.

1. In `velocity.yml`, set `player-info-forwarding-mode` to `"MODERN"`.
2. Keep the forwarding secret in `forwarding.secret` private. Use the same
   secret on Flarecord and on every backend that trusts it.
3. In Paper's `config/paper-global.yml`, enable Velocity forwarding and set its
   online mode and secret to match Flarecord:

   ```yaml
   proxies:
     velocity:
       enabled: true
       online-mode: true
       secret: "replace-with-the-same-private-secret"
   ```

4. Restart Paper and Flarecord.
5. Restrict direct public access to backend ports so players cannot bypass the
   proxy and its forwarding checks.

Set `online-mode` in Paper's Velocity section to the same value as
`online-mode` in Flarecord's `velocity.yml`. Keep online authentication enabled
unless you intentionally understand and have secured an offline-mode network.
Never publish or share the forwarding secret.

## Flarecord settings

`flarecord.yml` contains Flarecord-specific settings. The currently active
connection-protection settings are:

```yaml
general:
  enabled: true

security:
  anti-bot:
    enabled: true
    max-connections-per-ip: 20
    temporary-block-duration-seconds: 120
```

The guard counts concurrent login handshakes from the same IP address. If that
address exceeds the configured concurrent connection limit, additional
connections are refused and the address is temporarily blocked for the
configured duration. Shared networks, NAT gateways, and busy schools may have
many legitimate players on one address; raise the limit if needed. Setting
`security.anti-bot.enabled` to `false` disables this Flarecord guard.

Other Flarecord settings and example files are included for future features.
Changing a setting does not imply that every planned feature is active today;
see [Feature availability](#feature-availability).

## Configuration files

| File | What it is for |
|---|---|
| `velocity.yml` | Proxy listener, authentication, backend servers, forced hosts, query, metrics, and packet-limiter settings |
| `flarecord.yml` | Flarecord feature switches and limits, including the per-IP connection guard |
| `animations.yml` | Named animation definitions; currently loaded and validated, but not automatically played by the proxy |
| `forwarding.secret` | Private shared secret for modern forwarding to Paper |

Back up these files before making substantial changes. YAML indentation matters:
use spaces, not tabs. Keep values containing special characters in quotes.

## Performance and protection

Flarecord uses Velocity's network foundation and packet processing. Netty selects
the number of worker loops based on the processors available to the JVM by
default. Most users should leave this automatic setting alone. If you have
measured a reason to change it, you can set a positive worker count with the JVM
property `-Dflarecord.netty-worker-threads=4` before `-jar`.

The proxy also exposes Velocity's packet-limiter configuration in `velocity.yml`.
Start with the included defaults and tune only after observing legitimate
traffic; overly strict limits can disconnect players.

These controls provide application-level limits, not comprehensive DDoS
protection. They cannot absorb an attack that saturates your host's network
connection. For a public network:

- Use a hosting provider with network-level DDoS mitigation.
- Firewall backend servers so they accept traffic only from the proxy where
  possible.
- Keep modern forwarding enabled and protect its secret.
- Avoid exposing query or management ports unless you need them.
- Review logs and connection counts during unusual traffic.
- Do not use shared proxy addresses as a substitute for a firewall.

No player-count or throughput figure is promised: capacity depends on CPU,
memory, network quality, plugins, backend performance, and the traffic pattern.

## Feature availability

Flarecord is being developed incrementally. The current build includes first-run
configuration, YAML proxy configuration, Velocity-derived networking, proxy
branding, `/flarecord` status output, and a basic per-IP concurrent login guard.

Some configuration sections are present ahead of their complete player-facing
implementation. In particular, Flarecord does **not yet** provide a complete
automatic player queue like Velocity-CTD, Redis-based multi-proxy features, or an
animation playback command. Do not select Flarecord solely for those features.
If you need them now, use a project that documents and supports them for your
target version.

Velocity plugins may work because Flarecord is based on Velocity, but compatibility
is not guaranteed for every plugin or version. Test your plugins and forwarding
setup on a private staging server before moving a production network.

## Troubleshooting

| Symptom | What to check |
|---|---|
| “Server `<name>` for forced host does not exist” | Ensure that `<name>` is present under `servers`, or remove that host mapping. |
| “This server requires you to connect with Velocity” | Set Flarecord to `MODERN`, enable Velocity support in Paper, and use the same secret on both sides. |
| Paper sees the proxy address instead of the player address | Verify modern forwarding and the matching Paper configuration; restart both proxy and backend. |
| Proxy starts but players cannot join | Add a reachable backend under `servers` and make sure its port is open from the proxy host. |
| First-run prompts do not appear in a panel | Configure the `FLARECORD_*` environment variables in the panel. |
| A shared IP is temporarily blocked | Increase `max-connections-per-ip` or disable the Flarecord guard if the limit is inappropriate for your network. |
| A YAML file fails to load | Check indentation, quote special values, and review the startup log for the exact setting. |

## Getting help

Before asking for support, include the Flarecord version, Java version, relevant
startup messages, and the part of your configuration that is not secret. Remove
player IPs, account details, and all forwarding secrets first. Never upload
`forwarding.secret` or post it in a public issue.

Use the [GitHub issue tracker](https://github.com/firesmasher-c6/flarecord/issues)
for reproducible bugs and feature requests. For questions about the upstream
proxy, plugin API, or backend compatibility, also consult the
[Velocity documentation](https://docs.papermc.io/velocity/) and your backend's
documentation.

## License and attribution

Flarecord is licensed under the GNU General Public License, version 3 or later.
It is based on [Velocity](https://github.com/PaperMC/Velocity); see
[LICENSE](LICENSE) and the source headers for license and attribution details.
