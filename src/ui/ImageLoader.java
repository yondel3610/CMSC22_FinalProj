package ui;

import javafx.scene.image.Image;

public class ImageLoader {
    public static Image load(String filename) {
        String path = "/images/" + filename;
        var resource = ImageLoader.class.getResource(path);

        if (resource == null) {
            throw new IllegalArgumentException(
                "Image not found: " + path
            );
        }
        return new Image(resource.toExternalForm());
    }
}
