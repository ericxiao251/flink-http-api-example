# flink-http-api-example
### Steps to Run Application
1. Have Java installed.
    We can do this with a tool called [SDK Man](https://sdkman.io/install).
    
    ```
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    
    sdk install java 11.0.23-zulu
    ```

2. Run the Java program.

    ```
    git clone git@github.com:ericxiao251/flink-http-api-example.git
    cd flink-http-api-example/flink-http-api-example/
    
    ./gradlew run 
    ```

### Main Pieces

1. Java repo setup.

    Flink Dependencies: https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/configuration/overview/#gradle-build-script.

    Other Dependencies:
    * Http client.
    * JSON to Java Object (POJO) toolkit.

2. A simple Flink application (Main.java).

    Example Main: https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/datastream/overview/#example-program.
    Example Flink Async I/O function: https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/operators/asyncio/#async-io-api.

3. An class that implements the Async I/O abstract class in Flink.

    3 main abstract functions to implement:
    1. `public void open(Configuration parameters)`. This is what I would call the "initializer" of the operator, where you can start the HTTP client.
    2. `public void close()`. This function is used to gracefully shutdown everything when your Flink application is turned off.
    3. `public void asyncInvoke(String s, ResultFuture<String> resultFuture)`.

   
5. (Optional) A Java "POJO" to represent the HTTP request response body, the Java way.

### Main Things to Think About When Making HTTP Requests

1. Async vs Synchronous

    Sometimes HTTP Requests can take a lot longer, we do not want to block and create a bottleneck in our system.
   
3. Delivery Guarentees
4. Authentication
5. Retries

    * Flink has some built in support for retries: https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/operators/asyncio/#retry-support.

6. Monitoring

    * Request duration
    * Request response code
    * Request volume/rate
  
    Some talks on how to do this in Flink: https://www.youtube.com/watch?v=XgumbKHE2Zw.

7. Batching
