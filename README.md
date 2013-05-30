# Busdrone
Busdrone presents a Google Maps-based interface for viewing Seattle-area
live bus status by relaying data from the web service provided by
[Busview](http://www.its.washington.edu/projects/busview_overview.html).
Busdrone includes a Java service to relay the serialized objects from Busview
as JSON objects through websockets. Objects are cached locally using Redis.

## Dependencies
### Server
* [Redis](http://redis.io)
* [Jedis](https://github.com/xetorthio/jedis/)
 * [Apache Commons pool](http://commons.apache.org/proper/commons-pool/download_pool.cgi)
* [json-io](http://code.google.com/p/json-io/)
* [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket/)

### Client
* [jQuery](http://jquery.com/) (linked)