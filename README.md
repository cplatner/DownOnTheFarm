# Down on the Farm

During the [FarmVille](https://en.wikipedia.org/wiki/FarmVille) craze on
Facebook in about 2010, I recall getting dozens of message every day from
friends who were playing at the time.  I first muted all of those messages,
but later thought of an even better revenge.  I'd write my own 'game' that
would instead return a depressing status message related to farming.  At
the time, I didn't know about apps like [Cow Clicker](https://en.wikipedia.org/wiki/Cow_Clicker),
but making fun of FarmVille was definitely the idea.

I never made the app, but recently decided to put some code behind the idea.

This is the start of that code.  The basic idea is that crops die, animals die,
and machinery breaks.  However rewarding, farming (and ranching) is also a
continuous stream of woes.

For a given set of farming activities (planting, plowing, irrigating,
harvesting), this will produce a random depressing message.

## Building

1. Clone the repository

2. Build:

   ```bash
   ./gradlew build
   ```

3. Review test results:

   Open file:///./build/reports/tests/test/index.html in a web browser.

## Running

1. Start the application in a Docker container:

   ```bash
   ./gradlew bootRun
   ```

2. Use curl to get a farm status message:

   ```bash
   curl http://localhost:8080/api/v1/farm --header 'Content-Type: application/json' --data '{ "action": "harvest" }'

   ```

   Note that rate limiting is used, and it is set to a small number.  If no
   values are returned, run curl with -v to see if a 429 status is returned.

3. View the OpenAPI page:

   Open http://localhost:8080/swagger-ui/index.html in a web browser.

## Examples

   ```bash
   curl http://localhost:8080/api/v1/farm --header 'Content-Type: application/json' --data '{ "action": "plant" }'
   ```

   *A tornado sweeps through a small town near your farm, and destroys your barn in the process*

   ```bash
   curl http://localhost:8080/api/v1/farm --header 'Content-Type: application/json' --data '{ "action": "harvest" }'
   ```

   *A hailstorm destroys your crops*
