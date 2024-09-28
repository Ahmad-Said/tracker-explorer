package said.ahmad.javafx.tracker.system.file;

import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.net.URISyntaxException;

public class PathLayerDeserializer extends JsonDeserializer<PathLayer> {
    @Override
    public PathLayer deserialize(com.fasterxml.jackson.core.JsonParser jsonParser, com.fasterxml.jackson.databind.DeserializationContext deserializationContext) throws IOException {
        PathLayerSerialized pathLayerSerialized = jsonParser.readValueAs(PathLayerSerialized.class);
        PathLayer pathLayer = null;

        try {
            pathLayer = PathLayerHelper.parseURI(pathLayerSerialized.getPathURI()); // Ensure PathLayerHelper is available
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return pathLayer;
    }
}
