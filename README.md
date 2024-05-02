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

    Dependencies: https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/configuration/overview/#gradle-build-script.

3. A simple Flink application (Main.java).

    Example Main: https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/datastream/overview/#example-program.

4. An class that implements the Async I/O abstract class in Flink.

   
5. (Optional) A Java "POJO" to represent the HTTP request response body, the Java way.
