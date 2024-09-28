download javafx version from here, current java version is 17
https://gluonhq.com/products/javafx/

add the following to the VM options
```bash
--module-path "/path/to/sdk/javafx-sdk-21.0.4/lib"
--add-modules javafx.controls,javafx.fxml
```

add the following to the VM options
```bash
--add-opens java.base/java.util=ALL-UNNAMED
```

for windows build install wix toolset and add its binary to environment path
https://wixtoolset.org/docs/wix3/