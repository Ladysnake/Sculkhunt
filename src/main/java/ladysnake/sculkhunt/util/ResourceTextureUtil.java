package ladysnake.sculkhunt.util;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

public final class ResourceTextureUtil extends ResourceTexture {
    public ResourceTextureUtil(Identifier location) {
        super(location);
    }

    public static NativeImage load(ResourceManager resourceManager, Identifier identifier) throws IOException {
        return TextureData.load(resourceManager, identifier).getImage();
    }
}