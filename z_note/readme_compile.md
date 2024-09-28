download javafx version from here, current java version is 17
https://gluonhq.com/products/javafx/

add the following to the VM options
```bash
--module-path "/path/to/javafx-sdk-17.0.12/lib"
--add-modules javafx.controls,javafx.fxml
```

add the following to the VM options
```bash
--add-opens java.base/java.util=ALL-UNNAMED
```