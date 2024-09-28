package said.ahmad.javafx.tracker.system.file;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;

import java.io.IOException;

public class PathLayerSerializer extends JsonSerializer<PathLayer> {
    @Override
    public void serialize(PathLayer pathLayer, JsonGenerator jsonGenerator, com.fasterxml.jackson.databind.SerializerProvider serializers) throws IOException {
        PathLayerSerialized pathLayerSerialized = new PathLayerSerialized();
        if (pathLayer.isLocal()) {
            pathLayerSerialized.setPathURI(pathLayer.toFileIfLocal().toURI().toString());
        } else {
            pathLayerSerialized.setPathURI(pathLayer.toURI().toString());
        }
        jsonGenerator.writeObject(pathLayerSerialized);
    }
}

